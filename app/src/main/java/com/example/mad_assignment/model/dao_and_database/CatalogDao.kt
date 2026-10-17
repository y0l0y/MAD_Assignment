package com.example.mad_assignment.model.dao_and_database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.mad_assignment.model.entity.BookWithCategory
import com.example.mad_assignment.model.entity.CatalogMetaEntity
import com.example.mad_assignment.model.entity.CategoryWithBooks
import com.example.mad_assignment.model.entity.LocalBookEntity
import com.example.mad_assignment.model.entity.LocalCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalog_books")
    fun books(): Flow<List<LocalBookEntity>>

    @Query("SELECT * FROM catalog_categories ORDER BY name COLLATE NOCASE ASC")
    fun categories(): Flow<List<LocalCategoryEntity>>

    /* relation queries: room joins books and categories through the foreign key */
    @Transaction
    @Query("SELECT * FROM catalog_categories ORDER BY name COLLATE NOCASE ASC")
    fun categoriesWithBooks(): Flow<List<CategoryWithBooks>>

    @Transaction
    @Query("SELECT * FROM catalog_books")
    fun booksWithCategory(): Flow<List<BookWithCategory>>

    @Query("SELECT * FROM catalog_books")
    suspend fun allBooks(): List<LocalBookEntity>

    @Query("SELECT * FROM catalog_categories")
    suspend fun allCategories(): List<LocalCategoryEntity>

    @Query("SELECT COUNT(*) FROM catalog_books")
    suspend fun bookCount(): Int

    @Query("SELECT title FROM catalog_books WHERE id = :id")
    suspend fun titleOf(id: String): String?

    @Query("SELECT version FROM catalog_meta WHERE id = 0")
    suspend fun version(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<LocalBookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<LocalCategoryEntity>)

    @Upsert
    suspend fun setMeta(meta: CatalogMetaEntity)

    @Query("DELETE FROM catalog_books")
    suspend fun clearBooks()

    @Query("DELETE FROM catalog_categories")
    suspend fun clearCategories()

    /*
     * swap the whole catalog and its version in one go
     * the foreign key means a category must exist before a book can point at it, so:
     * - categories are inserted before books
     * - a book whose category is missing from the list (firebase data out of sync) is saved
     *   with no category instead of failing the whole sync
     */
    @Transaction
    suspend fun replaceCatalog(
        books: List<LocalBookEntity>,
        categories: List<LocalCategoryEntity>,
        version: Long
    ) {
        val knownCategoryIds = categories.map { it.id }.toSet()
        val safeBooks = books.map { book ->
            if (book.categoryId == null || book.categoryId in knownCategoryIds) book
            else book.copy(categoryId = null)
        }
        clearBooks()
        clearCategories()
        insertCategories(categories)
        insertBooks(safeBooks)
        setMeta(CatalogMetaEntity(version = version))
    }
}