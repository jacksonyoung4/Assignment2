package com.example.Assignment2

import retrofit2.http.GET
import retrofit2.http.Query

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30,
        @Query("page") page: Int = 1
    ): OpenLibraryResponse
}

@JsonClass(generateAdapter = true)
data class OpenLibraryResponse(
    val numFound: Int,
    val start: Int,
    val docs: List<BookDoc>
)

@JsonClass(generateAdapter = true)
data class BookDoc(
    val key: String?,
    val title: String?,
    @Json(name = "author_name") val authors: List<String>?,
    @Json(name = "first_publish_year") val firstPublishYear: Int?,
    @Json(name = "cover_i") val coverId: Int?
) {
    val coverUrl: String?
        get() = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
}

