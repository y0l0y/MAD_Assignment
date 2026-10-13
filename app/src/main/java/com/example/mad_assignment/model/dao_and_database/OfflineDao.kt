package com.example.mad_assignment.model.dao_and_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.mad_assignment.model.entity.BorrowWithBook
import com.example.mad_assignment.model.entity.CurrentBorrowEntity
import com.example.mad_assignment.model.entity.OfflineBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineDao {
    @Query("SELECT * FROM offline_books ORDER BY title COLLATE NOCASE ASC")
    fun getOfflineBook(): Flow<List<OfflineBookEntity>>

    @Transaction
    @Query("SELECT * FROM current_borrows ORDER BY borrowedAt DESC")
    fun getBorrowsWithBook(): Flow<List<BorrowWithBook>>

    @Upsert
    suspend fun upsertBooks(books: List<OfflineBookEntity>)

    @Insert
    suspend fun insertBorrows(borrows: List<CurrentBorrowEntity>)

    @Query("DELETE FROM current_borrows")
    suspend fun clearBorrows()

    @Query("DELETE FROM offline_books")
    suspend fun clearBooks()

    @Query("DELETE FROM offline_books WHERE id NOT IN (:ids)")
    suspend fun deleteBooksNotIn(ids: List<String>)

    @Transaction
    suspend fun replaceCache(
        books: List<OfflineBookEntity>,
        borrows: List<CurrentBorrowEntity>
    ) {
        clearBorrows()
        if (books.isEmpty())
            clearBooks()
        else
            deleteBooksNotIn(books.map { it.id })
        upsertBooks(books)
        insertBorrows(borrows)
    }
}