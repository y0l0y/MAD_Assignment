package com.example.mad_assignment.ui.view

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.mad_assignment.viewModel.BookViewModel
import kotlinx.serialization.Serializable

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
data object LocalBookSearch
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
                onNavigateToLocalSearch = {
                    navController.navigate(LocalBookSearch)
                },
                onNavigateToApiSearch = {
                    navController.navigate(ApiBookSearch)
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
            AddBookScreen(
                vm = viewModel,
                prefill = BookDraft(
                    title = route.title,
                    author = route.author,
                    isbn = route.isbn,
                    coverUrl = route.coverUrl.ifBlank { null }
                ),
                onNavigateBack = {
                    goBack()
                }
            )
        }
        composable<BookDetail> { backStackEntry ->
            val detailRoute = backStackEntry.toRoute<BookDetail>()
            BookUpdateScreen(
                bookID = detailRoute.bookID,
                books = viewModel.books.map { it.book },
                categories = viewModel.categories,
                onAddCategory = { viewModel.addCategory(it) },
                onDeleteCategory = { viewModel.deleteCategory(it) },
                onUpdateCategory = { book, categoryId ->
                    viewModel.updateBookCategory(book, categoryId)
                },
                onUpdateCover = { book, uri ->
                    viewModel.updateBookCover(book, uri)
                },
                onUpdateTitle = { book, newTitle ->
                    viewModel.updateBookTitle(book, newTitle)
                },
                onUpdateAuthor = { book, newAuthor ->
                    viewModel.updateBookAuthor(book, newAuthor)
                },
                onUpdateIsbn = { book, newIsbn ->
                    viewModel.updateBookIsbn(book, newIsbn)
                },
                onNavigateBack = {
                    goBack()
                }
            )
        }
        composable<LocalBookSearch> {
            TODO()
        }
        composable<ApiBookSearch> {
            ApiBookSearchScreen(
                vm = viewModel,
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
                onNavigateBack = {
                    goBack()
                }
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