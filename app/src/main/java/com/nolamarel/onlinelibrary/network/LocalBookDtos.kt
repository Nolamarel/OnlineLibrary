package com.nolamarel.onlinelibrary.network

data class CreateLocalBookRequest(
    val title: String,
    val author: String,
    val description: String? = null,
    val coverUrl: String? = null
)

data class CreateLocalBookResponse(
    val bookId: Long,
    val title: String,
    val author: String
)