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

    init {
        refresh()
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
            isAvailable = true
        )
        bookDao.insertBook(newBook); refresh()
    }
    fun updateBook(book: BookEntity) = viewModelScope.launch {
        bookDao.updateBook(book.copy(isAvailable = !book.isAvailable)); refresh()
    }

    fun deleteBook(book: BookEntity) = viewModelScope.launch {
        bookDao.deleteBook(book); refresh()
    }
}