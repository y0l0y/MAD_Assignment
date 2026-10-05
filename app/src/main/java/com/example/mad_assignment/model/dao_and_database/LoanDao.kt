package com.example.mad_assignment.model.dao_and_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.mad_assignment.model.entity.LoanEntity

@Dao
interface LoanDao {
    @Query("UPDATE loans SET returnedAt = :time WHERE bookId = :bookId AND returnedAt IS NULL")
    suspend fun returnLoan(bookId: Long, time: Long)

    @Query("SELECT COUNT(*) FROM loans WHERE bookId = :bookId AND returnedAt IS NULL")
    suspend fun countActiveLoans(bookId: Long): Int

    @Insert
    suspend fun insertLoan(loan: LoanEntity): Long

    @Transaction
    suspend fun borrowIfAvailable(loan: LoanEntity): Boolean {
        if (countActiveLoans(loan.bookId) > 0) return false
        insertLoan(loan)
        return true
    }
}