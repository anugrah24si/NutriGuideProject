package com.example.nutriguideproject.ui.chart

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.common.MainNav

/**
 * Halaman Grafik Gizi dengan tiga tab: Harian, Mingguan, dan Status.
 * Memakai custom chart view (bar, pie, line) tanpa library eksternal.
 * Seluruh data masih statis sesuai desain.
 */
class ChartActivity : AppCompatActivity() {

    private lateinit var tabs: List<TextView>
    private lateinit var contentDaily: View
    private lateinit var contentWeekly: View
    private lateinit var contentStatus: View

    private val days = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart)
        applySystemBarInsets()

        contentDaily = findViewById(R.id.contentDaily)
        contentWeekly = findViewById(R.id.contentWeekly)
        contentStatus = findViewById(R.id.contentStatus)

        setupTabs()
        populateDaily()
        populateWeekly()
        populateStatus()

        MainNav.setup(this, MainNav.Tab.CHART)
    }

    private fun setupTabs() {
        val tabDaily = findViewById<TextView>(R.id.tabDaily)
        val tabWeekly = findViewById<TextView>(R.id.tabWeekly)
        val tabStatus = findViewById<TextView>(R.id.tabStatus)
        tabs = listOf(tabDaily, tabWeekly, tabStatus)

        tabDaily.setOnClickListener { selectTab(0) }
        tabWeekly.setOnClickListener { selectTab(1) }
        tabStatus.setOnClickListener { selectTab(2) }
        selectTab(0)
    }

    private fun selectTab(index: Int) {
        tabs.forEachIndexed { i, tab ->
            val active = i == index
            tab.setBackgroundResource(if (active) R.drawable.bg_tab_active else android.R.color.transparent)
            tab.setTextColor(ContextCompat.getColor(this, if (active) R.color.text_dark else R.color.text_muted))
            tab.setTypeface(null, if (active) Typeface.BOLD else Typeface.NORMAL)
        }
        contentDaily.visibility = if (index == 0) View.VISIBLE else View.GONE
        contentWeekly.visibility = if (index == 1) View.VISIBLE else View.GONE
        contentStatus.visibility = if (index == 2) View.VISIBLE else View.GONE
    }

    private fun color(id: Int) = ContextCompat.getColor(this, id)

    private fun populateDaily() {
        findViewById<BarChartView>(R.id.barCalorie).setData(
            values = floatArrayOf(1750f, 1900f, 1650f, 1800f, 1700f, 2100f, 1800f),
            labels = days,
            maxValue = 2200f
        )

        findViewById<PieChartView>(R.id.pieMacro).setData(
            listOf(
                PieChartView.Slice("Protein", 25f, color(R.color.chart_protein)),
                PieChartView.Slice("Lemak", 25f, color(R.color.chart_fat)),
                PieChartView.Slice("Karbohidrat", 50f, color(R.color.chart_carbs))
            )
        )

        findViewById<LineChartView>(R.id.lineMacro).setData(
            seriesList = listOf(
                LineChartView.Series(floatArrayOf(72f, 70f, 68f, 75f, 71f, 74f, 70f), color(R.color.chart_protein)),
                LineChartView.Series(floatArrayOf(85f, 80f, 88f, 82f, 86f, 84f, 83f), color(R.color.chart_carbs)),
                LineChartView.Series(floatArrayOf(60f, 58f, 62f, 59f, 61f, 63f, 60f), color(R.color.chart_fat))
            ),
            labels = days,
            maxValue = 260f
        )
    }

    private fun populateWeekly() {
        val weeks = arrayOf("M1", "M2", "M3", "M4")
        findViewById<BarChartView>(R.id.barWeekly).setData(
            values = floatArrayOf(1850f, 1920f, 1780f, 1960f),
            labels = weeks,
            maxValue = 2200f
        )
        findViewById<LineChartView>(R.id.lineWeekly).setData(
            seriesList = listOf(
                LineChartView.Series(floatArrayOf(70f, 73f, 71f, 75f), color(R.color.chart_protein)),
                LineChartView.Series(floatArrayOf(84f, 82f, 86f, 83f), color(R.color.chart_carbs)),
                LineChartView.Series(floatArrayOf(60f, 61f, 59f, 62f), color(R.color.chart_fat))
            ),
            labels = weeks,
            maxValue = 260f
        )
    }

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
