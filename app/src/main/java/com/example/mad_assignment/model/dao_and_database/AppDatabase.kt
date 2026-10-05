package com.example.mad_assignment.model.dao_and_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mad_assignment.model.entity.BookEntity
import com.example.mad_assignment.model.entity.CategoryEntity
import com.example.mad_assignment.model.entity.LoanEntity

@Database(
    entities = [BookEntity::class, LoanEntity::class, CategoryEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao() : BooksDao
    abstract fun loanDao() : LoanDao
    abstract fun categoryDao() : CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "club_library_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}