package com.nolamarel.onlinelibrary.network

data class ReviewResponse(
    val reviewId: Long,
    val userId: Long,
    val userName: String,
    val bookId: Long,
    val rating: Int,
    val comment: String?,
    val status: String,
    val createdAt: String
)