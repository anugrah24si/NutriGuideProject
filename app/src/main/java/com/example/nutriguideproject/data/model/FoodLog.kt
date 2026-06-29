package com.example.nutriguideproject.data.model

import org.json.JSONObject

/**
 * Catatan makanan harian milik user — selaras dengan tabel public.food_logs.
 * Nilai gizi disimpan sebagai snapshot saat dicatat.
 */
data class FoodLog(
    val id: String? = null,
    val userId: String,
    val foodId: String? = null,
    val mealType: String,
    val name: String,
    val portion: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val loggedAt: String? = null
) {
    fun toJson(): JSONObject {
        val o = JSONObject()
            .put("user_id", userId)
            .put("meal_type", mealType)
            .put("name", name)
            .put("portion", portion)
            .put("calories", calories)
            .put("protein_g", proteinG)
            .put("carbs_g", carbsG)
            .put("fat_g", fatG)
        if (foodId != null) o.put("food_id", foodId)
        return o
    }

    companion object {
        fun fromJson(o: JSONObject): FoodLog = FoodLog(
            id = o.optString("id").ifBlank { null },
            userId = o.optString("user_id"),
            foodId = o.optString("food_id").ifBlank { null },
            mealType = o.optString("meal_type"),
            name = o.optString("name"),
            portion = o.optString("portion"),
            calories = o.optInt("calories", 0),
            proteinG = o.optDouble("protein_g", 0.0),
            carbsG = o.optDouble("carbs_g", 0.0),
            fatG = o.optDouble("fat_g", 0.0),
            loggedAt = o.optString("logged_at").ifBlank { null }
        )
    }
}
