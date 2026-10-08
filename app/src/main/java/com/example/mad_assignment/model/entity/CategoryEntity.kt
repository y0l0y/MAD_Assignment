package com.example.mad_assignment.model.entity

import com.google.firebase.firestore.DocumentId

data class CategoryEntity(
    @DocumentId val id: String = "",
    val name: String = ""
)