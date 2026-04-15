package com.nolamarel.onlinelibrary.network

data class SearchBookResponse(
    val externalId: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val description: String?
)