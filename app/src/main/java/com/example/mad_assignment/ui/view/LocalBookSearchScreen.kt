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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.other.SortOrder
import com.example.mad_assignment.ui.components.EmptyState
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioCard
import com.example.mad_assignment.ui.components.FolioChip
import com.example.mad_assignment.ui.components.FolioChipRow
import com.example.mad_assignment.ui.components.FolioFieldShape
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.StatusChip
import com.example.mad_assignment.ui.components.Tone
import com.example.mad_assignment.ui.components.folioFieldColors
import com.example.mad_assignment.viewModel.BookViewModel

@Composable
fun LocalBookSearchScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val booksToDisplay = vm.visibleBook
    val categories = vm.categories

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FolioBackBar(title = "Local Book Search", onBack = onNavigateBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = vm.searchQuery,
                onValueChange = { vm.updateSearchQuery(it) },
                label = { Text("Search by title, author, or ISBN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = FolioFieldShape,
                colors = folioFieldColors(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (vm.searchQuery.isNotEmpty() || vm.selectedCategoryId != null) {
                        IconButton(onClick = { vm.clearFilter() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search and filters"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            FolioChipRow(modifier = Modifier.fillMaxWidth()) {
                FolioChip(
                    label = "Title",
                    selected = vm.sortOrder == SortOrder.TITLE,
                    onClick = { vm.updateSortOrder(SortOrder.TITLE) }
                )
                FolioChip(
                    label = "Author",
                    selected = vm.sortOrder == SortOrder.AUTHOR,
                    onClick = { vm.updateSortOrder(SortOrder.AUTHOR) }
                )
                FolioChip(
                    label = "Available First",
                    selected = vm.sortOrder == SortOrder.AVAILABLE_FIRST,
                    onClick = { vm.updateSortOrder(SortOrder.AVAILABLE_FIRST) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FolioChipRow(modifier = Modifier.fillMaxWidth()) {
                FolioChip(
                    label = "All Categories",
                    selected = vm.selectedCategoryId == null,
                    onClick = { vm.updateCategoryFilter(null) }
                )
                categories.forEach { category ->
                    FolioChip(
                        label = category.name,
                        selected = vm.selectedCategoryId == category.id,
                        onClick = { vm.updateCategoryFilter(category.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (booksToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = if (vm.searchQuery.isBlank() && vm.selectedCategoryId == null)
                            "No books available."
                        else "No matching books found."
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(booksToDisplay, key = { it.book.id }) { item ->
                        FolioCard(contentPadding = 12.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BookCover(
                                    coverBase64 = item.book.coverBase64,
                                    coverUrl = item.book.coverUrl,
                                    title = item.book.title,
                                    author = item.book.author,
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
                                        text = item.book.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Muted(item.book.author)
                                    Muted(
                                        text = "ISBN: ${item.book.isbn}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (item.availabilityKnown) {
                                        Spacer(Modifier.height(6.dp))
                                        if (item.isAvailable) StatusChip("Available", Tone.Good)
                                        else StatusChip("On loan", Tone.Neutral)
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