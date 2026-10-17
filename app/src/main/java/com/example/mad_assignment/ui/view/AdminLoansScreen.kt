package com.example.mad_assignment.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.model.other.BookRules
import com.example.mad_assignment.model.other.LoanFilter
import com.example.mad_assignment.model.other.LoanRow
import com.example.mad_assignment.model.other.LoanStatus
import com.example.mad_assignment.ui.components.CommitteeBadge
import com.example.mad_assignment.ui.components.EmptyState
import com.example.mad_assignment.ui.components.Eyebrow
import com.example.mad_assignment.ui.components.FolioBackBar
import com.example.mad_assignment.ui.components.FolioBanner
import com.example.mad_assignment.ui.components.FolioButton
import com.example.mad_assignment.ui.components.FolioCard
import com.example.mad_assignment.ui.components.FolioChip
import com.example.mad_assignment.ui.components.FolioChipRow
import com.example.mad_assignment.ui.components.Muted
import com.example.mad_assignment.ui.components.StatTile
import com.example.mad_assignment.ui.components.StatusChip
import com.example.mad_assignment.ui.components.Tone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* display text only: which loans match a filter and what status a loan has is decided in LoanQuery */
private fun LoanFilter.label() = when (this) {
    LoanFilter.ALL -> "All"
    LoanFilter.BORROWED -> "Borrowed"
    LoanFilter.OVERDUE -> "Overdue"
    LoanFilter.RETURNED -> "Returned"
}

private fun LoanStatus.label() = when (this) {
    LoanStatus.BORROWED -> "On loan"
    LoanStatus.RETURN_REQUESTED -> "Return pending"
    LoanStatus.RETURNED -> "Returned"
}

private fun dateTime(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))

/* admin overview: returns waiting for confirmation, and every loan */
@Composable
fun AdminLoansContent(
    rows: List<LoanRow>,
    totalCount: Int,
    overdueCount: Int,
    selectedFilter: LoanFilter,
    pendingReturns: List<LoanEntity>,
    onFilterChange: (LoanFilter) -> Unit,
    onConfirm: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    /* which tab is open is pure screen state, so it stays here */
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            FolioBackBar(
                title = "Loans",
                onBack = onNavigateBack,
                trailing = { CommitteeBadge() }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Eyebrow("Committee desk")
                    Spacer(Modifier.height(8.dp))
                    Text("Out in the world", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatTile(
                            value = "${pendingReturns.size}",
                            label = "Return pending",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            value = "$totalCount",
                            label = "All loans",
                            modifier = Modifier.weight(1f)
                        )
                        StatTile(
                            value = "$overdueCount",
                            label = "Overdue",
                            modifier = Modifier.weight(1f),
                            valueColor = if (overdueCount > 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            item {
                FolioChipRow {
                    FolioChip(
                        label = "Pending returns · ${pendingReturns.size}",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    FolioChip(
                        label = "All loans · $totalCount",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
            if (selectedTab == 0) {
                item {
                    FolioBanner(
                        title = "Book in hand? Then confirm.",
                        body = "Only confirm after checking physical receipt. This closes the loan and makes the copy available again."
                    )
                }
                if (pendingReturns.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No returns are waiting for confirmation.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(pendingReturns, key = { it.id }) { loan ->
                        PendingReturnCard(loan = loan, onConfirm = { onConfirm(loan.bookId) })
                    }
                }
            } else {
                item {
                    FolioChipRow {
                        LoanFilter.entries.forEach { option ->
                            FolioChip(
                                label = if (option == LoanFilter.OVERDUE) "Overdue ($overdueCount)"
                                else option.label(),
                                selected = selectedFilter == option,
                                onClick = { onFilterChange(option) }
                            )
                        }
                    }
                }
                if (rows.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No loans to show.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(rows, key = { it.loan.id }) { row -> LoanRowCard(row) }
                }
            }
        }
    }
}

@Composable
private fun PendingReturnCard(loan: LoanEntity, onConfirm: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    FolioCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip("Return pending", Tone.Neutral)
            loan.returnRequestedAt?.let {
                Muted(dateTime(it), style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = loan.bookTitle.ifBlank { "Unknown book" },
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = loan.borrowerName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        Muted("Borrowed ${dateTime(loan.borrowedAt)}")
        if (loan.dueAt > 0) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Muted("Original due date")
                Text(
                    text = formatDay(loan.dueAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (BookRules.isOverdue(loan.dueAt)) cs.error else cs.onSurface
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        FolioButton(
            text = "Confirm receipt",
            onClick = onConfirm,
            icon = Icons.Outlined.Check
        )
    }
}

@Composable
private fun LoanRowCard(row: LoanRow) {
    val loan = row.loan
    val overdue = row.isOverdue
    val chipText = if (overdue) "${row.status.label()} · overdue" else row.status.label()
    val tone = when {
        overdue -> Tone.Danger
        row.status == LoanStatus.RETURNED -> Tone.Good
        else -> Tone.Neutral
    }
    FolioCard(contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = loan.bookTitle.ifBlank { "Unknown book" },
                    style = MaterialTheme.typography.titleLarge
                )
                Muted(loan.borrowerName)
            }
        }
        Spacer(Modifier.height(8.dp))
        StatusChip(chipText, tone)
        Spacer(Modifier.height(10.dp))
        Muted("Borrowed ${dateTime(loan.borrowedAt)}", style = MaterialTheme.typography.bodySmall)
        if (loan.dueAt > 0) {
            Muted("Due ${formatDay(loan.dueAt)}", style = MaterialTheme.typography.bodySmall)
        }
        loan.returnedAt?.let {
            Muted("Returned ${dateTime(it)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}