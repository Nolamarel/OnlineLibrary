package com.nolamarel.onlinelibrary

data class UserResponse(
    val userId: Long,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)