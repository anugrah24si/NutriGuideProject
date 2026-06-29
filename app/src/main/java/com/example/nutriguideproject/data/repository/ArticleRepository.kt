package com.example.nutriguideproject.data.repository

import android.os.Handler
import android.os.Looper
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.Article
import com.example.nutriguideproject.data.remote.SupabaseClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * CRUD artikel edukasi (tabel public.articles) via PostgREST.
 * Baca: member hanya artikel published; admin semua. Tulis: admin saja (RLS).
 */
class ArticleRepository(private val session: SessionManager) {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val jsonType = "application/json".toMediaType()
    private val endpoint get() = "${SupabaseClient.baseUrl}${SupabaseClient.REST}/articles"

    /** Semua artikel (untuk admin). */
    fun getAll(callback: (Result<List<Article>>) -> Unit) = runAsync(callback) {
        val request = baseRequest("$endpoint?select=*&order=created_at.desc").get().build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal memuat artikel"))
        parseList(body)
    }

    /** Hanya artikel published (untuk member). */
    fun getPublished(callback: (Result<List<Article>>) -> Unit) = runAsync(callback) {
        val request = baseRequest("$endpoint?status=eq.published&select=*&order=created_at.desc").get().build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal memuat artikel"))
        parseList(body)
    }

    fun addArticle(article: Article, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val request = baseRequest(endpoint)
            .addHeader("Prefer", "return=minimal")
            .post(article.toJson().toString().toRequestBody(jsonType))
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal menambah artikel"))
    }

    fun updateArticle(article: Article, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val id = article.id ?: throw IllegalArgumentException("ID artikel kosong")
        val request = baseRequest("$endpoint?id=eq.$id")
            .addHeader("Prefer", "return=minimal")
            .patch(article.toJson().toString().toRequestBody(jsonType))
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal mengubah artikel"))
    }

    fun deleteArticle(id: String, callback: (Result<Unit>) -> Unit) = runAsync(callback) {
        val request = baseRequest("$endpoint?id=eq.$id")
            .addHeader("Prefer", "return=minimal")
            .delete()
            .build()
        val (code, body) = execute(request)
        if (code !in 200..299) throw RuntimeException(parseError(body, "Gagal menghapus artikel"))
    }

    // --- helper ---

    private fun parseList(body: String): List<Article> {
        val arr = JSONArray(body)
        return (0 until arr.length()).map { Article.fromJson(arr.getJSONObject(it)) }
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
