package com.chordio.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class AppSettingsPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_FONT_SIZE = "font_size"
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun setLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, "greek") ?: "greek"
    }

    fun setFontSize(size: Float) {
        sharedPreferences.edit().putFloat(KEY_FONT_SIZE, size).apply()
    }

    fun getFontSize(): Float {
        return sharedPreferences.getFloat(KEY_FONT_SIZE, 16f)
    }
}