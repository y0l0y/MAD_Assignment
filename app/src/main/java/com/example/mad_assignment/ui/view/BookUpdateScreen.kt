package com.example.mad_assignment.ui.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.other.decodeCover

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookUpdateScreen(
    bookID: String,
    books : List<BookEntity>,
    categories: List<CategoryEntity>,
    onAddCategory: (String) -> String?,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onUpdateCategory: (BookEntity, String?) -> Unit,
    onUpdateCover: (BookEntity, Uri) -> Unit,
    onUpdateTitle: (BookEntity, String) -> String?,
    onUpdateAuthor: (BookEntity, String) -> String?,
    onUpdateIsbn: (BookEntity, String) -> String?,
    onNavigateBack: () -> Unit
) {
    val book = books.find { it.id == bookID }
    var editingField by rememberSaveable { mutableStateOf<String?>(null) }
    var fieldValue by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null)}

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && book != null)
            onUpdateCover(book, uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Book") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (book == null) {
                    Text(
                        text = "Book not found or has been deleted.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    BookUpdateInfoCard(book)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (editingField == null) {
                        CategoryPicker(
                            categories = categories,
                            selectedId = book.categoryId,
                            onSelected = { onUpdateCategory(book, it) },
                            onAddCategory = onAddCategory,
                            onDeleteCategory = onDeleteCategory,
                            modifier = Modifier.fillMaxWidth()
                        )
                        BookUpdateActions(
                            onChangeCover = { coverLauncher.launch("image/*") },
                            onChangeTitle = {
                                editingField = "title"
                                fieldValue = book.title
                            },
                            onChangeAuthor = {
                                editingField = "author"
                                fieldValue = book.author
                            },
                            onChangeIsbn = {
                                editingField = "isbn"
                                fieldValue = book.isbn
                            }
                        )
                    } else {
                        BookFieldEditSection(
                            editingField = editingField!!,
                            fieldValue = fieldValue,
                            onValueChange = { fieldValue = it },
                            onCancel = {
                                editingField = null
                                errorMessage = null
                            },
                            onSave = {
                                val error = when (editingField) {
                                    "title" -> onUpdateTitle(book, fieldValue)
                                    "author" -> onUpdateAuthor(book, fieldValue)
                                    "isbn" -> onUpdateIsbn(book, fieldValue)
                                    else -> null
                                }
                                if (error == null) {
                                    editingField = null
                                    errorMessage = null
                                } else {
                                    errorMessage = error
                                }
                            }
                        )
                    }
                }
            }
            ErrorPopup(
                message = errorMessage,
                onDismiss = { errorMessage = null },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun BookUpdateInfoCard(book: BookEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val coverBitmap = remember(book.coverBase64) { decodeCover(book.coverBase64) }
            val coverImage: Any? = coverBitmap ?: book.coverUrl
            if (coverImage != null) {
                Text(
                    text = "Current Book Cover",
                    style = MaterialTheme.typography.bodyMedium
                )
                AsyncImage(
                    model = coverImage,
                    contentDescription = "Cover of ${book.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "No cover yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
}

@Composable
fun BookUpdateActions(
    onChangeCover: () -> Unit,
    onChangeTitle: () -> Unit,
    onChangeAuthor: () -> Unit,
    onChangeIsbn: () -> Unit
) {
    Text(
        text = "Select what you want to change: ",
        style = MaterialTheme.typography.titleMedium
    )
    Button(
        onClick = onChangeCover,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Change Book Cover")
    }
    Button(
        onClick = onChangeTitle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Change Book Title")
    }
    Button(
        onClick = onChangeAuthor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Change Book Author")
    }
    Button(
        onClick = onChangeIsbn,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Change Book ISBN")
    }
}

@Composable
fun BookFieldEditSection(
    editingField: String,
    fieldValue: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val label = when (editingField) {
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
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f)
        ) {
            Text("Save")
        }
    }
}
