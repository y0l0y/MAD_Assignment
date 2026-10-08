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
import com.example.mad_assignment.model.entity.BorrowWithBook
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.OfflineBookEntity
import com.example.mad_assignment.model.other.BookQuery
import com.example.mad_assignment.model.other.BookRepository
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.model.other.CoverStorage
import com.example.mad_assignment.model.other.OnlineBook
import com.example.mad_assignment.model.other.SortOrder
import com.example.mad_assignment.model.other.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

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
    private companion object {
        const val WRITE_TIMEOUT_MS = 10_000L
        const val CONNECTION_MESSAGE = "Unable to reach the server. Please check you connection and try again."
    }
    var uiState by mutableStateOf<BookUiState>(BookUiState.Idle)
        private set

    /* all books and each with its loans */
    var books by mutableStateOf(listOf<BookWithLoans>())
        private set
    var categories by mutableStateOf(listOf<CategoryEntity>())
        private set

    /* local cache */
    var offlineBook by mutableStateOf(listOf<OfflineBookEntity>())
        private set
    var myBorrows by mutableStateOf(listOf<BorrowWithBook>())
        private set

    /* loading / error state */
    var isLoading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set

    /* local search / filter/ sort state */
    var searchQuery by mutableStateOf("")
        private set
    var selectedCategoryId by mutableStateOf<String?>(null) // null = all categories
        private set
    var sortOrder by mutableStateOf(SortOrder.TITLE)
        private set

    val visibleBook: List<BookWithLoans> by derivedStateOf {
        BookQuery.apply(books, searchQuery, selectedCategoryId, sortOrder)
    }
    /* the main list: all books */
    val sortedBooks: List<BookWithLoans> by derivedStateOf {
        BookQuery.apply(books, "", null, sortOrder)
    }

    var isAdminMode by mutableStateOf(false)
        private set
    var isDarkMode by mutableStateOf(false)
        private set
    var currentQuery = ""
        private set
    var borrowErrorMessage by mutableStateOf<String?>(null)
        private set

    val currentUserId: String? get() = repository.currentUid()
    val suggestedBorrowerName: String get() = repository.currentUserName()
    private var loadJob: Job? = null

    init {
        startLoading()
        viewModelScope.launch {
            repository.offlineBook.collectLatest { offlineBook = it }
        }
        viewModelScope.launch {
            repository.myBorrows.collectLatest { myBorrows = it }
        }
        viewModelScope.launch {
            preferencesRepo.darkModeFlow.collectLatest { isDarkMode = it }
        }
        viewModelScope.launch {
            preferencesRepo.sortOrderFlow.collectLatest { sortOrder = it }
        }
    }

    /* start listening firebase */
    private fun startLoading() {
        loadJob?.cancel()
        loadError = null
        isLoading = true
        loadJob = viewModelScope.launch {
            try {
                repository.ensureSignedIn()
                launch {
                    try {
                        repository.categories.collectLatest { categories = it }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // the book list show the error
                    }
                }
                repository.books.collectLatest { items ->
                    books = items
                    isLoading = false
                    loadError = null
                    repository.cacheSnapshot(items)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                isLoading = false
                loadError = "Failed to load book. Please check your connection."
            }
        }
    }

    fun reload() = startLoading()

    private fun launchWrite(block: suspend () -> Unit): Job = viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            borrowErrorMessage = "Unable to save the change. Please check your connection and try again."
        }
    }

    fun clearBorrowError() {
        borrowErrorMessage = null
    }

    fun toggleDarkMode() = viewModelScope.launch {
        preferencesRepo.saveDarkMode(!isDarkMode)
    }

    fun updateRole(){
        isAdminMode = !isAdminMode
    }

    /* local search / filter / sort */
    fun updateSearchQuery(query: String) { searchQuery = query }
    fun updateCategoryFilter(categoryId: String?) { selectedCategoryId = categoryId}
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
        launchWrite { repository.addCategory(name.trim()) }
        return null
    }

    fun deleteCategory(category: CategoryEntity) = launchWrite {
        if (selectedCategoryId == category.id)
            selectedCategoryId = null
        repository.deleteCategory(category)
    }

    fun updateBookCategory(
        book: BookEntity,
        categoryId: String?
    ) = launchWrite {
        repository.updateBook(book.copy(categoryId = categoryId))
    }

    private fun duplicateIsbnError(
        isbn: String,
        ignoreId: String? = null
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
        categoryId: String? = null,
        coverUrl: String? = null
    ): String? {
        val error = BookRules.validateTitle(title)
            ?: BookRules.validateAuthor(author)
            ?: BookRules.validateIsbn(isbn)
            ?: duplicateIsbnError(isbn)
        if (error != null) {
            return error
        }
        launchWrite {
            val cover = imageUri?.let { uri ->
                withContext(Dispatchers.IO) { coverStorage.encode(uri) }
            }
            repository.insertBook(
                BookEntity(
                    title = title.trim(),
                    author = author.trim(),
                    isbn = isbn.trim(),
                    categoryId = categoryId,
                    coverBase64 = cover,
                    coverUrl = coverUrl
                )
            )
        }
        return null
    }

    /* loans */
    fun borrowBook(
        bookId: String,
        userName: String
    ): String? {
        BookRules.validateBorrowerName(userName)?.let { return it }
        viewModelScope.launch {
            try {
                val success = withTimeoutOrNull(WRITE_TIMEOUT_MS.milliseconds) {
                    repository.borrowBook(bookId, userName.trim())
                }
                when (success) {
                    true -> Unit
                    false -> borrowErrorMessage = "Sorry, this book has already been borrowed."
                    null -> borrowErrorMessage = CONNECTION_MESSAGE
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                borrowErrorMessage = CONNECTION_MESSAGE
            }
        }
        return null
    }

    fun returnBook(bookId: String) = viewModelScope.launch {
        val loan = books.find { it.book.id == bookId }?.activeLoan ?: return@launch
        if (!isAdminMode && loan.borrowerUid != currentUserId) {
            borrowErrorMessage = "Only the person who borrowed this book, or an admin, can return it."
            return@launch
        }
        try {
            val done = withTimeoutOrNull(WRITE_TIMEOUT_MS.milliseconds) { repository.returnBook(bookId) }
            if (done == null) borrowErrorMessage = CONNECTION_MESSAGE
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            borrowErrorMessage = CONNECTION_MESSAGE
        }
    }

    fun deleteBook(book: BookEntity) = launchWrite {
        repository.deleteBook(book)
    }

    fun updateBookCover(
         book: BookEntity,
         uri: Uri,
         onError: (String) -> Unit = {}
    ) {
        launchWrite {
            val encoded = withContext(Dispatchers.IO) { coverStorage.encode(uri) }
            if (encoded == null) {
                onError("Unable to load the picture")
                return@launchWrite
            }
            repository.updateBook(
                book.copy(
                    coverBase64 = encoded
                )
            )
        }
    }

    fun updateBookTitle(book: BookEntity, newTitle: String): String? {
        BookRules.validateTitle(newTitle)?.let { return it }
        launchWrite {
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
        launchWrite {
            repository.updateBook(
                book.copy(
                    isbn = newIsbn.trim()
                )
            )
        }
        return null
    }

    /* find the book details by ISBN */
    fun lookupIsbn(
        isbn: String,
        onFound: (OnlineBook) -> Unit,
        onFail: (String) -> Unit
    ) {
        val cleanIsbn = BookRules.normaliseIsbn(isbn)
        viewModelScope.launch {
            try {
                val book = repository.findByIsbn(cleanIsbn)
                if (book == null) {
                    onFail("No book found online for the ISBN. Please type the details.")
                } else {
                    onFound(book)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                onFail("Failed to load book. Please check your connection.")
            }
        }
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
            } catch (e: CancellationException) {
                throw e
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