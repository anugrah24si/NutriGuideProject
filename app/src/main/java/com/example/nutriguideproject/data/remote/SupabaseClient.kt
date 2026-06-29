package com.example.nutriguideproject.data.remote

import com.example.nutriguideproject.BuildConfig
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Konfigurasi & klien HTTP untuk mengakses Supabase (Auth + REST/PostgREST).
 * URL & anon key dibaca dari BuildConfig (diisi via local.properties).
 */
object SupabaseClient {

    /** Base URL project Supabase, tanpa trailing slash. */
    val baseUrl: String = BuildConfig.SUPABASE_URL.trimEnd('/')

    /** Anon (public) key — aman dipakai di app selama RLS aktif. */
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY

    /** True jika URL & anon key sudah diisi di local.properties. */
    fun isConfigured(): Boolean = baseUrl.isNotBlank() && anonKey.isNotBlank()

    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    const val AUTH = "/auth/v1"
    const val REST = "/rest/v1"
}
