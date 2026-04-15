package com.nolamarel.onlinelibrary.network

data class AdminBookRequest(
    val genreId: Long? = null,
    val title: String,
    val author: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val isbn: String? = null,
    val source: String = "manual",
    val publishedYear: Int? = null,
    val pageCount: Int? = null,
    val language: String? = null
)