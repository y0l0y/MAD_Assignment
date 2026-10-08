package com.example.mad_assignment.model.other

import com.example.mad_assignment.model.entity.BookWithLoans

enum class SortOrder {
    TITLE,
    AUTHOR,
    AVAILABLE_FIRST
}


/*
three purpose
1. Text Search
2. Category Filter
3. Sorting
*/

object BookQuery {
    fun apply(
        books: List<BookWithLoans>,
        query: String,
        categoryId: String?,
        sortOrder: SortOrder
    ): List<BookWithLoans> {
        val q = query.trim()
        val isbnQuery = BookRules.normaliseIsbn(q)
        val filtered = books.filter { item ->
            val book = item.book
            val matchesCategory = categoryId == null || book.categoryId == categoryId
            val matchesText = q.isEmpty() ||
                    book.title.contains(q, ignoreCase = true) ||
                    book.author.contains(q, ignoreCase = true) ||
                    (isbnQuery.isNotEmpty() && BookRules.normaliseIsbn(book.isbn).contains(isbnQuery))
            matchesCategory && matchesText
        }
        return when (sortOrder) {
            SortOrder.TITLE -> filtered.sortedBy { it.book.title.lowercase() }
            SortOrder.AUTHOR -> filtered.sortedWith(
                compareBy(
                    { it.book.author.lowercase() },
                    { it.book.title.lowercase() }
                )
            )
            SortOrder.AVAILABLE_FIRST -> filtered.sortedWith(
                compareByDescending<BookWithLoans> { it.isAvailable }
                    .thenBy { it.book.title.lowercase() }
            )
        }

    }
}
