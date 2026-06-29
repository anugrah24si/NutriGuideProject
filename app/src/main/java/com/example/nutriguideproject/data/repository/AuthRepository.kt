package com.example.nutriguideproject.data.repository

import android.os.Handler
import android.os.Looper
import com.example.nutriguideproject.data.remote.SupabaseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

/** Hasil autentikasi yang dipakai UI. */
data class AuthUser(
    val userId: String,
    val accessToken: String,
    val role: String,
    val email: String
)

/**
 * Repository autentikasi berbasis Supabase Auth (GoTrue) + PostgREST.
 * Memakai OkHttp + org.json (tanpa library tambahan).
 * Operasi jaringan berjalan di background thread; callback dikembalikan ke main thread.
 */
class AuthRepository {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val jsonType = "application/json".toMediaType()

    /** Login dengan email & password, lalu ambil role dari tabel profiles. */
    fun signIn(email: String, password: String, callback: (Result<AuthUser>) -> Unit) =
        runAsync(callback) {
            requireConfigured()
            val payload = JSONObject().put("email", email).put("password", password)
            val request = Request.Builder()
                .url("${SupabaseClient.baseUrl}${SupabaseClient.AUTH}/token?grant_type=password")
                .addHeader("apikey", SupabaseClient.anonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonType))
                .build()

            val (code, body) = execute(request)
            if (code !in 200..299) throw RuntimeException(parseError(body, "Login gagal"))

            val obj = JSONObject(body)
            val token = obj.getString("access_token")
            val uid = obj.getJSONObject("user").getString("id")
            val role = fetchRole(uid, token)
            AuthUser(uid, token, role, email)
        }

    /** Daftar akun baru (otomatis role = member). */
    fun signUp(fullName: String, email: String, password: String, callback: (Result<AuthUser>) -> Unit) =
        runAsync(callback) {
            requireConfigured()
            val payload = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("data", JSONObject().put("full_name", fullName))
            val request = Request.Builder()
                .url("${SupabaseClient.baseUrl}${SupabaseClient.AUTH}/signup")
                .addHeader("apikey", SupabaseClient.anonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody(jsonType))
                .build()

            val (code, body) = execute(request)
            if (code !in 200..299) throw RuntimeException(parseError(body, "Pendaftaran gagal"))

            val obj = JSONObject(body)
            // Jika konfirmasi email dimatikan, signup langsung memberi session.
            val token = obj.optString("access_token", "")
            if (token.isNotBlank()) {
                val uid = obj.getJSONObject("user").getString("id")
                val role = runCatching { fetchRole(uid, token) }.getOrDefault("member")
                AuthUser(uid, token, role, email)
            } else {
                // Tidak ada session (mis. perlu verifikasi) → coba login langsung.
                signInBlocking(email, password)
            }
        }

    // --- helper ---

    private fun signInBlocking(email: String, password: String): AuthUser {
        val payload = JSONObject().put("email", email).put("password", password)
        val request = Request.Builder()
            .url("${SupabaseClient.baseUrl}${SupabaseClient.AUTH}/token?grant_type=password")
            .addHeader("apikey", SupabaseClient.anonKey)
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody(jsonType))
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Login gagal"))
        val obj = JSONObject(body)
        val token = obj.getString("access_token")
        val uid = obj.getJSONObject("user").getString("id")
        val role = fetchRole(uid, token)
        return AuthUser(uid, token, role, email)
    }

    private fun fetchRole(userId: String, accessToken: String): String {
        val request = Request.Builder()
            .url("${SupabaseClient.baseUrl}${SupabaseClient.REST}/profiles?select=role&id=eq.$userId")
            .addHeader("apikey", SupabaseClient.anonKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) return "member"
        val arr = JSONArray(body)
        return if (arr.length() > 0) arr.getJSONObject(0).optString("role", "member") else "member"
    }

    private fun execute(request: Request): Pair<Int, String> {
        SupabaseClient.http.newCall(request).execute().use { resp ->
            return resp.code to (resp.body?.string() ?: "")
        }
    }

    private fun requireConfigured() {
        if (!SupabaseClient.isConfigured()) {
            throw IllegalStateException(
                "Supabase belum dikonfigurasi. Isi SUPABASE_URL & SUPABASE_ANON_KEY di local.properties."
            )
        }
    }

    private fun parseError(body: String, fallback: String): String {
        return runCatching {
            val o = JSONObject(body)
            o.optString("error_description").ifBlank {
                o.optString("msg").ifBlank {
                    o.optString("message").ifBlank {
                        o.optString("error", fallback)
                    }
                }
            }
        }.getOrDefault(fallback).ifBlank { fallback }
    }

    private fun <T> runAsync(callback: (Result<T>) -> Unit, work: () -> T) {
        io.execute {
            val result = runCatching { work() }
            main.post { callback(result) }
        }
    }
}
