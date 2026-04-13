package com.nolamarel.onlinelibrary

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class LocalizationManager(private val context: Context) {
    var currentLanguage: String
        private set

    init {
        currentLanguage = Locale.getDefault().language // Получить текущий язык по умолчанию
    }

    fun setLanguage(language: String) {
        currentLanguage = language
        updateLanguage()
    }

    fun getString(resId: Int): String {
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(Locale(currentLanguage))
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return resources.getString(resId)
    }

    fun updateLanguage() {
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(Locale(currentLanguage))
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}
