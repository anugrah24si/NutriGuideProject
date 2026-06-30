package com.example.nutriguideproject.data.local

import android.content.Context

/**
 * Menyimpan sesi login (token, id user, role, email, nama) di SharedPreferences.
 */
class SessionManager(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("nutriguide_session", Context.MODE_PRIVATE)

    fun save(userId: String, accessToken: String, role: String, email: String, fullName: String) {
        prefs.edit()
            .putString(KEY_UID, userId)
            .putString(KEY_TOKEN, accessToken)
            .putString(KEY_ROLE, role)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, fullName)
            .apply()
    }

    val userId: String? get() = prefs.getString(KEY_UID, null)
    val accessToken: String? get() = prefs.getString(KEY_TOKEN, null)
    val role: String? get() = prefs.getString(KEY_ROLE, null)
    val email: String? get() = prefs.getString(KEY_EMAIL, null)
    val fullName: String? get() = prefs.getString(KEY_NAME, null)

    /** Nama tampilan: pakai full_name; jika kosong, pakai bagian sebelum @ dari email. */
    fun displayName(): String {
        val name = fullName?.trim()
        if (!name.isNullOrBlank()) return name
        val mail = email ?: return "Pengguna"
        return mail.substringBefore("@").replaceFirstChar { it.uppercase() }
    }

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()
    fun isAdmin(): Boolean = role == "admin"

    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_UID = "uid"
        private const val KEY_TOKEN = "token"
        private const val KEY_ROLE = "role"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
    }
}
