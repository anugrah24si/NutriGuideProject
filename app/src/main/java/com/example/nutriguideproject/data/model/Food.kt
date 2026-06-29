package com.example.nutriguideproject.data.model

import org.json.JSONObject

/**
 * Model data makanan (master) — selaras dengan tabel public.foods.
 * id null saat membuat data baru (di-generate database).
 */
data class Food(
    val id: String? = null,
    val name: String,
    val category: String,
    val serving: String,
    val calories: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double
) {
    /** JSON untuk insert/update (tanpa id; id dikelola database). */
    fun toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("category", category)
        .put("serving", serving)
        .put("calories", calories)
        .put("protein_g", proteinG)
        .put("carbs_g", carbsG)
        .put("fat_g", fatG)
        .put("fiber_g", fiberG)

    companion object {
        fun fromJson(o: JSONObject): Food = Food(
            id = o.optString("id").ifBlank { null },
            name = o.optString("name"),
            category = o.optString("category"),
            serving = o.optString("serving"),
            calories = o.optInt("calories", 0),
            proteinG = o.optDouble("protein_g", 0.0),
            carbsG = o.optDouble("carbs_g", 0.0),
            fatG = o.optDouble("fat_g", 0.0),
            fiberG = o.optDouble("fiber_g", 0.0)
        )
    }
}
