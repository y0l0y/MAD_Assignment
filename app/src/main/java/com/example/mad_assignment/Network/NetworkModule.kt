package com.example.mad_assignment.Network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/* get the data from the baseUrl then using Gson to convert the json to OnlineBook data class */
object NetworkModule {
    val api: BookApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://openlibrary.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApi::class.java)
    }
}