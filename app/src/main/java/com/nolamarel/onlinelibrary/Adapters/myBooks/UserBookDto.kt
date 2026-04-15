package com.nolamarel.onlinelibrary.Adapters.myBooks

data class UserBookDto(
    val userId: Long,
    val bookId: Long,
    val externalId: String?,
    val status: String,
    val progress: String,
    val addedAt: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val localFilePath: String?,
    val currentPage: Int?
)