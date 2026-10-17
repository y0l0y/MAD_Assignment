package com.example.mad_assignment.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mad_assignment.model.other.OnlineBook
import com.example.mad_assignment.viewModel.BookViewModel
import kotlinx.serialization.Serializable

/* key used to hand a book picked on the online search screen back to the add-book screen */
private const val PICKED_BOOK_KEY = "pickedBook"

@Serializable
data object BookList
@Serializable
data class AddBook(
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val coverUrl: String = ""
)
@Serializable
data class BookDetail(val bookID: String)
@Serializable
data object ApiBookSearch
@Serializable
data class LoanHistory(val bookID: String)
@Serializable
data object MyBooks
@Serializable
data object Auth

@Composable
fun AppNav(viewModel: BookViewModel) {
    val navController = rememberNavController()
    val goBack: () -> Unit = {
        if (navController.previousBackStackEntry != null) navController.popBackStack()
    }
    NavHost(
        navController = navController,
        startDestination = BookList
    ){
        composable<BookList> {
            BookListScreen(
                vm = viewModel,
                onNavigationToAdd = {
                    navController.navigate(AddBook())
                },
                onNavigateToMyBooks = {
                    navController.navigate(MyBooks)
                },
                onNavigateToAuth = {
                    navController.navigate(Auth)
                },
                onItemClick = { book ->
                    navController.navigate(BookDetail(bookID = book.id))
                },
                onLoanShow = { book ->
                    navController.navigate(LoanHistory(bookID = book.id))
                }
            )
        }
        composable<AddBook>{ backStackEntry ->
            val route = backStackEntry.toRoute<AddBook>()
            val picked by backStackEntry.savedStateHandle
                .getStateFlow<ArrayList<String>?>(PICKED_BOOK_KEY, null)
                .collectAsState()
            AddBookScreen(
                vm = viewModel,
                prefill = BookDraft(
                    title = route.title,
                    author = route.author,
                    isbn = route.isbn,
                    coverUrl = route.coverUrl.ifBlank { null }
                ),
                pickedBook = picked?.let {
                    OnlineBook(
                        title = it[0],
                        authors = it[1],
                        coverUrl = it[2].ifBlank { null },
                        isbn = it[3].ifBlank { null }
                    )
                },
                onPickedBookConsumed = {
                    backStackEntry.savedStateHandle[PICKED_BOOK_KEY] = null
                },
                onSearchOnline = { navController.navigate(ApiBookSearch) },
                onNavigateBack = { goBack() }
            )
        }
        composable<BookDetail> { backStackEntry ->
            val detailRoute = backStackEntry.toRoute<BookDetail>()
            BookUpdateScreen(
                vm = viewModel,
                bookID = detailRoute.bookID,
                onNavigateBack = { goBack() }
            )
        }
        composable<ApiBookSearch> {
            ApiBookSearchScreen(
                vm = viewModel,
                onBookSelected = { book ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        PICKED_BOOK_KEY,
                        arrayListOf(book.title, book.authors, book.coverUrl.orEmpty(), book.isbn.orEmpty())
                    )
                    goBack()
                },
                onNavigateBack = { goBack() }
            )
        }
        composable<Auth> {
            AuthScreen(
                vm = viewModel,
                onNavigateBack = { goBack() }
            )
        }
        composable<MyBooks> {
            MyBookScreen(
                vm = viewModel,
                onNavigateBack = { goBack() }
            )
        }
        composable<LoanHistory> { backStackEntry ->
            val route = backStackEntry.toRoute<LoanHistory>()
            LoanHistoryScreen(
                bookID = route.bookID,
                vm = viewModel,
                onNavigateBack = { goBack() }
            )
        }
    }
}