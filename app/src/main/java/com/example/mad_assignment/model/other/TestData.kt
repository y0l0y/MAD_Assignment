package com.example.mad_assignment.model.other

object TestData {
    data class SeedBook(
        val title: String,
        val author: String,
        val isbn: String,
        val category: String? = null,
        val borrower: String? = null
    )
    val categories = listOf(
        "Programming",
        "Software Engineering",
        "Mobile Development"
    )
    val initialBooks = listOf(
        SeedBook(
            title = "Clean Code",
            author = "Robert C. Martin",
            isbn = "978-0132350884",
            category = "Software Engineering"
        ),
        SeedBook(
            title = "Kotlin in Action",
            author = "Dmitry Jemerov & Svetlana Isakova",
            isbn = "978-1617293290",
            category = "Programming"
        ),
        SeedBook(
            title = "Android Programming: The Big Nerd Ranch Guide",
            author = "Bill Phillips, Chris Stewart & Kristin Marsicano",
            isbn = "978-0135245218",
            category = "Mobile Development",
            borrower = "John Doe"
        ),
        SeedBook(
            title = "Design Patterns",
            author = "Erich Gamma, Richard Helm, Ralph Johnson & John Vlissides",
            isbn = "978-0201633610",
            category = "Software Engineering"
        )
    )
}