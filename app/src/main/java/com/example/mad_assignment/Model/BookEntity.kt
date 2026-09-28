package com.example.mad_assignment.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val isbn: String,
    val isAvailable: Boolean = true,
    val borrowedBy: String? = null,
    val coverPath: String? = null
)