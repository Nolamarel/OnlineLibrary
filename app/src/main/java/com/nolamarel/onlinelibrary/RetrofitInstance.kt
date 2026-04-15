package com.nolamarel.onlinelibrary

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val userIdProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = tokenProvider()
        val userId = userIdProvider()
        val requestBuilder = chain.request().newBuilder()

        Log.d("AuthInterceptor", "Using userId: $userId")
        Log.d("AuthInterceptor", "Using token: $token")


        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        if (!userId.isNullOrEmpty()) {
            requestBuilder.addHeader("X-User-Id", userId)
        }

        return chain.proceed(requestBuilder.build())
    }
}


object RetrofitInstance {

    private val authInterceptor = AuthInterceptor(
        tokenProvider = {
            App.context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("token", null)
        },
        userIdProvider = {
            App.context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("userId", null)
        }
    )

    private val serverClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: GoogleBooksApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://www.googleapis.com/books/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleBooksApi::class.java)
    }

    val serverApi: ServerApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(serverClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServerApi::class.java)
    }
}

