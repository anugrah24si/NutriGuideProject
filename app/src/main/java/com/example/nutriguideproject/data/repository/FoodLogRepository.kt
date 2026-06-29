package com.example.nutriguideproject.data.repository

import android.os.Handler
import android.os.Looper
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.FoodLog
import com.example.nutriguideproject.data.remote.SupabaseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors

/**
 * CRUD catatan makanan harian (tabel public.food_logs) via PostgREST.
 * Tiap user hanya bisa mengakses miliknya sendiri (dijaga RLS).
 */
class FoodLogRepository(private val session: SessionManager) {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val jsonType = "application/json".toMediaType()
    private val endpoint get() = "${SupabaseClient.baseUrl}${SupabaseClient.REST}/food_logs"

    fun addLog(log: FoodLog, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val request = baseRequest(endpoint)
            .addHeader("Prefer", "return=minimal")
            .post(log.toJson().toString().toRequestBody(jsonType))
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal menyimpan log"))
    }

    /** Ambil log milik user untuk hari ini (waktu lokal), urut terbaru dulu. */
    fun getTodayLogs(callback: (Result<List<FoodLog>>) -> Unit) = runAsync(callback) {
        val uid = session.userId ?: throw IllegalStateException("Sesi tidak ditemukan.")
        val startIso = startOfTodayUtcIso()
        val url = "$endpoint?user_id=eq.$uid&logged_at=gte.$startIso&order=logged_at.desc"
        val request = baseRequest(url).get().build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal memuat log"))
        val arr = JSONArray(body)
        (0 until arr.length()).map { FoodLog.fromJson(arr.getJSONObject(it)) }
    }

    // --- helper ---

    private fun startOfTodayUtcIso(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(cal.timeInMillis))
    }

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
