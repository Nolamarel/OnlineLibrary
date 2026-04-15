package com.nolamarel.onlinelibrary.network

data class ChangePasswordRequest(
    val email: String,
    val oldPassword: String,
    val newPassword: String
)