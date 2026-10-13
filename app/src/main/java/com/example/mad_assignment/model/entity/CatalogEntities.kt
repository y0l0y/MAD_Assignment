package com.example.mad_assignment.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/* the book list stored on the phone (seeded on install, refreshed by the version check) */
@Entity(tableName = "catalog_books")
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
