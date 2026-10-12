package com.example.mad_assignment.ui.view

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.other.SortOrder
import com.example.mad_assignment.viewModel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalBookSearchScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val booksToDisplay = vm.visibleBook
    val categories = vm.categories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Book Search") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = vm.searchQuery,
                onValueChange = { vm.updateSearchQuery(it) },
                label = { Text("Search by title, author, or ISBN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = vm.sortOrder == SortOrder.TITLE,
                    onClick = { vm.updateSortOrder(SortOrder.TITLE) },
                    label = { Text("Title") }
                )
                FilterChip(
                    selected = vm.sortOrder == SortOrder.AUTHOR,
                    onClick = { vm.updateSortOrder(SortOrder.AUTHOR) },
                    label = { Text("Author") }
                )
                FilterChip(
                    selected = vm.sortOrder == SortOrder.AVAILABLE_FIRST,
                    onClick = { vm.updateSortOrder(SortOrder.AVAILABLE_FIRST) },
                    label = { Text("Available First") }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = vm.selectedCategoryId == null,
                    onClick = { vm.updateCategoryFilter(null) },
                    label = { Text("All Categories") }
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = vm.selectedCategoryId == category.id,
                        onClick = { vm.updateCategoryFilter(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (booksToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (vm.searchQuery.isBlank() && vm.selectedCategoryId == null)
                            "No books available."
                        else "No matching books found.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(booksToDisplay, key = { it.book.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = item.book.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Author: ${item.book.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ISBN: ${item.book.isbn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}