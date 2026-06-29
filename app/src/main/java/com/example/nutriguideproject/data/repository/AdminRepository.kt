package com.example.nutriguideproject.data.repository

import android.os.Handler
import android.os.Looper
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.Profile
import com.example.nutriguideproject.data.remote.SupabaseClient
import okhttp3.Request
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors

/** Ringkasan statistik untuk Admin Dashboard. */
data class AdminStats(
    val totalUsers: Int,
    val totalFoods: Int,
    val totalArticles: Int,
    val todayLogs: Int
)

/**
 * Data agregat & daftar user untuk area Admin (PostgREST).
 * Memerlukan sesi admin (RLS membatasi akses non-admin).
 */
class AdminRepository(private val session: SessionManager) {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val rest get() = "${SupabaseClient.baseUrl}${SupabaseClient.REST}"

    fun getStats(callback: (Result<AdminStats>) -> Unit) = runAsync(callback) {
        val users = count("$rest/profiles?select=id")
        val foods = count("$rest/foods?select=id")
        val articles = count("$rest/articles?select=id")
        val logs = count("$rest/food_logs?select=id&logged_at=gte.${startOfTodayUtcIso()}")
        AdminStats(users, foods, articles, logs)
    }

    fun getUsers(callback: (Result<List<Profile>>) -> Unit) = runAsync(callback) {
        val request = baseRequest("$rest/profiles?select=id,full_name,email,role&order=role.asc")
            .get().build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException("Gagal memuat daftar user")
        val arr = JSONArray(body)
        (0 until arr.length()).map { Profile.fromJson(arr.getJSONObject(it)) }
    }

    // --- helper ---

    private fun count(url: String): Int {
        val (code, body) = execute(baseRequest(url).get().build())
        if (code !in 200..299) return 0
        return JSONArray(body).length()
    }

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
    }

    private fun execute(request: Request): Pair<Int, String> {
        SupabaseClient.http.newCall(request).execute().use { resp ->
            return resp.code to (resp.body?.string() ?: "")
        }
    }

    private fun <T> runAsync(callback: (Result<T>) -> Unit, work: () -> T) {
        io.execute {
            val result = runCatching { work() }
            main.post { callback(result) }
        }
    }
}
