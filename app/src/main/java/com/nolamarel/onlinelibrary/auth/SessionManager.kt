package com.nolamarel.onlinelibrary.auth

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun saveUserId(userId: Long) {
        prefs.edit().putLong("userId", userId).apply()
    }

    fun getUserId(): Long {
        return prefs.getLong("userId", -1L)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}