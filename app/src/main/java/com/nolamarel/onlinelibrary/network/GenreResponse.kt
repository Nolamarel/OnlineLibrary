package com.nolamarel.onlinelibrary.network

data class GenreResponse(
    val genreId: Long,
    val name: String,
    val imageUrl: String?
)