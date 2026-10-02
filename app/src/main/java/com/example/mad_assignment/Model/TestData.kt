package com.example.mad_assignment.Model

object TestData {
    val initialBooks = listOf(
        BookEntity(
            title = "Clean Code",
            author = "Robert C. Martin",
            isbn = "978-0132350884",
            isAvailable = true,
            coverPath = null
        ),
        BookEntity(
            title = "Kotlin in Action",
            author = "Dmitry Jemerov & Svetlana Isakova",
            isbn = "978-1617293290",
            isAvailable = true,
            coverPath = null
        ),
        BookEntity(
            title = "Android Programming: The Big Nerd Ranch Guide",
            author = "Bill Phillips, Chris Stewart & Kristin Marsicano",
            isbn = "978-0135245218",
            isAvailable = false,
            borrowedBy = "John Doe",
            coverPath = null
        ),
        BookEntity(
            title = "Design Patterns",
            author = "Erich Gamma, Richard Helm, Ralph Johnson & John Vlissides",
            isbn = "978-0201633610",
            isAvailable = true,
            coverPath = null
        )
    )
}