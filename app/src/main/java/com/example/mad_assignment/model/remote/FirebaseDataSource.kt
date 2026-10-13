package com.example.mad_assignment.model.remote

import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.model.other.BookRules
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    /* authentication */
    fun currentUid(): String? = auth.currentUser?.uid

    fun currentUserName(): String = auth.currentUser?.displayName.orEmpty()

    suspend fun ensureUid(): String? {
        auth.currentUser?.let { return it.uid }
        return auth.signInAnonymously().await().user?.uid
    }

    /* live data */
    fun booksFlow(): Flow<List<BookEntity>> = booksCol.asFlow()
    fun categoriesFlow(): Flow<List<CategoryEntity>> = categoriesCol.asFlow()
    fun loansFlow(): Flow<List<LoanEntity>> = loansCol.asFlow()

    /* books */
    suspend fun insertBook(book: BookEntity): String {
        val ref = booksCol.document()
        ref.set(book).await()
        return ref.id
    }

    suspend fun updateBook(book: BookEntity) {
        booksCol.document(book.id).update(
            mapOf(
                "title" to book.title,
                "author" to book.author,
                "isbn" to book.isbn,
                "categoryId" to book.categoryId,
                "coverBase64" to book.coverBase64,
                "coverUrl" to book.coverUrl
            )
        ).await()
    }

    suspend fun deleteBook(bookId: String) {
        val loans = loansCol.whereEqualTo("bookId", bookId).get().await()
        val batch = db.batch()
        /* delete each loan */
        loans.documents.forEach { batch.delete(it.reference) }
        /* delete the book */
        batch.delete(booksCol.document(bookId))
        /* send them all at once */
        batch.commit().await()
    }

    /* categories */
    suspend fun addCategory(name: String): String {
        val ref = categoriesCol.document()
        ref.set(CategoryEntity(name = name)).await()
        return ref.id
    }

    suspend fun deleteCategory(categoryId: String) {
        val books = booksCol.whereEqualTo("categoryId", categoryId).get().await()
        val batch = db.batch()
        books.documents.forEach { batch.update(it.reference, "categoryId", null) }
        batch.delete(categoriesCol.document(categoryId))
        batch.commit().await()
    }

    /* loans */
    suspend fun borrowIfAvailable(
        bookId: String,
        borrowerName: String,
        borrowerUid: String
    ): Boolean = db.runTransaction { tx ->
        val bookRef = booksCol.document(bookId)
        val snapshot = tx.get(bookRef)
        if (!snapshot.exists())
            throw IllegalStateException("Book no longer exists")
        if (snapshot.getString("activeLoanId") != null)
            return@runTransaction false
        val loanRef = loansCol.document()
        val now = System.currentTimeMillis()
        tx.set(
            loanRef,
            LoanEntity(
                bookId = bookId,
                borrowerName = borrowerName,
                borrowerUid = borrowerUid,
                borrowedAt = now,
                dueAt = now + BookRules.LOAN_DAYS * BookRules.DAY_IN_MILLIS
            )
        )
        tx.update(bookRef, "activeLoanId", loanRef.id)
        true
    }.await()

    suspend fun returnBook(bookId: String): Boolean = db.runTransaction { tx ->
        val bookRef = booksCol.document(bookId)
        val loanId = tx.get(bookRef).getString("activeLoanId") ?: return@runTransaction false
        tx.update(loansCol.document(loanId), "returnedAt", System.currentTimeMillis())
        tx.update(bookRef, "activeLoanId", null)
        true
    }.await()
}