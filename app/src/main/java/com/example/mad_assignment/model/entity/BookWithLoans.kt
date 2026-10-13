package com.example.mad_assignment.model.entity

data class BookWithLoans(
    val book: BookEntity,
    val loans: List<LoanEntity>,
    /* from bookStatus: id of the loan that holds the book, null = on the shelf */
    val activeLoanId: String? = null,
    /* false while the status has not arrived (offline, or not published yet) */
    val availabilityKnown: Boolean = true
) {
    /* null when nobody holds the book, or the loan is not visible to this user */
    val activeLoan: LoanEntity? get() = activeLoanId?.let { id -> loans.firstOrNull { it.id == id } }

    /* borrower asked to return it, waiting for admin confirmation */
    val isReturnRequested: Boolean get() = activeLoan?.returnRequestedAt != null

    /* use for borrow and return button */
    val isAvailable: Boolean get() = availabilityKnown && activeLoanId == null

    /* sort for the history screen */
    val loansNewestFirst: List<LoanEntity>
        get() = loans.sortedByDescending { it.borrowedAt }
}
