package com.xe.propertyad.data

import retrofit2.http.GET
import retrofit2.http.Query

data class Suggestion(
    val placeId: String,
    val mainText: String,
    val secondaryText: String
)

interface ApiService {

    @GET("/")
    suspend fun getSuggestions(
        @Query("input") input: String
    ): List<Suggestion>
}
