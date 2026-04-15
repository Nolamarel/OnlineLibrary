package com.nolamarel.onlinelibrary.network

data class SearchBookResponse(
    val bookId: Long?,
    val externalId: String?,
    val title: String,
    val author: String,
    val description: String?,
    val coverUrl: String?,
    val source: String
)