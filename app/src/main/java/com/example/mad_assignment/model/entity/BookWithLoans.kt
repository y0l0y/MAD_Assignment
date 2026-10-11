package com.example.mad_assignment.model.entity

data class BookWithLoans(
    val book: BookEntity,
    val loans: List<LoanEntity>
) {
    /* null = nobody is borrowing the book */
    /* use to check the current loan book */
    val activeLoan: LoanEntity? get() = loans.firstOrNull { it.returnedAt == null }

    /* borrower asked to return it, waiting for admin confirmation */
    val isReturnRequested: Boolean get() = activeLoan?.returnRequestedAt != null

    /* use for borrow and return button */
    val isAvailable: Boolean get() = activeLoan == null

    /* sort for the history screen */
    val loansNewestFirst: List<LoanEntity>
        get() = loans.sortedByDescending { it.borrowedAt }
}