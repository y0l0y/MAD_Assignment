package com.example.mad_assignment.Network

import com.example.mad_assignment.Model.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BookApi {
    @GET("search.json")
    suspend fun searchBooks(@Query("q") query: String): OpenLibraryResponse
}