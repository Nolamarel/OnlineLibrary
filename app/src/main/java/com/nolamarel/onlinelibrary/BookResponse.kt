package com.nolamarel.onlinelibrary

data class BookResponse(
    val bookId: Long,
    val externalId: String?,
    val genreId: Long?,
    val title: String,
    val author: String,
    val description: String?,
    val coverUrl: String?,
    val isbn: String?,
    val source: String,
    val publishedYear: Int?,
    val pageCount: Int?,
    val language: String?
)