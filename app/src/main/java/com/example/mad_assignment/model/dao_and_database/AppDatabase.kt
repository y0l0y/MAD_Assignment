package com.example.mad_assignment.model.dao_and_database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mad_assignment.model.entity.CatalogMetaEntity
import com.example.mad_assignment.model.entity.LocalBookEntity
import com.example.mad_assignment.model.entity.LocalCategoryEntity

@Database(
    entities = [LocalBookEntity::class, LocalCategoryEntity::class, CatalogMetaEntity::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao

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