package com.example.mad_assignment.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.model.entity.OfflineBookEntity
import com.example.mad_assignment.viewModel.BookViewModel

@Composable
fun MyBookScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    MyBookContent(
        loans = vm.myLoans.mapNotNull { item -> item.activeLoan?.let { item.book to it } },
        offlineBooks = vm.offlineBook,
        onReturn = { vm.requestReturn(it) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookContent(
    loans: List<Pair<BookEntity, LoanEntity>>,
    offlineBooks: List<OfflineBookEntity>,
    onReturn: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Books") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Not returned") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Saved Offline") }
                )
            }
            if (selectedTab == 0) {
                BorrowedBookList(
                    loans = loans,
                    onReturn = onReturn
                )
            } else {
                OfflineBookList(books = offlineBooks)
            }
        }
    }
}

@Composable
private fun BorrowedBookList(
    loans: List<Pair<BookEntity, LoanEntity>>,
    onReturn: (String) -> Unit
) {
    if (loans.isEmpty()) {
        EmptyMessage("You have no unreturned books.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(loans, key = { it.second.id }) { (book, loan) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookCover(
                        coverBase64 = book.coverBase64,
                        coverUrl = book.coverUrl,
                        title = book.title,
                        modifier = Modifier.size(60.dp, 80.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Author: ${book.author}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Borrowed: ${formatDay(loan.borrowedAt)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        DueDateText(dueAt = loan.dueAt)
                    }
                    if (loan.returnRequestedAt != null) {
                        Text(
                            text = "Return requested",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        TextButton(onClick = { onReturn(book.id) }) {
                            Text("Request return")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineBookList(books: List<OfflineBookEntity>) {
    if (books.isEmpty()) {
        EmptyMessage("No books saved yet. Open the library once while online.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(books, key = { it.id }) { book ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookCover(
                        coverBase64 = book.coverBase64,
                        coverUrl = book.coverUrl,
                        title = book.title,
                        modifier = Modifier.size(60.dp, 80.dp)
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Author: ${book.author}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "ISBN: ${book.isbn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

