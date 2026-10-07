package com.example.mad_assignment.model.dao_and_database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.mad_assignment.model.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("UPDATE books SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun removeCategory(categoryId: Long)

    @Delete
    suspend fun deleteCategoryRow(category: CategoryEntity)

    @Transaction
    suspend fun deleteCategory(category: CategoryEntity) {
        removeCategory(category.id)
        deleteCategoryRow(category)
    }
}