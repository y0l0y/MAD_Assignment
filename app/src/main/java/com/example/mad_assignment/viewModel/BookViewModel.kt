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
import com.example.mad_assignment.model.entity.Roles
import com.example.mad_assignment.model.entity.UserProfile
import com.example.mad_assignment.model.other.BookQuery
import com.example.mad_assignment.model.other.BookRepository
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.model.other.CoverStorage
import com.example.mad_assignment.model.other.LoanFilter
import com.example.mad_assignment.model.other.LoanQuery
import com.example.mad_assignment.model.other.LoanRow
import com.example.mad_assignment.model.other.NetworkMonitor
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
    private val coverStorage: CoverStorage,
    private val network: NetworkMonitor
) : ViewModel() {
    private companion object {
        const val WRITE_TIMEOUT_MS = 10_000L
        const val SYNC_TIMEOUT_MS = 15_000L
        const val OFFLINE_MESSAGE = "You are offline. Borrowing and returning need an internet connection."
        const val CONNECTION_MESSAGE = "Unable to reach the server. Please check your connection and try again."
    }
    var uiState by mutableStateOf<BookUiState>(BookUiState.Idle)
        private set

    /* all books and each with its loans */
    var books by mutableStateOf(listOf<BookWithLoans>())
        private set
    var categories by mutableStateOf(listOf<CategoryEntity>())
        private set

    /* loading / error state */
    var isLoading by mutableStateOf(true)
        private set
    var loadError by mutableStateOf<String?>(null)
        private set

    /* loans this user may read: admin = all, student = own */
    var allLoans by mutableStateOf(listOf<LoanEntity>())
        private set

    /* admin loan list: which filter chip is selected (kept here so it survives rotation) */
    var loanFilter by mutableStateOf(LoanFilter.ALL)
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

    /* account: null uid = guest */
    var currentUserId by mutableStateOf<String?>(repository.currentUid())
        private set
    var profile by mutableStateOf<UserProfile?>(null)
        private set
    val isAdminMode: Boolean get() = profile?.role == Roles.ADMIN
    var authBusy by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set
    private var signUpInProgress = false
    var isOnline by mutableStateOf(true)
        private set
    var isDarkMode by mutableStateOf(false)
        private set
    var currentQuery = ""
        private set
    var borrowErrorMessage by mutableStateOf<String?>(null)
        private set

    private var loadJob: Job? = null

    init {
        startLoading()
        viewModelScope.launch {
            repository.authState.collectLatest { uid ->
                currentUserId = uid
                profile = null
                /* sign up loads the profile itself once the document is written */
                if (uid != null && !signUpInProgress) {
                    profile = loadProfileOrNull()
                }
                /* the loans this user may read depend on who is signed in and the role */
                startLoading()
                syncCatalogQuietly()
            }
        }
        /* show the bundled book list straight away, even offline */
        viewModelScope.launch {
            try {
                repository.ensureSeeded()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // the list stays empty until the next start
            }
        }
        viewModelScope.launch {
            network.isOnline.collectLatest { online ->
                val wasOffline = !isOnline
                isOnline = online
                if (online && wasOffline) syncCatalogQuietly()
            }
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
                launch {
                    try {
                        repository.categories.collectLatest { categories = it }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // the book list show the error
                    }
                }
                launch {
                    try {
                        repository.loans(currentUserId, isAdminMode).collectLatest {
                            allLoans = it.sortedByDescending { loan -> loan.borrowedAt }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // the book list shows the error
                    }
                }
                repository.books(currentUserId, isAdminMode).collectLatest { items ->
                    books = items
                    isLoading = false
                    loadError = null
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

    /* check the online catalog version (admin: first upload); offline keeps the local list */
    private suspend fun syncCatalogQuietly() {
        try {
            withTimeoutOrNull(SYNC_TIMEOUT_MS.milliseconds) {
                repository.syncCatalog(isAdmin = isAdminMode)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // offline or no access: keep the local catalog
        }
    }

    /* same feature with viewModelScope.launch just add a try and catch to handle error */
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

    /* authentication */
    private suspend fun loadProfileOrNull(): UserProfile? =
        try {
            repository.loadOrCreateProfile()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }

    private fun runAuth(
        onSuccess: () -> Unit,
        onFinally: () -> Unit = {},
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            authBusy = true
            authError = null
            try {
                block()
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                authError = e.message ?: "Something went wrong. Please try again."
            } finally {
                authBusy = false
                onFinally()
            }
        }
    }

    fun clearAuthError() {
        authError = null
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        val error = BookRules.validateEmail(email) ?: BookRules.validatePassword(password)
        if (error != null) {
            authError = error
            return
        }
        runAuth(onSuccess) { repository.signIn(email.trim(), password) }
    }

    fun signUp(name: String, email: String, password: String, onSuccess: () -> Unit) {
        val error = BookRules.validateBorrowerName(name)
            ?: BookRules.validateEmail(email)
            ?: BookRules.validatePassword(password)
        if (error != null) {
            authError = error
            return
        }
        signUpInProgress = true
        runAuth(onSuccess, onFinally = { signUpInProgress = false }) {
            repository.signUp(name.trim(), email.trim(), password)
            profile = repository.loadOrCreateProfile()
            startLoading()
        }
    }

    fun signOut() {
        repository.signOut()
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
    fun borrowBook(bookId: String) {
        if (currentUserId == null) {
            borrowErrorMessage = "Please sign in to borrow a book."
            return
        }
        if (!isOnline) {
            borrowErrorMessage = OFFLINE_MESSAGE
            return
        }
        val userName = profile?.name.orEmpty()
        BookRules.validateBorrowerName(userName)?.let {
            borrowErrorMessage = it
            return
        }
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
    }

    /* admin: loans whose return was requested, oldest request first */
    val pendingReturns: List<LoanEntity>
        get() = LoanQuery.pendingReturns(allLoans)

    /* admin: the loans to show for the selected filter, each with its status already worked out */
    val loanRows: List<LoanRow>
        get() = LoanQuery.rows(allLoans, loanFilter)

    /* admin: number shown on the Overdue chip (always counts every loan, not just the filtered ones) */
    val overdueLoanCount: Int
        get() = LoanQuery.overdueCount(allLoans)

    fun updateLoanFilter(filter: LoanFilter) { loanFilter = filter }

    /* books the signed-in user currently holds (online data only) */
    val myLoans: List<BookWithLoans>
        get() = currentUserId?.let { uid -> books.filter { it.activeLoan?.borrowerUid == uid } }
            ?: emptyList()

    /* borrower asks the admin to confirm the return */
    fun requestReturn(bookId: String) = viewModelScope.launch {
        if (!isOnline) {
            borrowErrorMessage = OFFLINE_MESSAGE
            return@launch
        }
        val loan = books.find { it.book.id == bookId }?.activeLoan ?: return@launch
        if (loan.borrowerUid != currentUserId) {
            borrowErrorMessage = "Only the person who borrowed this book can request its return."
            return@launch
        }
        try {
            val done = withTimeoutOrNull(WRITE_TIMEOUT_MS.milliseconds) {
                repository.requestReturn(bookId)
            }
            when (done) {
                true -> Unit
                false -> borrowErrorMessage = "A return has already been requested for this book."
                null -> borrowErrorMessage = CONNECTION_MESSAGE
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            borrowErrorMessage = CONNECTION_MESSAGE
        }
    }

    /* admin confirms a requested return */
    fun confirmReturn(bookId: String) = viewModelScope.launch {
        if (!isAdminMode) {
            borrowErrorMessage = "Only an admin can confirm a return."
            return@launch
        }
        if (!isOnline) {
            borrowErrorMessage = OFFLINE_MESSAGE
            return@launch
        }
        try {
            val done = withTimeoutOrNull(WRITE_TIMEOUT_MS.milliseconds) {
                repository.confirmReturn(bookId)
            }
            when (done) {
                true -> Unit
                false -> borrowErrorMessage = "No return has been requested for this book."
                null -> borrowErrorMessage = CONNECTION_MESSAGE
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            borrowErrorMessage = CONNECTION_MESSAGE
        }
    }

    fun deleteBook(book: BookEntity) = launchWrite {
        if (books.find { it.book.id == book.id }?.isAvailable == false) {
            borrowErrorMessage = "This book is borrowed (or its status is unknown) and cannot be deleted."
            return@launchWrite
        }
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
        launchWrite {
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