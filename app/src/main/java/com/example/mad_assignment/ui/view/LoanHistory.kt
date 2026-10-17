package com.example.mad_assignment.ui.view

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.entity.BookWithLoans
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.ui.components.EmptyState
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioBackBar
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
fun LoanHistoryScreen(
    bookID: String,
    vm: BookViewModel,
    onNavigateBack: () -> Unit
) {
    LoanHistoryContent(
        item = vm.books.find { it.book.id == bookID },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun LoanHistoryContent(
    item: BookWithLoans?,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { FolioBackBar(title = "Loan History", onBack = onNavigateBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                item == null -> {
                    Text(
                        text = "Book not found",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                item.loans.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.book.title,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        EmptyState(message = "This book has never been borrowed")
                    }
                }
                else -> {
                    /* one card per row on a phone, two per row when the screen is wide */
                    val wide = isWideScreen()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (wide) 2 else 1),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .widthIn(max = 1100.dp)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                Eyebrow("Loan history")
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = item.book.title,
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Spacer(Modifier.height(4.dp))
                                Muted(
                                    text = "by ${item.book.author}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(4.dp))
                                Muted("Total loans: ${item.loans.size}")
                            }
                        }
                        items(
                            items = item.loansNewestFirst,
                            key = { it.id }
                        ) { loan ->
                            LoanItemCard(loan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoanItemCard(loan: LoanEntity) {
    val returned = loan.returnedAt != null
    FolioCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = loan.borrowerName,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (returned) StatusChip("Returned", Tone.Good)
            else StatusChip("No return yet", Tone.Warn)
        }
        Spacer(Modifier.height(10.dp))
        Muted("Borrowed ${formateDateTime(loan.borrowedAt)}")
        if (loan.dueAt > 0) {
            Muted("Due ${formatDay(loan.dueAt)}")
        }
        if (returned) {
            Muted("Returned ${formateDateTime(loan.returnedAt)}")
        }
    }
}

private fun formateDateTime(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))