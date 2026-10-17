package com.example.mad_assignment.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.ui.components.EmptyState
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioButton
import com.example.mad_assignment.ui.components.FolioCard
import com.example.mad_assignment.ui.components.FolioFieldShape
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.folioFieldColors
import com.example.mad_assignment.ui.components.isWideScreen
import com.example.mad_assignment.viewModel.BookUiState
import com.example.mad_assignment.viewModel.BookViewModel

@Composable
fun ApiBookSearchScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState = vm.uiState
    var searchQuery by rememberSaveable { mutableStateOf(vm.currentQuery) }
    val wide = isWideScreen()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FolioBackBar(title = "Search Online", onBack = onNavigateBack) }
    ) { paddingValues ->
        /* on a wide screen the whole page is centred and results sit two per row */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 1100.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search API Books") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = FolioFieldShape,
                colors = folioFieldColors(),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistRemove,
                                    contentDescription = "Clear search text"
                                )
                            }
                        }
                        Button(
                            onClick = { vm.searchBooks(searchQuery) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Search", style = MaterialTheme.typography.labelLarge)
                        }
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
                        EmptyState(
                            message = "Enter a search term to query Open Library.",
                            icon = Icons.Outlined.Search
                        )
                    }
                }

                is BookUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is BookUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(message = "No books found from API.")
                    }
                }

                is BookUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(16.dp))
                            FolioButton(
                                text = "Retry",
                                onClick = { vm.retry() },
                                filled = false,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }

                is BookUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (wide) 2 else 1),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.books) { book ->
                            FolioCard(contentPadding = 12.dp) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BookCover(
                                        coverBase64 = null,
                                        coverUrl = book.coverUrl,
                                        title = book.title,
                                        author = book.authors,
                                        modifier = Modifier
                                            .width(48.dp)
                                            .aspectRatio(0.72f)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 14.dp)
                                    ) {
                                        Text(
                                            text = book.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Muted(book.authors)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}