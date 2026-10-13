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
import com.example.mad_assignment.model.entity.BorrowWithBook
import com.example.mad_assignment.model.entity.OfflineBookEntity
import com.example.mad_assignment.viewModel.BookViewModel

@Composable
fun MyBookScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    MyBookContent(
        borrows = vm.myBorrows,
        offlineBooks = vm.offlineBook,
        onReturn = { vm.returnBook(it) },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookContent(
    borrows: List<BorrowWithBook>,
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
                    borrows = borrows,
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
    borrows: List<BorrowWithBook>,
    onReturn: (String) -> Unit
) {
    if (borrows.isEmpty()) {
        EmptyMessage("You have no unreturned books.")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(borrows, key = { it.borrow.loanId }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookCover(
                        coverBase64 = item.book.coverBase64,
                        coverUrl = item.book.coverUrl,
                        title = item.book.title,
                        modifier = Modifier.size(60.dp, 80.dp)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = item.book.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Author: ${item.book.author}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Borrowed: ${formatDay(item.borrow.borrowedAt)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        DueDateText(dueAt = item.borrow.dueAt)
                    }
                    TextButton(onClick = { onReturn(item.book.id) }) {
                        Text("Return")
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

