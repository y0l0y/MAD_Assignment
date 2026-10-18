package com.example.mad_assignment.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookWithLoans
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.ui.components.Avatar
import com.example.mad_assignment.ui.components.CommitteeBadge
import com.example.mad_assignment.ui.components.EmptyState
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioButton
import com.example.mad_assignment.ui.components.FolioCard
import com.example.mad_assignment.ui.components.FolioChip
import com.example.mad_assignment.ui.components.FolioChipRow
import com.example.mad_assignment.ui.components.FolioFieldShape
import com.example.mad_assignment.ui.components.folioFieldColors
import com.example.mad_assignment.model.other.SortOrder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalConfiguration
import com.example.mad_assignment.ui.components.FolioHomeBar
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.StatusChip
import com.example.mad_assignment.ui.components.Tone
import com.example.mad_assignment.ui.components.initialsOf
import com.example.mad_assignment.ui.components.isWideScreen
import com.example.mad_assignment.ui.theme.FolioSerif
import com.example.mad_assignment.viewModel.BookViewModel
import kotlinx.coroutines.launch

@Composable
fun BookListScreen(
    vm: BookViewModel,
    onNavigationToAdd: () -> Unit,
    onNavigateToMyBooks: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onItemClick: (BookEntity) -> Unit,
    onLoanShow: (BookEntity) -> Unit,
    onNavigateToContact: () -> Unit
){
    BookListContent(
        vm = vm,
        onNavigationToAdd = onNavigationToAdd,
        onNavigateToMyBooks = onNavigateToMyBooks,
        onNavigateToAuth = onNavigateToAuth,
        onItemClick = onItemClick,
        onLoanShow = onLoanShow,
        onNavigateToContact = onNavigateToContact
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListContent(
    vm: BookViewModel,
    onNavigationToAdd: () -> Unit,
    onNavigateToMyBooks: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onItemClick: (BookEntity) -> Unit,
    onLoanShow: (BookEntity) -> Unit,
    onNavigateToContact: () -> Unit
) {
    var borrowBookId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteBookId by rememberSaveable { mutableStateOf<String?>(null) }
    val books = vm.books
    val bookToBorrow = books.find { it.book.id == borrowBookId }
    val bookToDelete = books.find { it.book.id == deleteBookId }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val accountLabel = when {
                vm.currentUserId == null -> null
                vm.profile == null -> "Signed in"
                vm.isAdminMode -> "${vm.profile?.name.orEmpty()} (admin)"
                else -> vm.profile?.name.orEmpty()
            }
            BookListDrawerContent(
                isDarkMode = vm.isDarkMode,
                accountLabel = accountLabel,
                onNavigateToMyBooks = {
                    if (vm.currentUserId == null) onNavigateToAuth() else onNavigateToMyBooks()
                },
                onToggleDarkMode = { vm.toggleDarkMode() },
                onSignIn = onNavigateToAuth,
                onNavigateToContact = { onNavigateToContact() },
                onSignOut = { vm.signOut() },
                onClose = { scope.launch { drawerState.close() }}
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                FolioHomeBar(
                    onMenu = { scope.launch { drawerState.open() } },
                    trailing = {
                        when {
                            vm.isAdminMode -> CommitteeBadge()
                            vm.currentUserId == null -> FolioChip(
                                label = "Sign in",
                                selected = false,
                                onClick = onNavigateToAuth
                            )
                            else -> Avatar(
                                initials = initialsOf(vm.profile?.name.orEmpty()),
                                onClick = onNavigateToMyBooks
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                if (vm.isAdminMode) {
                    FloatingActionButton(
                        onClick = onNavigationToAdd,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
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
            ) {
                if (books.isEmpty()) {
                    BookListEmptyState(
                        isLoading = vm.isLoading,
                        error = vm.loadError,
                        onRetry = { vm.reload() }
                    )
                } else {
                    val availableCount = books.count { it.isAvailable }
                    /* portrait phone: 2 cover tiles per row. wide (landscape / tablet):
                       cards with the cover on the left and the details beside it */
                    val wide = isWideScreen()
                    val screenWidthDp = LocalConfiguration.current.screenWidthDp
                    val shownBooks = vm.visibleBook
                    LazyVerticalGrid(
                        columns = when {
                            !wide -> GridCells.Fixed(2)
                            screenWidthDp >= 1000 -> GridCells.Fixed(3)
                            else -> GridCells.Fixed(2)
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .widthIn(max = 1200.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 24.dp,
                            bottom = if (vm.isAdminMode) 96.dp else 24.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (wide) 16.dp else 20.dp)
                    ) {
                        item(key = "header", span = { GridItemSpan(maxLineSpan) }) {
                            BookListHeader(
                                vm = vm,
                                isAdminMode = vm.isAdminMode,
                                total = books.size,
                                available = availableCount,
                                wide = wide
                            )
                        }
                        if (!vm.isOnline) {
                            item(span = { GridItemSpan(maxLineSpan) }) { OfflineBanner() }
                        }
                        if (shownBooks.isEmpty()) {
                            item(key = "no-match", span = { GridItemSpan(maxLineSpan) }) {
                                EmptyState(
                                    message = "No matching books found.",
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                                )
                            }
                        }
                        items(shownBooks, key = { it.book.id }) { item ->
                            BookItemCard(
                                item = item,
                                isAdminMode = vm.isAdminMode,
                                canReturn = vm.currentUserId != null && item.activeLoan?.borrowerUid == vm.currentUserId,
                                onItemClick = { onItemClick(item.book) },
                                onBorrowClick = {
                                    if (vm.currentUserId == null) onNavigateToAuth()
                                    else borrowBookId = item.book.id
                                },
                                onReturnClick = {
                                    if (vm.isAdminMode) vm.confirmReturn(item.book.id)
                                    else vm.requestReturn(item.book.id)
                                },
                                onDelete = { deleteBookId = item.book.id },
                                onLoanShow = { onLoanShow(item.book) },
                                horizontal = wide
                            )
                        }
                    }
                }
            }
        }
        if (bookToBorrow != null) {
            BookBorrowDialog(
                bookTitle = bookToBorrow.book.title,
                onConfirm = {
                    vm.borrowBook(bookToBorrow.book.id)
                    borrowBookId = null
                },
                onDismiss = { borrowBookId = null }
            )
        }
        vm.borrowErrorMessage?.let { message ->
            BookBorrowFailedDialog(
                message = message,
                onDismiss = { vm.clearBorrowError() }
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

/* hero text, search field, sort/category chips and section title above the grid */
@Composable
private fun BookListHeader(
    vm: BookViewModel,
    isAdminMode: Boolean,
    total: Int,
    available: Int,
    wide: Boolean
) {
    val hero: @Composable ColumnScope.() -> Unit = {
        Eyebrow(if (isAdminMode) "Committee desk" else "The club's shared shelf")
        Spacer(Modifier.height(8.dp))
        Text("A good book,\na little closer.", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(8.dp))
        Muted(
            text = "$total ${if (total == 1) "book" else "books"}. Shared stories. Yours to borrow.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Column {
        if (wide) {
            /* hero on the left, search beside it */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f), content = hero)
                BookSearchField(vm, Modifier.weight(1f))
            }
        } else {
            Column(content = hero)
            Spacer(Modifier.height(20.dp))
            BookSearchField(vm, Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(12.dp))
        FolioChipRow(Modifier.fillMaxWidth()) {
            FolioChip(
                label = "Title",
                selected = vm.sortOrder == SortOrder.TITLE,
                onClick = { vm.updateSortOrder(SortOrder.TITLE) }
            )
            FolioChip(
                label = "Author",
                selected = vm.sortOrder == SortOrder.AUTHOR,
                onClick = { vm.updateSortOrder(SortOrder.AUTHOR) }
            )
            FolioChip(
                label = "Available First",
                selected = vm.sortOrder == SortOrder.AVAILABLE_FIRST,
                onClick = { vm.updateSortOrder(SortOrder.AVAILABLE_FIRST) }
            )
        }
        Spacer(Modifier.height(8.dp))
        FolioChipRow(Modifier.fillMaxWidth()) {
            FolioChip(
                label = "All Categories",
                selected = vm.selectedCategoryId == null,
                onClick = { vm.updateCategoryFilter(null) }
            )
            vm.categories.forEach { category ->
                FolioChip(
                    label = "${category.name} (${vm.categoryBookCounts[category.id] ?: 0})",
                    selected = vm.selectedCategoryId == category.id,
                    onClick = { vm.updateCategoryFilter(category.id) }
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "The collection · $available available",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BookSearchField(vm: BookViewModel, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = vm.searchQuery,
        onValueChange = { vm.updateSearchQuery(it) },
        label = { Text("Search by title, author, or ISBN") },
        modifier = modifier,
        singleLine = true,
        shape = FolioFieldShape,
        colors = folioFieldColors(),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (vm.searchQuery.isNotEmpty() || vm.selectedCategoryId != null) {
                IconButton(onClick = { vm.clearFilter() }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search and filters")
                }
            }
        }
    )
}

/*
 one book in the grid: status chip + the action for this user.
 horizontal = false: cover tile on top, details underneath (portrait phones)
 horizontal = true : cover on the left, details beside it (landscape / tablets)
*/
@Composable
fun BookItemCard(
    item: BookWithLoans,
    isAdminMode: Boolean,
    canReturn: Boolean,
    onItemClick: () -> Unit,
    onBorrowClick: () -> Unit,
    onReturnClick: () -> Unit,
    onDelete: () -> Unit,
    onLoanShow: () -> Unit,
    horizontal: Boolean = false
){
    val book = item.book
    val loan = item.activeLoan
    val (chipText, tone) = when {
        item.isAvailable -> "Available" to Tone.Good
        !item.availabilityKnown -> "Checking…" to Tone.Neutral
        item.isReturnRequested -> "Return pending" to Tone.Neutral
        loan != null && BookRules.isOverdue(loan.dueAt) -> "Overdue" to Tone.Danger
        canReturn -> "Your loan" to Tone.Warn
        else -> "On loan" to Tone.Neutral
    }
    val isBorrowed = !item.isAvailable && item.availabilityKnown

    val details: @Composable ColumnScope.() -> Unit = {
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = if (horizontal) 2 else 1,
            overflow = TextOverflow.Ellipsis
        )
        Muted(book.author, maxLines = 1)
        Spacer(Modifier.height(8.dp))
        StatusChip(chipText, tone)
        if (isAdminMode && isBorrowed) {
            Spacer(Modifier.height(4.dp))
            Muted(
                text = "By ${loan?.borrowerName ?: "Unknown"}",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
        if (isBorrowed && (isAdminMode || canReturn)) {
            Spacer(Modifier.height(2.dp))
            DueDateText(dueAt = loan?.dueAt ?: 0L)
        }
        if (!isAdminMode) {
            if (item.isAvailable) {
                Spacer(Modifier.height(10.dp))
                FolioButton(
                    text = "Borrow",
                    onClick = onBorrowClick,
                    height = 40.dp
                )
            } else if (canReturn) {
                Spacer(Modifier.height(10.dp))
                FolioButton(
                    text = if (item.isReturnRequested) "Return requested" else "Request return",
                    onClick = onReturnClick,
                    filled = false,
                    enabled = !item.isReturnRequested,
                    height = 40.dp
                )
            }
        }
    }

    if (horizontal) {
        FolioCard(
            onClick = if (isAdminMode) onLoanShow else null,
            contentPadding = 12.dp
        ) {
            Box {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        BookCover(
                            coverBase64 = book.coverBase64,
                            coverUrl = book.coverUrl,
                            title = book.title,
                            author = book.author,
                            modifier = Modifier
                                .width(88.dp)
                                .aspectRatio(0.72f)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = if (isAdminMode) 36.dp else 0.dp),
                        content = details
                    )
                }
                if (isAdminMode) {
                    BookActionMenu(
                        onEdit = onItemClick,
                        onDelete = onDelete,
                        onReturn = if (item.isReturnRequested) onReturnClick else null,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (isAdminMode)
                        it.clickable(onClick = onLoanShow)
                    else
                        it
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                BookCover(
                    coverBase64 = book.coverBase64,
                    coverUrl = book.coverUrl,
                    title = book.title,
                    author = book.author,
                    modifier = Modifier
                        .width(110.dp)
                        .aspectRatio(0.72f)
                )
                if (isAdminMode) {
                    BookActionMenu(
                        onEdit = onItemClick,
                        onDelete = onDelete,
                        onReturn = if (item.isReturnRequested) onReturnClick else null,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            details()
        }
    }
}

@Composable
fun BookListDrawerContent(
    isDarkMode: Boolean,
    accountLabel: String?,
    onNavigateToMyBooks: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onNavigateToContact: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 4.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "CIS Library",
                        fontFamily = FolioSerif,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
            if (accountLabel != null) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Eyebrow("Signed in as", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = accountLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline
            )
            NavigationDrawerItem(
                label = { Text(if (accountLabel?.endsWith("(admin)") == true) "Loans (admin)" else "My Books") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Bookmarks,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = {
                    onNavigateToMyBooks()
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
                label = { Text("Contact Us") },
                icon = { Icon(imageVector = Icons.Default.Mail, contentDescription = "Contact Us") },
                selected = false,
                onClick = {
                    onNavigateToContact()
                    onClose()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            if (accountLabel == null) {
                NavigationDrawerItem(
                    label = { Text("Sign in / Create account") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Sign in"
                        )
                    },
                    selected = false,
                    onClick = {
                        onSignIn()
                        onClose()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            } else {
                NavigationDrawerItem(
                    label = { Text("Sign out") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Sign out"
                        )
                    },
                    selected = false,
                    onClick = {
                        onSignOut()
                        onClose()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}
@Composable
fun BookBorrowDialog(
    bookTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Borrow Book") },
        text = { Text("Borrow \"$bookTitle\"? It is due back in ${BookRules.LOAN_DAYS} days.") },
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
fun BookBorrowFailedDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Action Failed") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
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
    modifier: Modifier = Modifier,
    onReturn: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(48.dp)
        ) {
            Box (
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Action",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            if (onReturn != null) {
                DropdownMenuItem(
                    text = { Text("Confirm return") },
                    onClick = {
                        expanded = false
                        onReturn()
                    }
                )
            }
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

@Composable
fun BookListEmptyState(
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            error != null -> Column(
                modifier = Modifier.padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                FolioButton(
                    text = "Retry",
                    onClick = onRetry,
                    filled = false,
                    modifier = Modifier.width(160.dp)
                )
            }
            else -> EmptyState(
                message = "No book available. Please contact admin to add the book."
            )
        }
    }
}