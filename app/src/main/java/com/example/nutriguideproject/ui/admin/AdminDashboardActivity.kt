package com.example.nutriguideproject.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.repository.AdminRepository
import com.example.nutriguideproject.data.repository.AdminStats
import com.example.nutriguideproject.ui.shared.chart.BarChartView
import com.example.nutriguideproject.ui.shared.chart.LineChartView

/**
 * Halaman utama Admin (Admin Dashboard).
 * Dibuka saat pengguna memilih "Masuk sebagai Admin" pada halaman Auth.
 * Menampilkan ringkasan statistik, grafik, dan aksi cepat (data statis).
 */
class AdminDashboardActivity : AppCompatActivity() {

    private val adminRepository by lazy { AdminRepository(SessionManager(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)
        applySystemBarInsets()

        populateCharts()
        setupQuickActions()
        setupBottomNav()
    }

    override fun onStart() {
        super.onStart()
        loadStats()
    }

    private fun loadStats() {
        adminRepository.getStats { result ->
            result.onSuccess { renderStats(it) }
            // gagal/offline: biarkan angka contoh
        }
    }

    private fun renderStats(stats: AdminStats) {
        findViewById<android.widget.TextView>(R.id.tvStatUsers).text = stats.totalUsers.toString()
        findViewById<android.widget.TextView>(R.id.tvStatFood).text = String.format("%,d", stats.totalFoods)
        findViewById<android.widget.TextView>(R.id.tvStatArticle).text = stats.totalArticles.toString()
        findViewById<android.widget.TextView>(R.id.tvStatLog).text = stats.todayLogs.toString()
    }

    private fun color(id: Int) = ContextCompat.getColor(this, id)

    private fun populateCharts() {
        findViewById<LineChartView>(R.id.lineGrowth).setData(
            seriesList = listOf(
                LineChartView.Series(
                    floatArrayOf(120f, 180f, 250f, 310f, 420f),
                    color(R.color.green_dark)
                )
            ),
            labels = arrayOf("Jan", "Feb", "Mar", "Apr", "Mei"),
            maxValue = 600f
        )

        findViewById<BarChartView>(R.id.barActivity).setData(
            values = floatArrayOf(450f, 500f, 520f, 600f, 580f, 400f, 380f),
            labels = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"),
            maxValue = 800f
        )
    }

    private fun setupQuickActions() {
        // "Kelola Database" membuka halaman Kelola Data Makanan.
        findViewById<View>(R.id.rowManageDb).setOnClickListener {
            startActivity(android.content.Intent(this, ManageFoodActivity::class.java))
        }
        // "Buat Artikel Edukasi" membuka halaman Kelola Artikel.
        findViewById<View>(R.id.rowCreateArticle).setOnClickListener {
            startActivity(android.content.Intent(this, ManageArticleActivity::class.java))
        }
        // "Lihat Daftar User" membuka halaman Daftar User.
        findViewById<View>(R.id.rowUserList).setOnClickListener {
            startActivity(android.content.Intent(this, UserListActivity::class.java))
        }
        bindComingSoon(R.id.rowAddFood, "Tambah Data Makanan")
    }

    private fun setupBottomNav() {
        AdminNav.setup(this, AdminNav.Tab.DASHBOARD)
    }

    private fun bindComingSoon(viewId: Int, label: String) {
        findViewById<View>(viewId).setOnClickListener {
            Toast.makeText(this, "$label segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.adminRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
