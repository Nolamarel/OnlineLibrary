package com.nolamarel.onlinelibrary

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = false

    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext // ← Важно!

        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        darkTheme = preferences.getBoolean("dark_theme", false)

        AppCompatDelegate.setDefaultNightMode(
            if (darkTheme)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.edit().putBoolean("dark_theme", darkTheme).apply()

        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}