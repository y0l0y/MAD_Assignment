package com.example.mad_assignment.ui.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.other.OnlineBook
import com.example.mad_assignment.viewModel.BookViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

data class BookDraft(
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val categoryId: String? = null,
    val coverUrl: String? = null,
    val coverUri: Uri? = null
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookContent(
    categories: List<CategoryEntity>,
    prefill: BookDraft,
    onSaveBook: (BookDraft) -> String?,
    onLookupIsbn: (String, (OnlineBook) -> Unit, (String) -> Unit) -> Unit,
    onAddCategory: (String) -> String?,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf(prefill.title) }
    var author by rememberSaveable { mutableStateOf(prefill.author) }
    var isbn by rememberSaveable { mutableStateOf(prefill.isbn) }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var coverUrl by rememberSaveable { mutableStateOf<String?>(prefill.coverUrl) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var isLookingUp by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    fun scanAndFill() {
        scanIsbn(
            context = context,
            onScanned = { code ->
                isbn = code
                isLookingUp = true
                onLookupIsbn(
                    code,
                    { found ->
                        title = found.title
                        author = found.authors
                        coverUrl = found.coverUrl
                        isLookingUp = false
                    },
                    { message ->
                        errorMessage = message
                        isLookingUp = false
                    }
                )
            },
            onError = { message -> errorMessage = message }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Book") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val error = onSaveBook(
                        BookDraft(
                            title = title,
                            author = author,
                            isbn = isbn,
                            categoryId = categoryId,
                            coverUrl = coverUrl,
                            coverUri = imageUri
                        )
                    )
                    if (error == null) onNavigateBack() else errorMessage = error
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .imePadding()
            ) {
                Text("Save Book")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    onClick = {
                        photoPickerLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val preview: Any? = imageUri ?: coverUrl
                        if (preview != null) {
                            AsyncImage(
                                model = preview,
                                contentDescription = "Selected Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = "Tap to add book cover",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = { scanAndFill() },
                    enabled = !isLookingUp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isLookingUp)
                            "Looking up book details..."
                        else
                            "Scan ISBN barcode"
                    )
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
                CategoryPicker(
                    categories = categories,
                    selectedId = categoryId,
                    onSelected = { categoryId = it },
                    onAddCategory = onAddCategory,
                    onDeleteCategory = onDeleteCategory,
                    modifier = Modifier.fillMaxWidth()
                )
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
fun AddBookScreen(
    vm: BookViewModel,
    prefill: BookDraft = BookDraft(),
    onNavigateBack: () -> Unit
){
    AddBookContent(
        categories = vm.categories,
        prefill = prefill,
        onSaveBook = { d ->
            vm.addBook(
                title = d.title,
                author = d.author,
                isbn = d.isbn,
                imageUri = d.coverUri,
                categoryId = d.categoryId,
                coverUrl = d.coverUrl
            )
        },
        onLookupIsbn = { isbn, onFound, onFail ->
            vm.lookupIsbn(
                isbn = isbn,
                onFound = onFound,
                onFail = onFail
            ) },
        onAddCategory = { vm.addCategory(it) },
        onDeleteCategory = { vm.deleteCategory(it) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun ErrorPopup(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(3000.milliseconds)
            onDismiss()
        }
    }
    if (message != null) {
        Card(
            modifier = modifier.widthIn(max = 260.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}


