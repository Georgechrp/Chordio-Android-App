package com.chordio.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

class UserProfilePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_EMAIL = "email"
    }

    fun setUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(): String = sharedPreferences.getString(KEY_USERNAME, "Guest") ?: "Guest"

    fun setAvatarUrl(url: String) {
        sharedPreferences.edit().putString(KEY_AVATAR_URL, url).apply()
    }

    fun getAvatarUrl(): String? = sharedPreferences.getString(KEY_AVATAR_URL, null)

    fun setEmail(email: String) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String? = sharedPreferences.getString(KEY_EMAIL, null)
}
