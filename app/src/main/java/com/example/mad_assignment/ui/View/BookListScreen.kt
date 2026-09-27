package com.example.mad_assignment.ui.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.Model.BookEntity
import com.example.mad_assignment.ViewModel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListContent(
    books: List<BookEntity>,
    isAdminMode: Boolean,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onUpdateRole: () -> Unit,
    onNavigationToAdd: () -> Unit,
    onNavigateToLocalSearch: () -> Unit,
    onNavigateToApiSearch: () -> Unit,
    onItemClick: (BookEntity) -> Unit,
    onBorrowBook: (BookEntity, String) -> Unit,
    onReturnBook: (BookEntity) -> Unit,
    onDelete: (BookEntity) -> Unit
) {
    var showBorrowDialog by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    var borrowerName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Club Book Library") },
                actions = {
                    IconButton(onClick = onNavigateToLocalSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Books Local",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onNavigateToApiSearch) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Search Books Online",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.Brightness6 else Icons.Default.Brightness4,
                            contentDescription = "Toggle Dark Mode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onUpdateRole) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Update Role",
                            tint = if (isAdminMode) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdminMode) {
                FloatingActionButton(onClick = onNavigationToAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add Book")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (books.isEmpty()){
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = "No book available. Tap '+' to add.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(books) { book ->
                        BookItemCard(
                            book = book,
                            isAdminMode = isAdminMode,
                            onItemClick = { onItemClick(book) },
                            onBorrowClick = {
                                selectedBook = book
                                showBorrowDialog = true
                            },
                            onReturnClick = { onReturnBook(book) },
                            onDelete = { onDelete(book)}
                        )
                    }
                }
            }
        }
    }
    if (showBorrowDialog && selectedBook != null) {
        AlertDialog(
            onDismissRequest = { showBorrowDialog = false },
            title = { Text("Borrow Book") },
            text = {
                Column {
                    Text("Enter your name:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = borrowerName,
                        onValueChange = { borrowerName = it },
                        singleLine =  true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bookToBorrow = selectedBook
                        if (borrowerName.isNotBlank() && bookToBorrow != null) {
                            onBorrowBook(bookToBorrow, borrowerName.trim())
                            borrowerName = ""
                            showBorrowDialog = false
                            selectedBook = null
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBorrowDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BookListScreen(
    vm: BookViewModel,
    onNavigationToAdd: () -> Unit,
    onNavigateToApiSearch: () -> Unit,
    onNavigateToLocalSearch: () -> Unit,
    onItemClick: (BookEntity) -> Unit
){
    BookListContent(
        books = vm.books,
        isAdminMode = vm.isAdminMode,
        isDarkMode = vm.isDarkMode,
        onToggleDarkMode = { vm.toggleDarkMode() },
        onUpdateRole = { vm.updateRole() },
        onNavigationToAdd = onNavigationToAdd,
        onNavigateToLocalSearch = onNavigateToLocalSearch,
        onNavigateToApiSearch = onNavigateToApiSearch,
        onItemClick = onItemClick,
        onBorrowBook = { book, name -> vm.borrowBook(book, name)},
        onReturnBook = { book -> vm.returnBook(book) },
        onDelete = { book -> vm.deleteBook(book) }
    )
}

@Composable
fun BookItemCard(
    book: BookEntity,
    isAdminMode: Boolean,
    onItemClick: () -> Unit,
    onBorrowClick: () -> Unit,
    onReturnClick: () -> Unit,
    onDelete: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (isAdminMode) it.clickable(onClick = onItemClick) else it },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
                    .padding(16.dp)
            ) {
                Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Author: ${book.author}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "ISBN: ${book.isbn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (book.isAvailable) {
                    Text(
                        text = "Status: Available",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Status: Borrowed (By: ${book.borrowedBy ?: "Unknown"})",
                        color = Color(0xFFC62828),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (isAdminMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onItemClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Book",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete book",
                            tint = Color.Red
                        )
                    }
                }
            } else {
                if (book.isAvailable) {
                    TextButton(onClick = onBorrowClick) {
                        Text("Borrow")
                    }
                } else {
                    TextButton(onClick = onReturnClick) {
                        Text(
                            text = "Return",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookListScreenPreview() {
    val sampleBook = listOf(
        BookEntity(id = 1, title = "Kotlin", author = "bblabu", isbn = "987654321", isAvailable = true),
        BookEntity(id = 2, title = "Jetpack", author = "bblabu", isbn = "123456789", isAvailable = false, borrowedBy = "bblabu")
    )

    MaterialTheme{
        BookListContent(
            books = sampleBook,
            isAdminMode = false,
            isDarkMode = false,
            onToggleDarkMode = {},
            onUpdateRole = {},
            onNavigationToAdd = {},
            onNavigateToLocalSearch = {},
            onNavigateToApiSearch = {},
            onItemClick = {},
            onBorrowBook = { _,_ -> },
            onReturnBook = {},
            onDelete = {}
        )
    }
}