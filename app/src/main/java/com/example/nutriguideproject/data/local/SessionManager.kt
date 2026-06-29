package com.example.nutriguideproject.data.local

import android.content.Context

/**
 * Menyimpan sesi login (token, id user, role, email) di SharedPreferences.
 */
class SessionManager(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("nutriguide_session", Context.MODE_PRIVATE)

    fun save(userId: String, accessToken: String, role: String, email: String) {
        prefs.edit()
            .putString(KEY_UID, userId)
            .putString(KEY_TOKEN, accessToken)
            .putString(KEY_ROLE, role)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    val userId: String? get() = prefs.getString(KEY_UID, null)
    val accessToken: String? get() = prefs.getString(KEY_TOKEN, null)
    val role: String? get() = prefs.getString(KEY_ROLE, null)
    val email: String? get() = prefs.getString(KEY_EMAIL, null)

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()
    fun isAdmin(): Boolean = role == "admin"

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_UID = "uid"
        private const val KEY_TOKEN = "token"
        private const val KEY_ROLE = "role"
        private const val KEY_EMAIL = "email"
    }
}
