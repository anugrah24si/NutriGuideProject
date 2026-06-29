package com.example.nutriguideproject.ui.user.chart

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.shared.chart.LineChartView
import com.example.nutriguideproject.ui.shared.chart.PieChartView
import com.example.nutriguideproject.ui.user.common.MainNav

/**
 * Halaman Grafik Gizi — menampilkan ringkasan Status (pencapaian target & tren).
 * Memakai custom chart view (pie & line) tanpa library eksternal.
 */
class ChartActivity : AppCompatActivity() {

    private val days = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart)
        applySystemBarInsets()

        populateStatus()
        MainNav.setup(this, MainNav.Tab.CHART)
    }

    private fun color(id: Int) = ContextCompat.getColor(this, id)

    private fun populateStatus() {
        findViewById<PieChartView>(R.id.pieStatus).setData(
            listOf(
                PieChartView.Slice("Tercapai", 70f, color(R.color.chart_protein)),
                PieChartView.Slice("Kurang", 30f, color(R.color.chart_carbs))
            )
        )
        findViewById<LineChartView>(R.id.lineWeight).setData(
            seriesList = listOf(
                LineChartView.Series(floatArrayOf(65f, 64f, 64f, 63f, 63f, 62f, 62f), color(R.color.chart_protein))
            ),
            labels = days,
            maxValue = 130f
        )
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.chartRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
