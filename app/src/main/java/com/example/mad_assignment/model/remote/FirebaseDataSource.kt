package com.example.mad_assignment.model.remote

import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookStatus
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.model.entity.UserProfile
import com.example.mad_assignment.model.other.BookRules
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/* turn data change from firestore to flow */
/* addSnapshotListener = "call me when data change" */
/* trysend() put the list to flow */
/* close the listener when no use */
private inline fun <reified T : Any> Query.asFlow(): Flow<List<T>> = callbackFlow {
    val registration = addSnapshotListener { snapshots, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshots != null) trySend(snapshots.toObjects(T::class.java))
    }
    awaitClose { registration.remove() }
}

class FirebaseDataSource(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
){
    private val booksCol get() = db.collection("books")
    private val categoriesCol get() = db.collection("categories")
    private val loansCol get() = db.collection("loans")
    private val statusCol get() = db.collection("bookStatus")
    private val metaDoc get() = db.collection("meta").document("catalog")
    private val usersCol get() = db.collection("users")

    /* authentication */
    fun currentUid(): String? = auth.currentUser?.uid

    /* emits the uid on every sign in / sign out (null = signed out) */
    fun authStateFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(name: String, email: String, password: String) {
        val uid = auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            ?: throw IllegalStateException("Account was created but no user ID was returned")
        usersCol.document(uid).set(UserProfile(name = name, email = email)).await()
    }

    fun signOut() = auth.signOut()

    /* read users/{uid}; create a default profile if it is missing (old or half-created account) */
    suspend fun loadOrCreateProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        val ref = usersCol.document(user.uid)
        val snapshot = ref.get().await()
        if (snapshot.exists()) return snapshot.toObject(UserProfile::class.java)
        val email = user.email.orEmpty()
        val profile = UserProfile(name = email.substringBefore('@'), email = email)
        ref.set(profile).await()
        return profile
    }

    /* live data: only the small documents, the book list itself is stored in room */
    fun statusFlow(): Flow<List<BookStatus>> = statusCol.asFlow()
    fun loansFlow(): Flow<List<LoanEntity>> = loansCol.asFlow()

    /* catalog (books + categories): read once, then copied into room */
    suspend fun catalogVersion(): Long? = metaDoc.get().await().getLong("version")

    suspend fun fetchBooks(): List<BookEntity> =
        booksCol.get().await().toObjects(BookEntity::class.java)

    suspend fun fetchCategories(): List<CategoryEntity> =
        categoriesCol.get().await().toObjects(CategoryEntity::class.java)

    /* every catalog change adds 1 to the version, so other phones know to download again */
    private fun bumpVersion(batch: WriteBatch) {
        batch.set(metaDoc, mapOf("version" to FieldValue.increment(1)), SetOptions.merge())
    }

    private fun bookFields(book: BookEntity) = mapOf(
        "title" to book.title,
        "author" to book.author,
        "isbn" to book.isbn,
        "categoryId" to book.categoryId,
        "coverBase64" to book.coverBase64,
        "coverUrl" to book.coverUrl
    )

    /* first admin start: upload the catalog that ships in the app (books, status, categories) */
    suspend fun publishCatalog(
        books: List<BookEntity>,
        categories: List<CategoryEntity>,
        version: Long
    ) {
        val writes = mutableListOf<(WriteBatch) -> Unit>()
        categories.forEach { category ->
            writes.add { batch ->
                batch.set(categoriesCol.document(category.id), mapOf("name" to category.name))
            }
        }
        books.forEach { book ->
            writes.add { batch -> batch.set(booksCol.document(book.id), bookFields(book)) }
            writes.add { batch ->
                batch.set(statusCol.document(book.id), mapOf<String, Any?>("activeLoanId" to null))
            }
        }
        /* a batch holds at most 500 writes */
        writes.chunked(400).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { write -> write(batch) }
            batch.commit().await()
        }
        metaDoc.set(mapOf("version" to version)).await()
    }

    /* books (admin) */
    suspend fun insertBook(book: BookEntity): String {
        val ref = booksCol.document()
        val batch = db.batch()
        batch.set(ref, bookFields(book))
        batch.set(statusCol.document(ref.id), mapOf<String, Any?>("activeLoanId" to null))
        bumpVersion(batch)
        batch.commit().await()
        return ref.id
    }

    suspend fun updateBook(book: BookEntity) {
        val batch = db.batch()
        batch.update(booksCol.document(book.id), bookFields(book))
        bumpVersion(batch)
        batch.commit().await()
    }

    /* the loan history stays, only the book and its status go */
    suspend fun deleteBook(bookId: String) {
        val batch = db.batch()
        batch.delete(booksCol.document(bookId))
        batch.delete(statusCol.document(bookId))
        bumpVersion(batch)
        batch.commit().await()
    }

    /* categories (admin) */
    suspend fun addCategory(name: String): String {
        val ref = categoriesCol.document()
        val batch = db.batch()
        batch.set(ref, mapOf("name" to name))
        bumpVersion(batch)
        batch.commit().await()
        return ref.id
    }

    suspend fun deleteCategory(categoryId: String) {
        val books = booksCol.whereEqualTo("categoryId", categoryId).get().await()
        val batch = db.batch()
        books.documents.forEach { batch.update(it.reference, "categoryId", null) }
        batch.delete(categoriesCol.document(categoryId))
        bumpVersion(batch)
        batch.commit().await()
    }

    /* loans: transactions, so they fail when offline instead of being saved for later */
    suspend fun borrowIfAvailable(
        bookId: String,
        bookTitle: String,
        borrowerName: String,
        borrowerUid: String
    ): Boolean = db.runTransaction { tx ->
        val statusRef = statusCol.document(bookId)
        val book = tx.get(booksCol.document(bookId))
        val status = tx.get(statusRef)
        if (!book.exists())
            throw IllegalStateException("Book no longer exists")
        if (status.getString("activeLoanId") != null)
            return@runTransaction false
        val loanRef = loansCol.document()
        val now = System.currentTimeMillis()
        tx.set(
            loanRef,
            LoanEntity(
                bookId = bookId,
                bookTitle = bookTitle,
                borrowerName = borrowerName,
                borrowerUid = borrowerUid,
                borrowedAt = now,
                dueAt = now + BookRules.LOAN_DAYS * BookRules.DAY_IN_MILLIS
            )
        )
        tx.set(statusRef, mapOf("activeLoanId" to loanRef.id))
        true
    }.await()

    /* borrower asks for the return to be confirmed */
    suspend fun requestReturn(bookId: String, uid: String): Boolean = db.runTransaction { tx ->
        val loanId = tx.get(statusCol.document(bookId)).getString("activeLoanId")
            ?: return@runTransaction false
        val loanRef = loansCol.document(loanId)
        val loan = tx.get(loanRef)
        if (loan.getString("borrowerUid") != uid)
            throw IllegalStateException("Not your loan")
        if (loan.getLong("returnRequestedAt") != null)
            return@runTransaction false
        tx.update(loanRef, "returnRequestedAt", System.currentTimeMillis())
        true
    }.await()

    /* admin confirms a requested return: closes the loan and puts the book back on the shelf */
    suspend fun confirmReturn(bookId: String): Boolean = db.runTransaction { tx ->
        val statusRef = statusCol.document(bookId)
        val loanId = tx.get(statusRef).getString("activeLoanId") ?: return@runTransaction false
        val loanRef = loansCol.document(loanId)
        if (tx.get(loanRef).getLong("returnRequestedAt") == null)
            return@runTransaction false
        tx.update(loanRef, "returnedAt", System.currentTimeMillis())
        tx.set(statusRef, mapOf<String, Any?>("activeLoanId" to null))
        true
    }.await()
}
