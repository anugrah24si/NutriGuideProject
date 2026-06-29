package com.example.nutriguideproject.data.remote

import android.os.Handler
import android.os.Looper
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.Timer
import java.util.TimerTask

/**
 * Berlangganan perubahan satu tabel via Supabase Realtime (WebSocket).
 * Setiap ada INSERT/UPDATE/DELETE pada tabel, [onChange] dipanggil di main thread.
 *
 * Dibuat defensif: bila koneksi gagal, tidak meng-crash app — layar tetap bisa
 * memuat data manual (refresh saat dibuka).
 */
class RealtimeTable(
    private val table: String,
    private val accessToken: String?,
    private val onChange: () -> Unit
) {

    private var webSocket: WebSocket? = null
    private var heartbeat: Timer? = null
    private val main = Handler(Looper.getMainLooper())
    private var ref = 0

    fun connect() {
        if (!SupabaseClient.isConfigured()) return
        runCatching {
            val wsUrl = SupabaseClient.baseUrl.replaceFirst("https://", "wss://")
                .replaceFirst("http://", "ws://") +
                "/realtime/v1/websocket?apikey=${SupabaseClient.anonKey}&vsn=1.0.0"
            val request = Request.Builder().url(wsUrl).build()
            webSocket = SupabaseClient.http.newWebSocket(request, listener)
        }
    }

    fun disconnect() {
        heartbeat?.cancel()
        heartbeat = null
        runCatching { webSocket?.close(1000, null) }
        webSocket = null
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(ws: WebSocket, response: Response) {
            joinChannel(ws)
            startHeartbeat(ws)
        }

        override fun onMessage(ws: WebSocket, text: String) {
            runCatching {
                val event = JSONObject(text).optString("event")
                if (event == "postgres_changes") {
                    main.post { onChange() }
                }
            }
        }

        // onFailure/onClosed: diabaikan — fallback ke refresh manual.
        override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {}
    }

    private fun joinChannel(ws: WebSocket) {
        val changeConfig = JSONObject()
            .put("event", "*")
            .put("schema", "public")
            .put("table", table)
        val config = JSONObject()
            .put("postgres_changes", JSONArray().put(changeConfig))
        val payload = JSONObject().put("config", config)
        if (!accessToken.isNullOrBlank()) payload.put("access_token", accessToken)

        val join = JSONObject()
            .put("topic", "realtime:public:$table")
            .put("event", "phx_join")
            .put("payload", payload)
            .put("ref", (++ref).toString())
        ws.send(join.toString())
    }

    private fun startHeartbeat(ws: WebSocket) {
        heartbeat = Timer()
        heartbeat?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val msg = JSONObject()
                    .put("topic", "phoenix")
                    .put("event", "heartbeat")
                    .put("payload", JSONObject())
                    .put("ref", (++ref).toString())
                runCatching { ws.send(msg.toString()) }
            }
        }, 25_000, 25_000)
    }
}
