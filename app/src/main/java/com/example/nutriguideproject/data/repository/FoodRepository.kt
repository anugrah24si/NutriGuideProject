package com.example.nutriguideproject.data.repository

import android.os.Handler
import android.os.Looper
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.Food
import com.example.nutriguideproject.data.remote.SupabaseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * CRUD data makanan (tabel public.foods) via Supabase PostgREST.
 * Membaca butuh user login (RLS: authenticated); menulis butuh admin (RLS: is_admin()).
 */
class FoodRepository(private val session: SessionManager) {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val jsonType = "application/json".toMediaType()
    private val endpoint get() = "${SupabaseClient.baseUrl}${SupabaseClient.REST}/foods"

    fun getFoods(callback: (Result<List<Food>>) -> Unit) = runAsync(callback) {
        val request = baseRequest("$endpoint?select=*&order=name.asc").get().build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal memuat data makanan"))
        val arr = JSONArray(body)
        (0 until arr.length()).map { Food.fromJson(arr.getJSONObject(it)) }
    }

    fun addFood(food: Food, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val request = baseRequest(endpoint)
            .addHeader("Prefer", "return=minimal")
            .post(food.toJson().toString().toRequestBody(jsonType))
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal menambah makanan"))
    }

    fun updateFood(food: Food, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val id = food.id ?: throw IllegalArgumentException("ID makanan kosong")
        val request = baseRequest("$endpoint?id=eq.$id")
            .addHeader("Prefer", "return=minimal")
            .patch(food.toJson().toString().toRequestBody(jsonType))
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal mengubah makanan"))
    }

    fun deleteFood(id: String, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val request = baseRequest("$endpoint?id=eq.$id")
            .addHeader("Prefer", "return=minimal")
            .delete()
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal menghapus makanan"))
    }

    // --- helper ---

    private fun baseRequest(url: String): Request.Builder {
        if (!SupabaseClient.isConfigured()) {
            throw IllegalStateException("Supabase belum dikonfigurasi (cek local.properties).")
        }
        val token = session.accessToken
            ?: throw IllegalStateException("Sesi tidak ditemukan. Silakan login ulang.")
        return Request.Builder()
            .url(url)
            .addHeader("apikey", SupabaseClient.anonKey)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
    }

    private fun execute(request: Request): Pair<Int, String> {
        SupabaseClient.http.newCall(request).execute().use { resp ->
            return resp.code to (resp.body?.string() ?: "")
        }
    }

    private fun parseError(body: String, fallback: String): String = runCatching {
        val o = JSONObject(body)
        o.optString("message").ifBlank { o.optString("hint").ifBlank { fallback } }
    }.getOrDefault(fallback).ifBlank { fallback }

    private fun <T> runAsync(callback: (Result<T>) -> Unit, work: () -> T) {
        io.execute {
            val result = runCatching { work() }
            main.post { callback(result) }
        }
    }
}
