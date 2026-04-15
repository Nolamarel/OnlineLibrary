package com.nolamarel.onlinelibrary.network

data class AdminUpdateReviewRequest(
    val rating: Int,
    val comment: String? = null,
    val status: String
)