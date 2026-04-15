package com.nolamarel.onlinelibrary.network

data class MyBookResponse(
    val bookId: Long,
    val externalId: String?,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val status: String,
    val progress: String?,
    val localFilePath: String?,
    val fileFormat: String?,
    val currentPage: Int?
)