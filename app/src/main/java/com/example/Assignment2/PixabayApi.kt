package com.example.Assignment2

import retrofit2.http.GET
import retrofit2.http.Query

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface PixabayApi {
    @GET("api/")
    suspend fun searchImages(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("image_type") imageType: String = "photo",
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1
    ): PixabayResponse
}

@JsonClass(generateAdapter = true)
data class PixabayResponse(
    val total: Int,
    val totalHits: Int,
    val hits: List<Hit>
)

@JsonClass(generateAdapter = true)
data class Hit(
    val id: Int,
    @Json(name = "previewURL") val previewUrl: String,
    @Json(name = "webformatURL") val webUrl: String,
    @Json(name = "largeImageURL") val largeUrl: String,
    val tags: String
)

