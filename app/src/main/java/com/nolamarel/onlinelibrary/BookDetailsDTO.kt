package com.nolamarel.onlinelibrary

data class BookDetailsDTO(
    val id: String,
    val title: String,
    val author: String,
    val image: String,
    val description: String,
    val genreId: String
)
