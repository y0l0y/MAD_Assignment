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
data object AddBook
@Serializable
data class BookDetail(val bookID: Long)
@Serializable
data object LocalBookSearch
@Serializable
data object ApiBookSearch
@Serializable
data class LoanHistory(val bookID: Long)

@Composable
fun AppNav(viewModel: BookViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = BookList
    ){
        composable<BookList> {
            BookListScreen(
                vm = viewModel,
                onNavigationToAdd = {
                    navController.navigate(AddBook)
                },
                onNavigateToLocalSearch = {
                    navController.navigate(LocalBookSearch)
                },
                onNavigateToApiSearch = {
                    navController.navigate(ApiBookSearch)
                },
                onItemClick = { book ->
                    navController.navigate(BookDetail(bookID = book.id))
                },
                onLoanShow = { book ->
                    navController.navigate(LoanHistory(bookID = book.id))
                }
            )
        }
        composable<AddBook>{
            AddBookScreen(
                vm = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
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
                    navController.popBackStack()
                }
            )
        }
        composable<LocalBookSearch> {
            TODO()
        }
        composable<ApiBookSearch> {
            ApiBookSearchScreen(
                vm = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<LoanHistory> { backStackEntry ->
            val route = backStackEntry.toRoute<LoanHistory>()
            LoanHistoryScreen(
                bookID = route.bookID,
                vm = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}