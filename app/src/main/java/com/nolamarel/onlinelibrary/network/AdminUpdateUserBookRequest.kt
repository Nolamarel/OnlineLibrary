package com.nolamarel.onlinelibrary.network

data class AdminUpdateUserBookRequest(
    val status: String,
    val progress: String,
    val currentPage: Int?
)