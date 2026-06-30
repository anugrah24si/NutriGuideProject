package com.example.nutriguideproject.data.model

import org.json.JSONObject

/**
 * Profil lengkap milik user (tabel public.profiles) untuk dibaca & diedit sendiri.
 * Field angka nullable karena bisa belum diisi.
 */
data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String,
    val age: Int?,
    val weightKg: Double?,
    val heightCm: Double?,
    val activityLevel: String,
    val goal: String,
    val dailyCalorieTarget: Int?
) {
    /** JSON untuk update (hanya field yang boleh diubah user). */
    fun toUpdateJson(): JSONObject = JSONObject()
        .put("full_name", fullName)
        .put("age", age ?: JSONObject.NULL)
        .put("weight_kg", weightKg ?: JSONObject.NULL)
        .put("height_cm", heightCm ?: JSONObject.NULL)
        .put("activity_level", activityLevel)
        .put("goal", goal)
        .put("daily_calorie_target", dailyCalorieTarget ?: JSONObject.NULL)

    companion object {
        fun fromJson(o: JSONObject): UserProfile = UserProfile(
            id = o.optString("id"),
            fullName = o.optString("full_name").ifBlank { "" },
            email = o.optString("email"),
            role = o.optString("role", "member"),
            age = if (o.isNull("age")) null else o.optInt("age"),
            weightKg = if (o.isNull("weight_kg")) null else o.optDouble("weight_kg"),
            heightCm = if (o.isNull("height_cm")) null else o.optDouble("height_cm"),
            activityLevel = o.optString("activity_level").ifBlank { "" },
            goal = o.optString("goal").ifBlank { "" },
            dailyCalorieTarget = if (o.isNull("daily_calorie_target")) null else o.optInt("daily_calorie_target")
        )
    }
}
