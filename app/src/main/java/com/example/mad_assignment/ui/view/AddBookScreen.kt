package com.example.mad_assignment.ui.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.other.OnlineBook
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioButton
import com.example.mad_assignment.ui.components.FolioFieldShape
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.folioFieldColors
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
    var coverUrl by rememberSaveable { mutableStateOf(prefill.coverUrl) }
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FolioBackBar(title = "Add Book", onBack = onNavigateBack) },
        bottomBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                FolioButton(
                    text = "Save Book",
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
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp)
                )
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Eyebrow("New arrival")
                    Spacer(Modifier.height(8.dp))
                    Text("Add to the shelf", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(4.dp))
                    Muted(
                        text = "Scan the barcode or fill in the details.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                val coverShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(coverShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, coverShape)
                        .clickable { photoPickerLauncher.launch("image/*") },
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Tap to add book cover",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                FolioButton(
                    text = if (isLookingUp) "Looking up book details..." else "Scan ISBN barcode",
                    onClick = { scanAndFill() },
                    icon = Icons.Outlined.QrCodeScanner,
                    filled = false,
                    enabled = !isLookingUp
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FolioFieldShape,
                    colors = folioFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FolioFieldShape,
                    colors = folioFieldColors(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FolioFieldShape,
                    colors = folioFieldColors(),
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
    /* errors from changes that have no form field to show them (e.g. deleting a category offline) */
    vm.borrowErrorMessage?.let { message ->
        BookBorrowFailedDialog(
            message = message,
            onDismiss = { vm.clearBorrowError() }
        )
    }
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
        Box(
            modifier = modifier
                .widthIn(max = 260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
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