package com.example.mad_assignment.network

import com.example.mad_assignment.model.other.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BookApi {
    @GET("search.json")
    suspend fun searchBooks(@Query("q") query: String): OpenLibraryResponse
}