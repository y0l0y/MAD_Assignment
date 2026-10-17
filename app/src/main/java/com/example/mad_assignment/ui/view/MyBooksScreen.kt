package com.example.mad_assignment.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.ui.components.EmptyState
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioBanner
import com.example.mad_assignment.ui.components.FolioButton
import com.example.mad_assignment.ui.components.FolioCard
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.StatusChip
import com.example.mad_assignment.ui.components.Tone
import com.example.mad_assignment.ui.components.isWideScreen
import com.example.mad_assignment.viewModel.BookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyBookScreen(
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    if (vm.isAdminMode) {
        AdminLoansContent(
            rows = vm.loanRows,
            totalCount = vm.allLoans.size,
            overdueCount = vm.overdueLoanCount,
            selectedFilter = vm.loanFilter,
            pendingReturns = vm.pendingReturns,
            onFilterChange = { vm.updateLoanFilter(it) },
            onConfirm = { vm.confirmReturn(it) },
            onNavigateBack = onNavigateBack
        )
    } else {
        MyBookContent(
            loans = vm.myLoans.mapNotNull { item -> item.activeLoan?.let { item.book to it } },
            onReturn = { vm.requestReturn(it) },
            onNavigateBack = onNavigateBack
        )
    }
    vm.borrowErrorMessage?.let { message ->
        BookBorrowFailedDialog(
            message = message,
            onDismiss = { vm.clearBorrowError() }
        )
    }
}

@Composable
fun MyBookContent(
    loans: List<Pair<BookEntity, LoanEntity>>,
    onReturn: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FolioBackBar(title = "My Books", onBack = onNavigateBack) }
    ) { paddingValues ->
        BorrowedBookList(
            loans = loans,
            onReturn = onReturn,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
private fun BorrowedBookList(
    loans: List<Pair<BookEntity, LoanEntity>>,
    onReturn: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }
    /* one loan card per row on a phone, two per row when the screen is wide */
    val wide = isWideScreen()
    Box(modifier = modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (wide) 2 else 1),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .widthIn(max = 1100.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Eyebrow("Your reading chapter")
                Spacer(Modifier.height(8.dp))
                Text("My borrowed books", style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(8.dp))
                Muted(
                    text = "${loans.size} open ${if (loans.size == 1) "loan" else "loans"} · $today",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        if (loans.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState(
                    message = "You have no unreturned books.",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(loans, key = { it.second.id }) { (book, loan) ->
                LoanCard(book = book, loan = loan, onReturn = { onReturn(book.id) })
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FolioBanner(
                    title = "Returning a book?",
                    body = "Hand it in to the committee, then tap Return book. Your loan stays open until the committee confirms receipt."
                )
            }
        }
    }
    }
}

@Composable
private fun LoanCard(
    book: BookEntity,
    loan: LoanEntity,
    onReturn: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val returnRequested = loan.returnRequestedAt != null
    val overdue = BookRules.isOverdue(loan.dueAt)
    val soon = loan.dueAt > 0 && daysLeft(loan.dueAt) <= 3
    val (chipText, tone) = when {
        returnRequested -> "Return pending" to Tone.Neutral
        overdue -> "Overdue" to Tone.Danger
        soon -> {
            val days = daysLeft(loan.dueAt)
            "Due soon · $days ${if (days == 1) "day" else "days"} left" to Tone.Warn
        }
        else -> "On loan" to Tone.Neutral
    }
    val dueColor = when {
        returnRequested -> cs.onSurface
        overdue -> cs.error
        soon -> cs.tertiary
        else -> cs.onSurface
    }

    FolioCard {
        Row {
            BookCover(
                coverBase64 = book.coverBase64,
                coverUrl = book.coverUrl,
                title = book.title,
                author = book.author,
                modifier = Modifier
                    .width(64.dp)
                    .aspectRatio(0.72f)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(book.title, style = MaterialTheme.typography.titleLarge)
                Muted(book.author)
                Spacer(Modifier.height(6.dp))
                StatusChip(chipText, tone)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            if (loan.dueAt > 0) {
                Column {
                    Text(
                        "RETURN BY",
                        color = cs.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = formatDay(loan.dueAt),
                        color = dueColor,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }
            Muted("Borrowed ${formatDay(loan.borrowedAt)}")
        }
        Spacer(Modifier.height(14.dp))
        if (returnRequested) {
            Muted("Waiting for the committee to confirm they received it.")
        } else {
            FolioButton(
                text = "Request return",
                onClick = onReturn,
                icon = Icons.Outlined.Undo,
                filled = false
            )
        }
    }
}