package com.nolamarel.onlinelibrary

data class UserDTO(
    val id: String? = null,
    val login: String,
    val password: String,
    val username: String,
    val email: String?
)