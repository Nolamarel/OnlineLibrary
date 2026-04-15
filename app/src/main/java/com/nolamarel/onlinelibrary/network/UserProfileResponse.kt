package com.nolamarel.onlinelibrary.network

data class UserProfileResponse(
    val userId: Long,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)