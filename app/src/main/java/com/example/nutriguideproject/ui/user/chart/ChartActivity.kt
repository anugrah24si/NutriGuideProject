package com.example.nutriguideproject.ui.user.chart

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.repository.FoodLogRepository
import com.example.nutriguideproject.data.repository.ProfileRepository
import com.example.nutriguideproject.ui.shared.chart.LineChartView
import com.example.nutriguideproject.ui.shared.chart.PieChartView
import com.example.nutriguideproject.ui.user.common.MainNav
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Halaman Grafik Gizi — data diambil langsung dari Supabase milik user yang login.
 *
 * Pie chart  : persentase kalori hari ini vs target harian (dari profil user).
 * Line chart : total kalori per hari selama 7 hari terakhir.
 */
class ChartActivity : AppCompatActivity() {

    private val session by lazy { SessionManager(this) }
    private val logRepository by lazy { FoodLogRepository(session) }
    private val profileRepository by lazy { ProfileRepository(session) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart)
        applySystemBarInsets()
        MainNav.setup(this, MainNav.Tab.CHART)
    }

    override fun onStart() {
        super.onStart()
        loadChartData()
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private fun loadChartData() {
        // Ambil target kalori dari profil user
        profileRepository.getMyProfile { profileResult ->
            val calorieTarget = profileResult.getOrNull()?.dailyCalorieTarget ?: 2000

            // Ambil log 7 hari terakhir
            logRepository.getLast7DaysLogs { logResult ->
                logResult.onSuccess { grouped ->
                    renderPieChart(grouped, calorieTarget)
                    renderLineChart(grouped)
                }.onFailure {
                    // Gagal/offline: tampilkan grafik kosong daripada crash
                    renderPieChart(emptyMap(), calorieTarget)
                    renderLineChart(emptyMap())
                }
            }
        }
    }

    // ── Pie chart: kalori hari ini vs target ─────────────────────────────────

    private fun renderPieChart(grouped: Map<String, List<*>>, calorieTarget: Int) {
        val todayKey = todayDateKey()

        @Suppress("UNCHECKED_CAST")
        val todayLogs = (grouped[todayKey] as? List<com.example.nutriguideproject.data.model.FoodLog>)
            ?: emptyList()

        val todayCalories = todayLogs.sumOf { it.calories }
        val achieved = todayCalories.coerceAtMost(calorieTarget).toFloat()
        val remaining = (calorieTarget - todayCalories).coerceAtLeast(0).toFloat()

        val colorAchieved = ContextCompat.getColor(this, R.color.chart_protein)
        val colorRemaining = ContextCompat.getColor(this, R.color.chart_carbs)

        val slices = if (todayCalories == 0) {
            // Belum ada log → tampilkan 100% "Belum ada"
            listOf(PieChartView.Slice("Belum ada", 1f, colorRemaining))
        } else {
            listOf(
                PieChartView.Slice("Tercapai", achieved, colorAchieved),
                PieChartView.Slice("Sisa", remaining.coerceAtLeast(0f), colorRemaining)
            ).filter { it.value > 0f }
        }

        findViewById<PieChartView>(R.id.pieStatus).setData(slices)

        // Update subtitle pie chart dengan angka nyata
        findViewById<TextView>(R.id.tvPieSubtitle)?.text =
            "$todayCalories / $calorieTarget kkal hari ini"
    }

    // ── Line chart: kalori per hari 7 hari terakhir ──────────────────────────

    private fun renderLineChart(grouped: Map<String, List<*>>) {
        // Buat urutan 7 hari: dari 6 hari lalu sampai hari ini
        val cal = Calendar.getInstance()
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val labelFmt = SimpleDateFormat("EEE", Locale("id")) // Sen, Sel, Rab, ...

        val labels = Array(7) { "" }
        val values = FloatArray(7) { 0f }

        for (i in 6 downTo 0) {
            val idx = 6 - i
            val dayKey = dateFmt.format(cal.time)
            labels[idx] = labelFmt.format(cal.time).take(3)

            @Suppress("UNCHECKED_CAST")
            val dayLogs = (grouped[dayKey] as? List<com.example.nutriguideproject.data.model.FoodLog>)
                ?: emptyList()
            values[idx] = dayLogs.sumOf { it.calories }.toFloat()

            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        val maxCalories = values.maxOrNull()?.let { it * 1.2f }?.coerceAtLeast(500f) ?: 2000f

        val color = ContextCompat.getColor(this, R.color.chart_protein)
        findViewById<LineChartView>(R.id.lineWeight).setData(
            seriesList = listOf(LineChartView.Series(values, color)),
            labels = labels,
            maxValue = maxCalories
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Tanggal hari ini dalam format yyyy-MM-dd (zona lokal perangkat). */
    private fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

    private fun color(id: Int) = ContextCompat.getColor(this, id)

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.chartRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
