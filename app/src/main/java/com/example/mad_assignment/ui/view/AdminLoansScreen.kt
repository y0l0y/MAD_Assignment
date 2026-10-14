package com.example.mad_assignment.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mad_assignment.model.entity.LoanEntity
import com.example.mad_assignment.model.other.BookRules
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LoanFilter(val label: String) {
    ALL("All"),
    BORROWED("Borrowed"),
    OVERDUE("Overdue"),
    RETURNED("Returned")
}

private fun LoanEntity.isReturned() = returnedAt != null
private fun LoanEntity.isOverdueNow() = !isReturned() && BookRules.isOverdue(dueAt)
private fun LoanEntity.statusLabel() = when {
    isReturned() -> "Returned"
    returnRequestedAt != null -> "Return requested"
    else -> "Borrowed"
}

private fun dateTime(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(millis))

/* admin overview: returns waiting for confirmation, and every loan */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoansContent(
    allLoans: List<LoanEntity>,
    pendingReturns: List<LoanEntity>,
    onConfirm: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var filterName by rememberSaveable { mutableStateOf(LoanFilter.ALL.name) }
    val filter = LoanFilter.valueOf(filterName)
    val overdueCount = allLoans.count { it.isOverdueNow() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pending returns (${pendingReturns.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All loans (${allLoans.size})") }
                )
            }
            if (selectedTab == 0) {
                if (pendingReturns.isEmpty()) {
                    EmptyText("No returns are waiting for confirmation.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingReturns, key = { it.id }) { loan ->
                            PendingReturnCard(loan = loan, onConfirm = { onConfirm(loan.bookId) })
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LoanFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filterName = option.name },
                            label = {
                                Text(
                                    if (option == LoanFilter.OVERDUE) "Overdue ($overdueCount)"
                                    else option.label
                                )
                            }
                        )
                    }
                }
                val shown = allLoans.filter {
                    when (filter) {
                        LoanFilter.ALL -> true
                        LoanFilter.BORROWED -> !it.isReturned()
                        LoanFilter.OVERDUE -> it.isOverdueNow()
                        LoanFilter.RETURNED -> it.isReturned()
                    }
                }
                if (shown.isEmpty()) {
                    EmptyText("No loans to show.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(shown, key = { it.id }) { loan -> LoanRowCard(loan) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyText(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PendingReturnCard(loan: LoanEntity, onConfirm: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = loan.bookTitle.ifBlank { "Unknown book" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Borrower: ${loan.borrowerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Borrowed: ${dateTime(loan.borrowedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            loan.returnRequestedAt?.let {
                Text(
                    text = "Return requested: ${dateTime(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            DueDateText(dueAt = loan.dueAt)
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Confirm return")
            }
        }
    }
}

@Composable
private fun LoanRowCard(loan: LoanEntity) {
    val overdue = loan.isOverdueNow()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (overdue) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = loan.bookTitle.ifBlank { "Unknown book" },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Borrower: ${loan.borrowerName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Borrowed: ${dateTime(loan.borrowedAt)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (loan.dueAt > 0) {
                Text(
                    text = "Due: ${formatDay(loan.dueAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = if (overdue) "${loan.statusLabel()} (overdue)" else loan.statusLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    overdue -> MaterialTheme.colorScheme.error
                    loan.isReturned() -> MaterialTheme.colorScheme.onSurfaceVariant
                    loan.returnRequestedAt != null -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            loan.returnedAt?.let {
                Text(
                    text = "Returned: ${dateTime(it)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
