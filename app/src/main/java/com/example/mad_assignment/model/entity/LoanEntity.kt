package com.example.mad_assignment.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/* one loans.bookId match one books.id so one book can have many loan */
@Entity(
    tableName = "loans",
    foreignKeys = [ForeignKey(
        entity = BookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("bookId")]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val borrowerName: String,
    val borrowedAt: Long = System.currentTimeMillis(),
    val returnedAt: Long? = null
)