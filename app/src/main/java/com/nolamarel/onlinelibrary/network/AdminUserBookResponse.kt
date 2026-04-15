package com.nolamarel.onlinelibrary.network

data class AdminUserBookResponse(
    val userId: Long,
    val userName: String,
    val bookId: Long,
    val bookTitle: String,
    val status: String,
    val progress: String,
    val addedAt: String,
    val localFilePath: String?,
    val currentPage: Int?
)