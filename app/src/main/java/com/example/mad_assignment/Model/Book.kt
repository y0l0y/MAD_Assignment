package com.example.mad_assignment.Model

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("title")
    val title: String,
    @SerializedName("author_name")
    val authorName: List<String>? = null
)

data class OpenLibraryResponse(
    @SerializedName("docs")
    val docs: List<Book>
)