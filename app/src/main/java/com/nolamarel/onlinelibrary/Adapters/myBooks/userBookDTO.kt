package com.nolamarel.onlinelibrary.Adapters.myBooks

data class UserBookDTO(
    val bookId: String,
    val title: String,
    val author: String,
    val image: String,
    val description: String,
    val localPath: String? = null
)