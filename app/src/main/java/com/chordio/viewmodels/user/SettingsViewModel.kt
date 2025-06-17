package com.chordio.viewmodels.user


import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.chordio.sharedpreferences.AppSettingsPreferences

class SettingsViewModel(private val appSettingsPreferences: AppSettingsPreferences) : ViewModel() {
    val darkMode = mutableStateOf(appSettingsPreferences.isDarkMode())
    private val language = mutableStateOf(appSettingsPreferences.getLanguage())
    val fontSize = mutableFloatStateOf(appSettingsPreferences.getFontSize())

    var onThemeChange: (() -> Unit)? = null // Callback για το Activity

    fun toggleDarkMode() {
        darkMode.value = !darkMode.value
        appSettingsPreferences.setDarkMode(darkMode.value)

        onThemeChange?.invoke()
    }

    fun changeLanguage(newLanguage: String) {
        language.value = newLanguage
        appSettingsPreferences.setLanguage(newLanguage)
    }

    fun changeFontSize(newSize: Float) {
        fontSize.floatValue = newSize
        appSettingsPreferences.setFontSize(newSize)
    }

}
