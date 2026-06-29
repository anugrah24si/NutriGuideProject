package com.example.nutriguideproject.data.model

import org.json.JSONObject

/**
 * Profil pengguna — selaras dengan tabel public.profiles (untuk Daftar User admin).
 */
data class Profile(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String
) {
    val isAdmin: Boolean get() = role == "admin"

    companion object {
        fun fromJson(o: JSONObject): Profile = Profile(
            id = o.optString("id"),
            fullName = o.optString("full_name").ifBlank { "(Tanpa nama)" },
            email = o.optString("email"),
            role = o.optString("role", "member")
        )
    }
}
