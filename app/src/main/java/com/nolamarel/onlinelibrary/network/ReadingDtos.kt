package com.nolamarel.onlinelibrary.network

data class UpdateProgressRequest(
    val progress: String,
    val currentPage: Int? = null,
    val locator: String? = null,
    val localFilePath: String? = null,
    val fileFormat: String? = null
)

data class MessageResponse(
    val message: String
)