package com.example.mad_assignment.Model

object BookRules {
    const val MAX_TITLE_LENGTH = 200
    const val MAX_AUTHOR_LENGTH = 100

    fun normaliseIsbn(isbn: String): String =
        isbn.filter { it != '-' && it != ' ' }.uppercase()
    fun validateTitle(title: String): String? =
        when {
            title.isBlank() -> "Title cannot be empty"
            title.trim().length > MAX_TITLE_LENGTH -> "Title must be less than $MAX_TITLE_LENGTH characters"
            else -> null
        }

    fun validateAuthor(author: String): String? =
        when {
            author.isBlank() -> "Author cannot be empty"
            author.trim().length > MAX_AUTHOR_LENGTH -> "Author must be less than $MAX_AUTHOR_LENGTH characters"
            else -> null
        }

    fun validateIsbn(isbn: String): String? = when {
        isbn.isBlank() -> "ISBN cannot be empty"
        !isbn.all { it.isDigit() || it.isLetter() || it == '-' || it == ' '} -> "ISBN contains invalid characters"
        normaliseIsbn(isbn).length != 10 && normaliseIsbn(isbn).length != 13 -> "ISBN must have 10 or 13 digits"
        else -> null
    }
}