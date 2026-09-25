package com.example.mad_assignment.ViewModel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.Model.AppDatabase
import com.example.mad_assignment.Model.BookEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookViewModel (application: Application) : AndroidViewModel(application) {
    private val bookDao = AppDatabase.getDatabase(application).bookDao()

    var books by mutableStateOf(listOf<BookEntity>())
        private set

    var isAdminMode by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    fun updateRole(){
        isAdminMode = !isAdminMode
    }

    fun refresh() = viewModelScope.launch {
        bookDao.getAllBooks().collectLatest { bookList ->
            books = bookList
        }
    }

    fun addBook(title: String, author: String, isbn: String) = viewModelScope.launch {
        val newBook = BookEntity(
            title = title,
            author = author,
            isbn = isbn,
            isAvailable = true,
            borrowedBy = null
        )
        bookDao.insertBook(newBook)
        refresh()
    }
    fun borrowBook(book: BookEntity, userName: String) = viewModelScope.launch {
        if (book.isAvailable){
            val updatedBook = book.copy(
                isAvailable = false,
                borrowedBy = userName
            )
            bookDao.updateBook(updatedBook)
            refresh()
        }
    }

    fun returnBook(book: BookEntity) = viewModelScope.launch {
        val updatedBook = book.copy(
            isAvailable = true,
            borrowedBy = null
        )
        bookDao.updateBook(updatedBook)
        refresh()
    }

    fun deleteBook(book: BookEntity) = viewModelScope.launch {
        bookDao.deleteBook(book)
        refresh()
    }

    fun updateBookTitle(book: BookEntity, newTitle: String) = viewModelScope.launch {
        val updatedBook = book.copy(
            title = newTitle
        )
        bookDao.updateBook(updatedBook)
        refresh()
    }

    fun updateBookAuthor(book: BookEntity, newAuthor: String) = viewModelScope.launch {
        val updatedBook = book.copy(
            author = newAuthor
        )
        bookDao.updateBook(updatedBook)
        refresh()
    }

    fun updateBookIsbn(book: BookEntity, newIsbn: String) = viewModelScope.launch {
        val updatedBook = book.copy(
            isbn = newIsbn
        )
        bookDao.updateBook(updatedBook)
        refresh()
    }
}