package com.example.mad_assignment.ui.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mad_assignment.ViewModel.BookUiState
import com.example.mad_assignment.ViewModel.BookViewModel

@Composable
fun ApiBookSearchScreen(viewModel: BookViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf(viewModel.currentQuery) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search API Books") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                Button(onClick = { viewModel.searchBooks(searchQuery) }) {
                    Text("Search")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (val state = uiState) {
            is BookUiState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Enter a search term to query Open Library.")
                }
            }
            is BookUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BookUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No books found from API.")
                }
            }
            is BookUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is BookUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.books) { book ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(text = "Author: ${book.authorName?.joinToString() ?: "Unknown"}")
                            }
                        }
                    }
                }
            }
        }
    }
}
