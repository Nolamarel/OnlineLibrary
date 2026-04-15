package com.nolamarel.onlinelibrary.network

data class AdminUpdateUserRequest(
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean
)