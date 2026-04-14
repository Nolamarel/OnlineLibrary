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

    fun saveLastOpenedBook(
        bookId: Long,
        title: String?,
        currentPage: Int,
        localPath: String
    ) {
        prefs.edit()
            .putLong("last_book_id", bookId)
            .putString("last_book_title", title)
            .putInt("last_book_page", currentPage)
            .putString("last_book_path", localPath)
            .apply()
    }

    fun getLastBookId(): Long = prefs.getLong("last_book_id", -1L)

    fun getLastBookTitle(): String? = prefs.getString("last_book_title", null)

    fun getLastBookPage(): Int = prefs.getInt("last_book_page", 0)

    fun getLastBookPath(): String? = prefs.getString("last_book_path", null)
}