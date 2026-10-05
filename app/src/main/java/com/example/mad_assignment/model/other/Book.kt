package com.example.mad_assignment.model.other

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("author_name")
    val authorName: List<String>? = null,
    @SerializedName("cover_i")
    val coverId: Long? = null
)

data class OpenLibraryResponse(
    @SerializedName("docs")
    val docs: List<Book>? = null
)

data class OnlineBook(
    val title: String,
    val authors: String,
    val coverUrl: String?
)

fun Book.toOnlineBook(): OnlineBook? {
    val cleanTitle = title?.trim()
    if (cleanTitle.isNullOrEmpty()) return null
    val authors = authorName
        ?.filter { it.isNotBlank() }
        ?.joinToString()
        ?.takeIf { it.isNotBlank() }
        ?: "Unknown author"
    return OnlineBook(
        title = cleanTitle,
        authors = authors,
        coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
    )
}