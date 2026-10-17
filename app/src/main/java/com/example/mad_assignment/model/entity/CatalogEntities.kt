package com.example.mad_assignment.model.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/*
 * the book list stored on the phone (seeded on install, refreshed by the version check)
 * relationship: many books -> one category (categoryId points at LocalCategoryEntity.id)
 * - SET_NULL: if a category row is removed, its books stay and just become "no category"
 *   (same rule the admin delete-category action applies in firebase)
 * - the index on categoryId keeps the join and the foreign key checks fast
 */
@Entity(
    tableName = "catalog_books",
    foreignKeys = [
        ForeignKey(
            entity = LocalCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class LocalBookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val categoryId: String?,
    val coverBase64: String?,
    val coverUrl: String?
)

@Entity(tableName = "catalog_categories")
data class LocalCategoryEntity(
    @PrimaryKey val id: String,
    val name: String
)

/* one category with all of its books (one-to-many), loaded by room in one query */
data class CategoryWithBooks(
    @Embedded val category: LocalCategoryEntity,
    @Relation(parentColumn = "id", entityColumn = "categoryId")
    val books: List<LocalBookEntity>
)

/* one book with its category, or null when it has none (many-to-one) */
data class BookWithCategory(
    @Embedded val book: LocalBookEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: LocalCategoryEntity?
)

/* one row (id = 0): which catalog version is on the phone */
@Entity(tableName = "catalog_meta")
data class CatalogMetaEntity(
    @PrimaryKey val id: Int = 0,
    val version: Long
)

fun LocalBookEntity.toBook() = BookEntity(
    id = id,
    title = title,
    author = author,
    isbn = isbn,
    categoryId = categoryId,
    coverBase64 = coverBase64,
    coverUrl = coverUrl
)

fun BookEntity.toLocal() = LocalBookEntity(
    id = id,
    title = title,
    author = author,
    isbn = isbn,
    categoryId = categoryId,
    coverBase64 = coverBase64,
    coverUrl = coverUrl
)

fun LocalCategoryEntity.toCategory() = CategoryEntity(id = id, name = name)

fun CategoryEntity.toLocal() = LocalCategoryEntity(id = id, name = name)