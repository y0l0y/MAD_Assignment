package com.example.mad_assignment.model.dao_and_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.BookWithLoans
import kotlinx.coroutines.flow.Flow

@Dao
interface BooksDao {
    /* Using Flow to keep update the data when the data in database is making changes*/
    @Transaction
    @Query("Select * FROM books ORDER BY title ASC")
    fun getBooksWithLoans(): Flow<List<BookWithLoans>>

    /* use for history screen */
    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookWithLoans(bookId: Long): Flow<BookWithLoans?>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int

    @Insert
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)
}