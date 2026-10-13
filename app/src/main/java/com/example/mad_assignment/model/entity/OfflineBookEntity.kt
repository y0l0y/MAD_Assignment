package com.example.mad_assignment.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_books")
data class OfflineBookEntity (
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val coverBase64: String?,
    val coverUrl: String?,
    val cachedAt: Long
)
