package com.example.mad_assignment.model.entity

import com.google.firebase.firestore.DocumentId

/* bookStatus/{bookId}: who holds the book (null = on the shelf). Small on purpose, it is the only live book data. */
data class BookStatus(
    @DocumentId val id: String = "",
    val activeLoanId: String? = null
)
