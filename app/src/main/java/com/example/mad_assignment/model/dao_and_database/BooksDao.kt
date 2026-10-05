package com.example.mad_assignment.model.dao_and_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mad_assignment.model.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BooksDao {
    /* Using Flow to keep update the data when the data in database is making changes*/
    @Query("Select * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)
}