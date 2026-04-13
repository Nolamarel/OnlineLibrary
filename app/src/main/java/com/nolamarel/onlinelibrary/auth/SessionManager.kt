package com.nolamarel.onlinelibrary.auth

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}