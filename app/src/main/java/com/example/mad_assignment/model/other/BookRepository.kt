package com.example.mad_assignment.model.other

import com.example.mad_assignment.model.dao_and_database.CatalogDao
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookWithLoans
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.UserProfile
import com.example.mad_assignment.model.entity.toBook
import com.example.mad_assignment.model.entity.toCategory
import com.example.mad_assignment.model.entity.toLocal
import com.example.mad_assignment.model.remote.FirebaseDataSource
import com.example.mad_assignment.network.BookApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/* a live firebase flow must never block or break the book list that comes from room */
private fun <T> Flow<T>.orInitial(initial: T): Flow<T> =
    onStart { emit(initial) }.catch { emit(initial) }

class BookRepository(
    private val catalogDao: CatalogDao,
    private val remote: FirebaseDataSource,
    private val api: BookApi,
    private val seed: CatalogSeed
) {
    private val seedLock = Mutex()

    /* authentication */
    val authState: Flow<String?> = remote.authStateFlow()
    fun currentUid(): String? = remote.currentUid()
    suspend fun signIn(email: String, password: String) = remote.signIn(email, password)
    suspend fun signUp(name: String, email: String, password: String) =
        remote.signUp(name, email, password)
    fun signOut() = remote.signOut()
    suspend fun loadOrCreateProfile(): UserProfile? = remote.loadOrCreateProfile()

    /* books = local catalog (room) + availability (bookStatus) + the loans this user may see */
    fun books(uid: String?, isAdmin: Boolean): Flow<List<BookWithLoans>> = combine(
        catalogDao.books(),
        remote.statusFlow().orInitial(emptyList()),
        remote.loansFlow(uid, isAdmin).orInitial(emptyList())
    ) { local, status, loans ->
        /* an empty status list means "not received yet" (offline or not published) */
        val statusKnown = status.isNotEmpty()
        val activeByBook = status.associate { it.id to it.activeLoanId }
        val loansByBook = loans.groupBy { it.bookId }
        local.map { it.toBook() }
            .sortedBy { it.title.lowercase() }
            .map { book ->
                BookWithLoans(
                    book = book,
                    loans = loansByBook[book.id].orEmpty(),
                    activeLoanId = activeByBook[book.id],
                    availabilityKnown = statusKnown
                )
            }
    }

    val categories: Flow<List<CategoryEntity>> =
        catalogDao.categories().map { list -> list.map { it.toCategory() } }

    /* catalog sync */

    /* first start: copy the book list that ships in the app into room */
    suspend fun ensureSeeded() = seedLock.withLock {
        if (catalogDao.version() != null || catalogDao.bookCount() > 0) return@withLock
        val data = seed.read()
        if (data == null || data.books.isEmpty()) return@withLock
        catalogDao.replaceCatalog(data.books, data.categories, data.version)
    }

    /* compare the online version with the one on the phone */
    suspend fun syncCatalog(isAdmin: Boolean = false) {
        ensureSeeded()
        val remoteVersion = remote.catalogVersion()
        val localVersion = catalogDao.version() ?: 0L
        if (remoteVersion == null) {
            /* nothing online yet: the first admin uploads the catalog from the app */
            if (isAdmin) publishLocalCatalog(localVersion)
        } else if (remoteVersion > localVersion) {
            val books = remote.fetchBooks()
            val categories = remote.fetchCategories()
            catalogDao.replaceCatalog(
                books.map { it.toLocal() },
                categories.map { it.toLocal() },
                remoteVersion
            )
        }
    }

    private suspend fun publishLocalCatalog(version: Long) {
        val books = catalogDao.allBooks().map { it.toBook() }
        if (books.isEmpty()) return
        val categories = catalogDao.allCategories().map { it.toCategory() }
        remote.publishCatalog(books, categories, maxOf(version, 1L))
    }

    private suspend fun syncAfterWrite() {
        try {
            syncCatalog()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            /* the change is saved online, the phone updates on the next start */
        }
    }

    /* catalog changes (admin): write online, then pull the new version */
    suspend fun insertBook(book: BookEntity): String {
        val id = remote.insertBook(book)
        syncAfterWrite()
        return id
    }

    suspend fun updateBook(book: BookEntity) {
        remote.updateBook(book)
        syncAfterWrite()
    }

    suspend fun deleteBook(book: BookEntity) {
        remote.deleteBook(book.id)
        syncAfterWrite()
    }

    suspend fun addCategory(name: String): String {
        val id = remote.addCategory(name)
        syncAfterWrite()
        return id
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        remote.deleteCategory(category.id)
        syncAfterWrite()
    }

    /* loans */
    suspend fun requestReturn(bookId: String): Boolean {
        val uid = remote.currentUid() ?: throw IllegalStateException("Sign in required")
        return remote.requestReturn(bookId, uid)
    }

    suspend fun confirmReturn(bookId: String): Boolean = remote.confirmReturn(bookId)

    suspend fun borrowBook(
        bookId: String,
        borrowerName: String
    ): Boolean {
        val uid = remote.currentUid() ?: throw IllegalStateException("Sign in required")
        val title = catalogDao.titleOf(bookId).orEmpty()
        return remote.borrowIfAvailable(bookId, title, borrowerName, uid)
    }

    /* Network */
    suspend fun searchOnline(query: String): List<OnlineBook> {
        val response = api.searchBooks(query)
        return response.docs.orEmpty()
            .mapNotNull { it.toOnlineBook() }
            .distinctBy { it.title.lowercase() to it.authors.lowercase() }
    }

    /* find one book by its ISBN, null = not found */
    suspend fun findByIsbn(isbn: String): OnlineBook? =
        api.searchBooks("isbn:$isbn").docs?.firstNotNullOfOrNull { it.toOnlineBook() }
}
