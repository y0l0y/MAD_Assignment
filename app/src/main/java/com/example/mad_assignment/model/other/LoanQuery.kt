package com.example.mad_assignment.model.other

import com.example.mad_assignment.model.entity.LoanEntity

/* which loans the admin loan list shows */
enum class LoanFilter {
    ALL,
    BORROWED,
    OVERDUE,
    RETURNED
}

/* where a loan is in its life: out -> return requested by the borrower -> confirmed by the admin */
enum class LoanStatus {
    BORROWED,
    RETURN_REQUESTED,
    RETURNED
}

/* a loan plus the decisions already made about it, so the screen only has to display it */
data class LoanRow(
    val loan: LoanEntity,
    val status: LoanStatus,
    val isOverdue: Boolean
)

/*
two purposes (same idea as BookQuery, no android and no network, so easy to unit test)
1. Work out the status / overdue state of a loan
2. Filter and count loans for the admin screens
*/
object LoanQuery {
    fun status(loan: LoanEntity): LoanStatus = when {
        loan.returnedAt != null -> LoanStatus.RETURNED
        loan.returnRequestedAt != null -> LoanStatus.RETURN_REQUESTED
        else -> LoanStatus.BORROWED
    }

    /* a returned loan is never overdue, even if it came back late */
    fun isOverdue(loan: LoanEntity): Boolean =
        loan.returnedAt == null && BookRules.isOverdue(loan.dueAt)

    fun toRow(loan: LoanEntity) = LoanRow(loan, status(loan), isOverdue(loan))

    /* BORROWED means "still out", so it includes loans whose return was requested */
    fun rows(loans: List<LoanEntity>, filter: LoanFilter): List<LoanRow> =
        loans.map { toRow(it) }.filter { row ->
            when (filter) {
                LoanFilter.ALL -> true
                LoanFilter.BORROWED -> row.status != LoanStatus.RETURNED
                LoanFilter.OVERDUE -> row.isOverdue
                LoanFilter.RETURNED -> row.status == LoanStatus.RETURNED
            }
        }

    fun overdueCount(loans: List<LoanEntity>): Int = loans.count { isOverdue(it) }

    /* loans waiting for the admin to confirm the return, oldest request first */
    fun pendingReturns(loans: List<LoanEntity>): List<LoanEntity> =
        loans.filter { status(it) == LoanStatus.RETURN_REQUESTED }
            .sortedBy { it.returnRequestedAt }
}