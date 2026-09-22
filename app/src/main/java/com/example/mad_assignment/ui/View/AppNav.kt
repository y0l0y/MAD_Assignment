package com.example.mad_assignment.ui.View

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mad_assignment.ViewModel.BookViewModel
import kotlinx.serialization.Serializable

@Serializable
data object BookList
@Serializable
data object AddBook

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
    }
}