package com.example.mad_assignment.Model

import com.example.mad_assignment.Network.BookApi
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BooksDao,
    private val api: BookApi
) {
    val books: Flow<List<BookEntity>> = bookDao.getAllBooks()
    suspend fun insertBook(book: BookEntity) =
        bookDao.insertBook(book)
    suspend fun updateBook(book: BookEntity) =
        bookDao.updateBook(book)
    suspend fun deleteBook(book: BookEntity) =
        bookDao.deleteBook(book)
    suspend fun getBooksCount(): Int {
        return bookDao.getBooksCount()
    }
    suspend fun searchOnline(query: String): List<OnlineBook> {
        val response = api.searchBooks(query)
        return response.docs.orEmpty()
            .mapNotNull { it.toOnlineBook() }
            .distinctBy { it.title.lowercase() to it.authors.lowercase() }
    }
}