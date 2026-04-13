package com.nolamarel.onlinelibrary

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val login: String,
    val password: String
)

data class RegisterRequest(
    val login: String,
    val password: String,
    val userName: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    @SerializedName("id") val userId: String
)