package com.example.mad_assignment.model.entity

import com.google.firebase.firestore.DocumentId

data class BookEntity(
    @DocumentId val id: String = "",
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val categoryId: String? = null,
    val coverBase64: String? = null,
    val coverUrl: String? = null
)

