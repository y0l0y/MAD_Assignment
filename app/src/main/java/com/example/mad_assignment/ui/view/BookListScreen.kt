package com.example.mad_assignment.ui.view

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookWithLoans
import com.example.mad_assignment.viewModel.BookViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun BookListScreen(
    vm: BookViewModel,
    onNavigationToAdd: () -> Unit,
    onNavigateToApiSearch: () -> Unit,
    onNavigateToLocalSearch: () -> Unit,
    onItemClick: (BookEntity) -> Unit,
    onLoanShow: (BookEntity) -> Unit
){
    BookListContent(
        vm = vm,
        onNavigationToAdd = onNavigationToAdd,
        onNavigateToLocalSearch = onNavigateToLocalSearch,
        onNavigateToApiSearch = onNavigateToApiSearch,
        onItemClick = onItemClick,
        onLoanShow = onLoanShow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListContent(
    vm: BookViewModel,
    onNavigationToAdd: () -> Unit,
    onNavigateToLocalSearch: () -> Unit,
    onNavigateToApiSearch: () -> Unit,
    onItemClick: (BookEntity) -> Unit,
    onLoanShow: (BookEntity) -> Unit
) {
    var borrowBookId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteBookId by rememberSaveable { mutableStateOf<Long?>(null) }
    var borrowerName by rememberSaveable { mutableStateOf("") }
    val books = vm.books
    val bookToBorrow = books.find { it.book.id == borrowBookId }
    val bookToDelete = books.find { it.book.id == deleteBookId }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BookListDrawerContent(
                isDarkMode = vm.isDarkMode,
                isAdminMode = vm.isAdminMode,
                onNavigateToLocalSearch = onNavigateToLocalSearch,
                onNavigateToApiSearch = onNavigateToApiSearch,
                onToggleDarkMode = { vm.toggleDarkMode() },
                onUpdateRole = { vm.updateRole() },
                onClose = { scope.launch { drawerState.close() }}
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Club Book Library") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Menu"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (vm.isAdminMode) {
                    FloatingActionButton(onClick = onNavigationToAdd) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Book"
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (books.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No book available. Please contact admin to add the book.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(books, key = { it.book.id }) { item ->
                            BookItemCard(
                                item = item,
                                isAdminMode = vm.isAdminMode,
                                onItemClick = { onItemClick(item.book) },
                                onBorrowClick = { borrowBookId = item.book.id },
                                onReturnClick = { vm.returnBook(item.book.id) },
                                onDelete = { deleteBookId = item.book.id },
                                onLoanShow = { onLoanShow(item.book) }
                            )
                        }
                    }
                }
            }
        }
        if (bookToBorrow != null) {
            BookBorrowDialog(
                bookTitle = bookToBorrow.book.title,
                borrowerName = borrowerName,
                onNameChange = { borrowerName = it },
                onConfirm = {
                    if (vm.borrowBook(bookToBorrow.book.id, borrowerName) == null) {
                        borrowerName = ""
                        borrowBookId = null
                    }
                },
                onDismiss = { borrowBookId = null }
            )
        }
        if (bookToDelete != null) {
            BookDeleteDialog(
                bookTitle = bookToDelete.book.title,
                onConfirm = {
                    vm.deleteBook(bookToDelete.book)
                    deleteBookId = null
                },
                onDismiss = { deleteBookId = null }
            )
        }
    }
}

@Composable
fun BookItemCard(
    item: BookWithLoans,
    isAdminMode: Boolean,
    onItemClick: () -> Unit,
    onBorrowClick: () -> Unit,
    onReturnClick: () -> Unit,
    onDelete: () -> Unit,
    onLoanShow: () -> Unit
){
    val book = item.book
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (isAdminMode)
                    it.clickable(onClick = onLoanShow)
                else
                    it
                 },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (book.coverPath != null) {
                AsyncImage(
                    model = File(book.coverPath),
                    contentDescription = book.title,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(60.dp, 80.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(60.dp, 80.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "No book cover",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Author: ${book.author}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "ISBN: ${book.isbn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (item.isAvailable) {
                    Text(
                        text = "Status: Available",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Status: Borrowed (By: ${item.activeLoan?.borrowerName ?: "Unknown"})",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (isAdminMode) {
                BookActionMenu(
                    onEdit = onItemClick,
                    onDelete = onDelete
                )
            } else {
                if (item.isAvailable) {
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

@Composable
fun BookListDrawerContent(
    isDarkMode: Boolean,
    isAdminMode: Boolean,
    onNavigateToLocalSearch: () -> Unit,
    onNavigateToApiSearch: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onUpdateRole: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        NavigationDrawerItem(
            label = { Text("Search Book Local") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Local"
                )
            },
            selected = false,
            onClick = {
                onNavigateToLocalSearch()
                onClose()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Search Book Online") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Search Online"
                )
            },
            selected = false,
            onClick = {
                onNavigateToApiSearch()
                onClose()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text(if (isDarkMode) "Light Mode" else "Dark Mode") },
            icon = {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.Brightness6 else Icons.Default.Brightness4,
                    contentDescription = "Switch Theme"
                )
            },
            selected = false,
            onClick = {
                onToggleDarkMode()
                onClose()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text(if (isAdminMode) "User Mode" else "Admin Mode") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Switch Role",
                    tint = if (isAdminMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            selected = isAdminMode,
            onClick = {
                onUpdateRole()
                onClose()
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun BookBorrowDialog(
    bookTitle: String,
    borrowerName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Borrow Book") },
        text = {
            Column {
                Text("Enter your name to borrow \"$bookTitle\":")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = borrowerName,
                    onValueChange = onNameChange,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookDeleteDialog(
    bookTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Book") },
        text = { Text("Are you sure you want to delete \"${bookTitle}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BookActionMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Action"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Book",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Book",
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}

