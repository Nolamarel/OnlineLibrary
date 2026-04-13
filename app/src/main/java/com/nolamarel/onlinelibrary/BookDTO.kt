package com.nolamarel.onlinelibrary

data class BookDTO(
    val id: String,
    val author: String,
    val title: String,
    val image: String?,
    val description: String?
)
