package com.example.cscalc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {
    private static final String PREFERENCES = "app_settings";
    private static final String DARK_THEME_KEY = "dark_theme";

    private ThemeManager() {
    }

    public static boolean isDarkTheme(Context context) {
        return preferences(context).getBoolean(DARK_THEME_KEY, false);
    }

    public static void applySavedMode(Context context) {
        setMode(isDarkTheme(context));
    }

    public static void setDarkTheme(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(DARK_THEME_KEY, enabled).apply();
        setMode(enabled);
    }

    private static void setMode(boolean darkTheme) {
        AppCompatDelegate.setDefaultNightMode(darkTheme
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
}
