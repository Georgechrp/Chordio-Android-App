package com.chordio.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class SessionPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_LAST_LOGIN = "last_login"
    }

    fun setAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? = sharedPreferences.getString(KEY_AUTH_TOKEN, null)

    fun clearAuthToken() {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    fun setLastLogin(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_LOGIN, timestamp).apply()
    }

    fun getLastLogin(): Long = sharedPreferences.getLong(KEY_LAST_LOGIN, 0)
}
