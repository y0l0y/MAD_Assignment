package com.example.mad_assignment.model.other

import com.example.mad_assignment.model.dao_and_database.OfflineDao
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookWithLoans
import com.example.mad_assignment.model.entity.BorrowWithBook
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.CurrentBorrowEntity
import com.example.mad_assignment.model.entity.OfflineBookEntity
import com.example.mad_assignment.model.remote.FirebaseDataSource
import com.example.mad_assignment.network.BookApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class BookRepository(
    private val offlineDao: OfflineDao,
    private val remote: FirebaseDataSource,
    private val api: BookApi
) {
    suspend fun ensureSignedIn() {
        remote.ensureUid()
    }
    fun currentUid(): String? = remote.currentUid()
    fun currentUserName(): String = remote.currentUserName()

    /* books */
    val books: Flow<List<BookWithLoans>> = combine(
        remote.booksFlow(),
        remote.loansFlow()
    ) { books, loans ->
        val loansByBook = loans.groupBy { it.bookId }
        books.sortedBy { it.title.lowercase() }
            .map { BookWithLoans(it, loansByBook[it.id].orEmpty()) }
    }
    suspend fun insertBook(book: BookEntity): String = remote.insertBook(book)
    suspend fun updateBook(book: BookEntity) = remote.updateBook(book)
    suspend fun deleteBook(book: BookEntity) = remote.deleteBook(book.id)

    /* loans */
    suspend fun returnBook(bookId: String): Boolean = remote.returnBook(bookId)
    suspend fun borrowBook(
        bookId: String,
        borrowerName: String
    ): Boolean {
        val uid = remote.ensureUid().orEmpty()
        return remote.borrowIfAvailable(bookId, borrowerName, uid)
    }

    /* categories */
    val categories: Flow<List<CategoryEntity>> = remote.categoriesFlow()
    suspend fun addCategory(name: String): String = remote.addCategory(name)

    suspend fun deleteCategory(category: CategoryEntity) = remote.deleteCategory(category.id)

    /* local cache (room): offline book list + active loan for this user */
    val offlineBook: Flow<List<OfflineBookEntity>> = offlineDao.getOfflineBook()
    val myBorrows: Flow<List<BorrowWithBook>> = offlineDao.getBorrowsWithBook()

    suspend fun cacheSnapshot(items: List<BookWithLoans>) {
        val uid = remote.currentUid()
        val now = System.currentTimeMillis()
        val books = items.map {
            OfflineBookEntity(
                id = it.book.id,
                title = it.book.title,
                author = it.book.author,
                isbn = it.book.isbn,
                coverBase64 = it.book.coverBase64,
                coverUrl = it.book.coverUrl,
                cachedAt = now
            )
        }
        val borrows = items.mapNotNull { item ->
            val loan = item.activeLoan
            if (loan != null && uid != null && loan.borrowerUid == uid) {
                CurrentBorrowEntity(
                    loanId = loan.id,
                    bookId = item.book.id,
                    borrowerName = loan.borrowerName,
                    borrowedAt = loan.borrowedAt,
                    dueAt = loan.dueAt
                )
            } else null
        }
        offlineDao.replaceCache(books, borrows)
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