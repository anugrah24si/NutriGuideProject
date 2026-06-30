package com.example.nutriguideproject.data.repository

import android.os.Handler
import android.os.Looper
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.UserProfile
import com.example.nutriguideproject.data.remote.SupabaseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * Baca & ubah profil milik user sendiri (tabel public.profiles).
 * RLS hanya mengizinkan akses baris milik sendiri.
 */
class ProfileRepository(private val session: SessionManager) {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val jsonType = "application/json".toMediaType()
    private val endpoint get() = "${SupabaseClient.baseUrl}${SupabaseClient.REST}/profiles"

    fun getMyProfile(callback: (Result<UserProfile>) -> Unit) = runAsync(callback) {
        val uid = session.userId ?: throw IllegalStateException("Sesi tidak ditemukan.")
        val request = baseRequest("$endpoint?id=eq.$uid&select=*").get().build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal memuat profil"))
        val arr = JSONArray(body)
        if (arr.length() == 0) throw RuntimeException("Profil tidak ditemukan")
        UserProfile.fromJson(arr.getJSONObject(0))
    }

    fun updateMyProfile(profile: UserProfile, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val uid = session.userId ?: throw IllegalStateException("Sesi tidak ditemukan.")
        val request = baseRequest("$endpoint?id=eq.$uid")
            .addHeader("Prefer", "return=minimal")
            .patch(profile.toUpdateJson().toString().toRequestBody(jsonType))
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal menyimpan profil"))
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
        JSONObject(body).optString("message").ifBlank { fallback }
    }.getOrDefault(fallback).ifBlank { fallback }

    private fun <T> runAsync(callback: (Result<T>) -> Unit, work: () -> T) {
        io.execute {
            val result = runCatching { work() }
            main.post { callback(result) }
        }
    }
}
