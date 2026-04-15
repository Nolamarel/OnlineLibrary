package com.nolamarel.onlinelibrary.network

data class CreateReviewRequest(
    val rating: Int,
    val comment: String?
)