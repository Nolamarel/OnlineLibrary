package com.nolamarel.onlinelibrary.network

data class BookReviewsResponse(
    val averageRating: Double,
    val reviewsCount: Int,
    val reviews: List<ReviewResponse>
)