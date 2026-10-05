package com.example.mad_assignment.model.entity

import androidx.room.Embedded
import androidx.room.Relation

data class BookWithLoans(
    /* pull all the BookEntity column to here */
    @Embedded val book: BookEntity,
    /* match each LoanEntity with BookEntity */
    @Relation(
        parentColumn = "id",
        entityColumn = "bookId"
    )
    val loans: List<LoanEntity>
) {
    /* null = nobody is borrowing the book */
    /* use to check the current loan book */
    val activeLoan: LoanEntity? get() = loans.firstOrNull { it.returnedAt == null }

    /* use for borrow and return button */
    val isAvailable: Boolean get() = activeLoan == null

    /* sort for the history screen */
    val loansNewestFirst: List<LoanEntity>
        get() = loans.sortedByDescending { it.borrowedAt }
}