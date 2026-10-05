package com.example.mad_assignment.model.other

import com.example.mad_assignment.model.dao_and_database.BooksDao
import com.example.mad_assignment.model.dao_and_database.CategoryDao
import com.example.mad_assignment.model.dao_and_database.LoanDao
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookWithLoans
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.network.BookApi
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BooksDao,
    private val loanDao: LoanDao,
    private val categoryDao: CategoryDao,
    private val api: BookApi
) {
    /* books */
    val books: Flow<List<BookWithLoans>> = bookDao.getBooksWithLoans()
    fun getBookWithLoans(bookId: Long): Flow<BookWithLoans?> = bookDao.getBookWithLoans(bookId)
    suspend fun insertBook(book: BookEntity): Long = bookDao.insertBook(book)
    suspend fun updateBook(book: BookEntity) = bookDao.updateBook(book)
    suspend fun deleteBook(book: BookEntity) = bookDao.deleteBook(book)
    suspend fun getBooksCount(): Int = bookDao.getBooksCount()

    /* loans */
    suspend fun returnBook(bookId: Long) = loanDao.returnLoan(bookId, System.currentTimeMillis())
    suspend fun borrowBook(
        bookId: Long,
        borrowerName: String
    ): Boolean = loanDao.borrowIfAvailable(
        LoanEntity(
            bookId = bookId,
            borrowerName = borrowerName
        )
    )

    /* categories */
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun addCategory(name: String): Long {
        val id = categoryDao.insertCategory(CategoryEntity(name = name))
        return if (id != -1L) id else categoryDao.findByName(name)?.id ?: -1L
    }

    /* seed data: categories -> Books -> Loans */
    suspend fun seedIfEmpty() {
        if (getBooksCount() > 0) return
        val categoryIds = TestData.categories.associateWith { addCategory(it) }
        TestData.initialBooks.forEach { seed ->
            val bookId = bookDao.insertBook(
                BookEntity(
                    title = seed.title,
                    author = seed.author,
                    isbn = seed.isbn,
                    categoryId = seed.category?.let { categoryIds[it] }
                )
            )
            seed.borrower?.let {
                loanDao.insertLoan(
                    LoanEntity(
                        bookId = bookId,
                        borrowerName = it
                    )
                )
            }
        }
    }

    /* Network */
    suspend fun searchOnline(query: String): List<OnlineBook> {
        val response = api.searchBooks(query)
        return response.docs.orEmpty()
            .mapNotNull { it.toOnlineBook() }
            .distinctBy { it.title.lowercase() to it.authors.lowercase() }
    }
}