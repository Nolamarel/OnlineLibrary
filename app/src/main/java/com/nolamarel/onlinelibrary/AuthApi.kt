package com.nolamarel.onlinelibrary

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>
}

