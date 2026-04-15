package com.nolamarel.onlinelibrary.network

data class UpdateProgressRequest(
    val progress: String,
    val currentPage: Int,
    val locator: String?,
    val localFilePath: String?,
    val fileFormat: String?
)