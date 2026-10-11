package com.example.mad_assignment.model.entity

import com.google.firebase.firestore.DocumentId

data class LoanEntity(
    @DocumentId val id: String = "",
    val bookId: String = "",
    val borrowerName: String = "",
    val borrowerUid: String = "",
    val borrowedAt: Long = System.currentTimeMillis(),
    /* 0 = no due date */
    val dueAt: Long = 0L,
    val returnedAt: Long? = null,
    /* set by the borrower, waits for an admin to confirm */
    val returnRequestedAt: Long? = null
)