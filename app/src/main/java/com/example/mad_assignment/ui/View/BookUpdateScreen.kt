package com.example.mad_assignment.ui.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.Model.BookEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookUpdateScreen(
    bookID: Long,
    books : List<BookEntity>,
    onUpdateTitle: (BookEntity, String) -> Unit,
    onUpdateAuthor: (BookEntity, String) -> Unit,
    onUpdateIsbn: (BookEntity, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val book = books.find { it.id == bookID }
    var editingField by remember { mutableStateOf<String?>(null) }
    var fieldValue by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Book") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (book == null) {
                Text(
                    text = "Book not found or has been deleted.",
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Current Title: ${book.title}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Current Author: ${book.author}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Current ISBN: ${book.isbn}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (editingField == null) {
                    Text(
                        text = "Select what you want to change: ",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            editingField = "title"
                            fieldValue = book.title
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Book Title")
                    }
                    Button(
                        onClick = {
                            editingField = "author"
                            fieldValue = book.author
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Book Author")
                    }
                    Button(
                        onClick = {
                            editingField = "isbn"
                            fieldValue = book.isbn
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Change Book ISBN")
                    }
                } else {
                    val label = when(editingField) {
                        "title" -> "New Title"
                        "author" -> "New Author"
                        "isbn" -> "New ISBN"
                        else -> "Something Wrong"
                    }
                    Text(
                        text = "Editing: $label",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = fieldValue,
                        onValueChange = { fieldValue = it },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingField = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (fieldValue.isNotBlank()) {
                                    when (editingField) {
                                        "title" -> onUpdateTitle(book, fieldValue.trim())
                                        "author" -> onUpdateAuthor(book, fieldValue.trim())
                                        "isbn" -> onUpdateIsbn(book, fieldValue.trim())
                                    }
                                    editingField = null
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }


}