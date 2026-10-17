package com.example.mad_assignment.ui.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Title
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioButton
import com.example.mad_assignment.ui.components.FolioFieldShape
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.folioFieldColors
import com.example.mad_assignment.ui.components.isWideScreen
import com.example.mad_assignment.viewModel.BookViewModel

@Composable
fun BookUpdateScreen(
    vm: BookViewModel,
    bookID: String,
    onNavigateBack: () -> Unit
) {
    val book = vm.books.find { it.book.id == bookID }?.book
    var editingField by rememberSaveable { mutableStateOf<String?>(null) }
    var fieldValue by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null)}

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && book != null)
            vm.updateBookCover(book, uri)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FolioBackBar(title = "Update Book", onBack = onNavigateBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (book == null) {
                Text(
                    text = "Book not found or has been deleted.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                val wide = isWideScreen()
                /* category + change buttons, or the edit field while editing */
                val actions: @Composable ColumnScope.() -> Unit = {
                    if (editingField == null) {
                        CategoryPicker(
                            categories = vm.categories,
                            selectedId = book.categoryId,
                            onSelected = { vm.updateBookCategory(book, it) },
                            onAddCategory = { vm.addCategory(it) },
                            onDeleteCategory = { vm.deleteCategory(it) },
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
                                    "title" -> vm.updateBookTitle(book, fieldValue)
                                    "author" -> vm.updateBookAuthor(book, fieldValue)
                                    "isbn" -> vm.updateBookIsbn(book, fieldValue)
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
                if (wide) {
                    /* landscape / tablet: book on the left, what to change on the right */
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .widthIn(max = 1000.dp)
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BookUpdateInfoCard(book)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            content = actions
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        BookUpdateInfoCard(book)
                        actions()
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
    /* errors from changes that have no form field to show them (cover, category, offline) */
    vm.borrowErrorMessage?.let { message ->
        BookBorrowFailedDialog(
            message = message,
            onDismiss = { vm.clearBorrowError() }
        )
    }
}

/* cover on a sand tile, then title, author and ISBN */
@Composable
fun BookUpdateInfoCard(book: BookEntity) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            BookCover(
                coverBase64 = book.coverBase64,
                coverUrl = book.coverUrl,
                title = book.title,
                author = book.author,
                modifier = Modifier
                    .width(170.dp)
                    .aspectRatio(0.72f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(book.title, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(4.dp))
        Muted("by ${book.author}", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        Muted("ISBN: ${book.isbn}")
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
        text = "Select what you want to change",
        style = MaterialTheme.typography.titleLarge
    )
    FolioButton(
        text = "Change Book Cover",
        onClick = onChangeCover,
        icon = Icons.Outlined.Image,
        filled = false
    )
    FolioButton(
        text = "Change Book Title",
        onClick = onChangeTitle,
        icon = Icons.Outlined.Title,
        filled = false
    )
    FolioButton(
        text = "Change Book Author",
        onClick = onChangeAuthor,
        icon = Icons.Outlined.Person,
        filled = false
    )
    FolioButton(
        text = "Change Book ISBN",
        onClick = onChangeIsbn,
        icon = Icons.Outlined.QrCode,
        filled = false
    )
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
    Eyebrow("Editing")
    Text(
        text = label,
        style = MaterialTheme.typography.titleLarge
    )
    OutlinedTextField(
        value = fieldValue,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = FolioFieldShape,
        colors = folioFieldColors(),
        singleLine = true
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FolioButton(
            text = "Cancel",
            onClick = onCancel,
            filled = false,
            modifier = Modifier.weight(1f)
        )
        FolioButton(
            text = "Save",
            onClick = onSave,
            modifier = Modifier.weight(1f)
        )
    }
}