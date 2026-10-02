package com.example.mad_assignment.ViewModel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.Model.BookEntity
import com.example.mad_assignment.Model.BookRepository
import com.example.mad_assignment.Model.BookRules
import com.example.mad_assignment.Model.CoverStorage
import com.example.mad_assignment.Model.OnlineBook
import com.example.mad_assignment.Model.TestData
import com.example.mad_assignment.Model.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class BookUiState {
    object Idle: BookUiState()
    object Loading: BookUiState()
    data class Success(val books: List<OnlineBook>) : BookUiState()
    object Empty: BookUiState()
    data class Error(val message: String): BookUiState()
}

class BookViewModel (
    private val repository: BookRepository,
    private val preferencesRepo: UserPreferencesRepository,
    private val coverStorage: CoverStorage
) : ViewModel() {
    var uiState by mutableStateOf<BookUiState>(BookUiState.Idle)
        private set
    var books by mutableStateOf(listOf<BookEntity>())
        private set
    var isAdminMode by mutableStateOf(false)
        private set
    var isDarkMode by mutableStateOf(false)
        private set
    var currentQuery = ""
        private set

    init {
        viewModelScope.launch {
            repository.books.collectLatest { books = it }
        }
        viewModelScope.launch {
            preferencesRepo.darkModeFlow.collectLatest { isDarkMode = it }
        }
        preloadTestData()
    }

    private fun preloadTestData() {
        viewModelScope.launch {
            val count = repository.getBooksCount()
            if (count == 0) {
                TestData.initialBooks.forEach { book ->
                    repository.insertBook(book)
                }
            }
        }
    }

    fun toggleDarkMode() = viewModelScope.launch {
        preferencesRepo.saveDarkMode(!isDarkMode)
    }

    fun updateRole(){
        isAdminMode = !isAdminMode
    }

    private fun duplicateIsbnError(
        isbn: String,
        ignoreId: Long? = null
    ): String? {
        val clean = BookRules.normaliseIsbn(isbn)
        val exists = books.any {
            it.id != ignoreId && BookRules.normaliseIsbn(it.isbn) == clean
        }
        return if (exists) "A book with this ISBN already exists" else null
    }

    fun addBook(
        title: String,
        author: String,
        isbn: String,
        imageUri: Uri?
    ): String? {
        val error = BookRules.validateTitle(title)
            ?: BookRules.validateAuthor(author)
            ?: BookRules.validateIsbn(isbn)
            ?: duplicateIsbnError(isbn)
        if (error != null) {
            return error
        }
        viewModelScope.launch {
            val coverPath = imageUri?.let { uri ->
                withContext(Dispatchers.IO) { coverStorage.copy(uri) }
            }
            repository.insertBook(
                BookEntity(
                    title = title.trim(),
                    author = author.trim(),
                    isbn = isbn.trim(),
                    coverPath = coverPath
                )
            )
        }
        return null
    }

    fun borrowBook(
        book: BookEntity,
        userName: String
    ) = viewModelScope.launch {
        if (book.isAvailable){
            repository.updateBook(
                book.copy(
                    isAvailable = false,
                    borrowedBy = userName
                )
            )
        }
    }

    fun returnBook(book: BookEntity) = viewModelScope.launch {
        repository.updateBook(
            book.copy(
                isAvailable = true,
                borrowedBy = null
            )
        )
    }

    fun deleteBook(book: BookEntity) = viewModelScope.launch {
        repository.deleteBook(book)
        book.coverPath?.let { path ->
            withContext(Dispatchers.IO) {
                coverStorage.delete(path)
            }
        }
    }

    fun updateBookCover(book: BookEntity, uri: Uri) {
        viewModelScope.launch {
            val newPath = withContext(Dispatchers.IO) { coverStorage.copy(uri) }
                ?: return@launch
            repository.updateBook(
                book.copy(
                    coverPath = newPath
                )
            )
            book.coverPath?.let { old ->
                withContext(Dispatchers.IO) { coverStorage.delete(old) }
            }
        }
    }

    fun updateBookTitle(book: BookEntity, newTitle: String): String? {
        BookRules.validateTitle(newTitle)?.let { return it }
        viewModelScope.launch {
            repository.updateBook(
                book.copy(
                    title = newTitle.trim()
                )
            )
        }
        return null
    }
    fun updateBookAuthor(book: BookEntity, newAuthor: String): String? {
        BookRules.validateAuthor(newAuthor)?.let { return it }
        viewModelScope.launch {
            repository.updateBook(
                book.copy(
                    author = newAuthor.trim()
                )
            )
        }
        return null
    }

    fun updateBookIsbn(book: BookEntity, newIsbn: String): String? {
        val error = BookRules.validateIsbn(newIsbn) ?: duplicateIsbnError(newIsbn, ignoreId = book.id)
        if (error != null) return error
        viewModelScope.launch {
            repository.updateBook(
                book.copy(
                    isbn = newIsbn.trim()
                )
            )
        }
        return null
    }

    fun searchBooks(query: String) {
        currentQuery = query
        if (query.isBlank()) {
            uiState = BookUiState.Idle
            return
        }

        viewModelScope.launch {
            uiState = BookUiState.Loading
            try {
                val result = repository.searchOnline(query.trim())
                uiState = if (result.isEmpty()) {
                    BookUiState.Empty
                } else {
                    BookUiState.Success(result)
                }
            } catch (_: Exception) {
                uiState = BookUiState.Error("Failed to load book")
            }
        }
    }

    fun retry() {
        if (currentQuery.isNotBlank()) {
            searchBooks(currentQuery)
        }
    }
}