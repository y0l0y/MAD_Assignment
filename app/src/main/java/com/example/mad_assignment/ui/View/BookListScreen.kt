package com.example.mad_assignment.ui.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    onNavigationToAdd: () -> Unit,
    onUpdateAvailability: (BookEntity) -> Unit,
    onDelete: (BookEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Club Book Library") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigationToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
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
                            onUpdateAvailability = { onUpdateAvailability(book) },
                            onDelete = { onDelete(book)}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookListScreen(
    vm: BookViewModel,
    onNavigationToAdd: () -> Unit
){
    BookListContent(
        books = vm.books,
        onNavigationToAdd = onNavigationToAdd,
        onUpdateAvailability = {vm.updateBook(it)},
        onDelete = {vm.deleteBook(it)}
    )
}

@Composable
fun BookItemCard(
    book: BookEntity,
    onUpdateAvailability: () -> Unit,
    onDelete: () -> Unit
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(text = book.title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Author: ${book.author}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "ISBN: ${book.isbn}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if(book.isAvailable) "Status: Available" else "Status: Borrowed",
                color = if (book.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                style = MaterialTheme.typography.bodySmall
            )
            Row {
                TextButton(onClick = onUpdateAvailability) {
                    Text(if (book.isAvailable) "Borrow" else "Return")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Book", tint = Color.Red)
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
        BookEntity(id = 2, title = "Jetpack", author = "bblabu", isbn = "123456789", isAvailable = false)
    )

    MaterialTheme{
        BookListContent(
            books = sampleBook,
            onNavigationToAdd = {},
            onUpdateAvailability = {},
            onDelete = {}
        )
    }
}