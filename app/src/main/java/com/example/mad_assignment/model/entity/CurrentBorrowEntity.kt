package com.example.mad_assignment.model.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "current_borrows",
    foreignKeys = [ForeignKey(
        entity = OfflineBookEntity::class,
        parentColumns = ["id"],
        childColumns = ["bookId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("bookId")]
)
data class CurrentBorrowEntity(
    @PrimaryKey val loanId: String,
    val bookId: String,
    val borrowerName: String,
    val borrowedAt: Long,
    val dueAt: Long
)

data class BorrowWithBook(
    @Embedded val borrow: CurrentBorrowEntity,
    @Relation(parentColumn = "bookId", entityColumn = "id")
    val book: OfflineBookEntity
)