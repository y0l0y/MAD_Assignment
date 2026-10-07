package com.example.mad_assignment.viewModel

import android.net.Uri
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookWithLoans
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.model.other.BookQuery
import com.example.mad_assignment.model.other.BookRepository
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.model.other.CoverStorage
import com.example.mad_assignment.model.other.OnlineBook
import com.example.mad_assignment.model.other.SortOrder
import com.example.mad_assignment.model.other.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
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

    /* all books and each with its loans */
    var books by mutableStateOf(listOf<BookWithLoans>())
        private set
    var categories by mutableStateOf(listOf<CategoryEntity>())
        private set

    /* local search / filter/ sort state */
    var searchQuery by mutableStateOf("")
        private set
    var selectedCategoryId by mutableStateOf<Long?>(null) // null = all categories
        private set
    var sortOrder by mutableStateOf(SortOrder.TITLE)
        private set

    val visibleBook: List<BookWithLoans> by derivedStateOf {
        BookQuery.apply(books, searchQuery, selectedCategoryId, sortOrder)
    }
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
            repository.categories.collectLatest { categories = it }
        }
        viewModelScope.launch {
            preferencesRepo.darkModeFlow.collectLatest { isDarkMode = it }
        }
        viewModelScope.launch {
            preferencesRepo.sortOrderFlow.collectLatest { sortOrder = it }
        }
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
    }

    fun toggleDarkMode() = viewModelScope.launch {
        preferencesRepo.saveDarkMode(!isDarkMode)
    }

    fun updateRole(){
        isAdminMode = !isAdminMode
    }

    /* local search / filter / sort */
    fun updateSearchQuery(query: String) { searchQuery = query }
    fun updateCategoryFilter(categoryId: Long?) { selectedCategoryId = categoryId}
    fun updateSortOrder(order: SortOrder) {
        sortOrder = order
        viewModelScope.launch { preferencesRepo.saveSortOrder(order) }
    }
    fun clearFilter() {
        searchQuery = ""
        selectedCategoryId = null
    }

    /* categories */
    private fun duplicateCategoryError(name: String): String? {
        val exists = categories.any {
            it.name.equals(
                name.trim(),
                ignoreCase = true
            )
        }
        return if (exists) "This category already exists" else null
    }
    fun addCategory(name: String): String? {
        val error = BookRules.validateCategoryName(name)
            ?: duplicateCategoryError(name)
        if (error != null) return error
        viewModelScope.launch { repository.addCategory(name.trim()) }
        return null
    }
    fun deleteCategory(category: CategoryEntity) = viewModelScope.launch {
        if (selectedCategoryId == category.id)
            selectedCategoryId = null
        repository.deleteCategory(category)
    }
    fun updateBookCategory(
        book: BookEntity,
        categoryId: Long?
    ) = viewModelScope.launch {
        repository.updateBook(book.copy(categoryId = categoryId))
    }

    private fun duplicateIsbnError(
        isbn: String,
        ignoreId: Long? = null
    ): String? {
        val clean = BookRules.normaliseIsbn(isbn)
        val exists = books.any {
            it.book.id != ignoreId && BookRules.normaliseIsbn(it.book.isbn) == clean
        }
        return if (exists) "A book with this ISBN already exists" else null
    }

    fun addBook(
        title: String,
        author: String,
        isbn: String,
        imageUri: Uri?,
        categoryId: Long? = null
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
                    categoryId = categoryId,
                    coverPath = coverPath
                )
            )
        }
        return null
    }

    /* loans */

    fun borrowBook(
        bookId: Long,
        userName: String
    ): String? {
        BookRules.validateBorrowerName(userName)?.let { return it }
        viewModelScope.launch {
            repository.borrowBook(bookId, userName.trim())
        }
        return null
    }

    fun returnBook(bookId: Long) = viewModelScope.launch {
        repository.returnBook(bookId)
    }

    fun loanHistory(bookId: Long): Flow<List<LoanEntity>> =
        repository.getBookWithLoans(bookId).map { it?.loansNewestFirst.orEmpty() }

    fun bookWithLoans(bookId: Long): Flow<BookWithLoans?> =
        repository.getBookWithLoans(bookId)

    fun deleteBook(book: BookEntity) = viewModelScope.launch {
        repository.deleteBook(book)
        book.coverPath?.let { path ->
            withContext(Dispatchers.IO) {
                coverStorage.delete(path)
            }
        }
    }

    fun updateBookCover(
         book: BookEntity,
         uri: Uri,
         onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val newPath = withContext(Dispatchers.IO) { coverStorage.copy(uri) }
            if (newPath == null) {
                onError("Unable to load the picture")
                return@launch
            }
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

    /* Online Search */
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