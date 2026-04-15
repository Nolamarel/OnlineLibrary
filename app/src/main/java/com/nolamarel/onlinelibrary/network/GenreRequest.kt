package com.nolamarel.onlinelibrary.network

data class GenreRequest(
    val name: String,
    val imageUrl: String? = null
)