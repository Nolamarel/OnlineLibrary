package com.nolamarel.onlinelibrary;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocalizationManager {
    private String currentLanguage;
    private Context context;

    public LocalizationManager(Context context) {
        this.context = context;
        currentLanguage = Locale.getDefault().getLanguage(); // Получить текущий язык по умолчанию
    }

    public void setLanguage(String language) {
        currentLanguage = language;
        updateLanguage();
    }

    public String getString(int resId) {
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(new Locale(currentLanguage));
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return resources.getString(resId);
    }

    public void updateLanguage() {
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(new Locale(currentLanguage));
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }
}
