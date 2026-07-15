package com.example.nutriguideproject.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.repository.AdminRepository
import com.example.nutriguideproject.data.repository.AdminStats
import com.example.nutriguideproject.ui.shared.chart.LineChartView

/**
 * Halaman utama Admin (Admin Dashboard).
 * Dibuka saat pengguna login dengan role admin.
 * Menampilkan ringkasan statistik, grafik, dan aksi cepat:
 * - Tambah Data Makanan → ManageFoodActivity + form tambah terbuka
 * - Buat Artikel Edukasi → ManageArticleActivity
 * - Kelola Database → ManageFoodActivity (daftar)
 * - Lihat Daftar User → UserListActivity
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

    /** Isi chart "Pertumbuhan Pengguna" (line chart). Bar chart aktivitas 7 hari sudah dihapus. */
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
    }


    /**
     * Menghubungkan 4 kartu "Aksi Cepat" di dashboard admin.
     * Layout: activity_admin_dashboard.xml (rowAddFood, rowCreateArticle, ...).
     */
    private fun setupQuickActions() {
        // 1) Tambah Data Makanan
        //    Shortcut langsung ke form input makanan baru.
        //    Extra EXTRA_OPEN_ADD_FORM=true → ManageFoodActivity memanggil showAddForm().
        findViewById<View>(R.id.rowAddFood).setOnClickListener {
            val intent = Intent(this, ManageFoodActivity::class.java).apply {
                putExtra(ManageFoodActivity.EXTRA_OPEN_ADD_FORM, true)
            }
            startActivity(intent)
        }

        // 2) Buat Artikel Edukasi → halaman kelola artikel
        findViewById<View>(R.id.rowCreateArticle).setOnClickListener {
            startActivity(Intent(this, ManageArticleActivity::class.java))
        }

        // 3) Kelola Database → daftar makanan (tanpa auto-open form)
        findViewById<View>(R.id.rowManageDb).setOnClickListener {
            startActivity(Intent(this, ManageFoodActivity::class.java))
        }

        // 4) Lihat Daftar User → daftar pengguna
        findViewById<View>(R.id.rowUserList).setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
    }

    private fun setupBottomNav() {
        AdminNav.setup(this, AdminNav.Tab.DASHBOARD)
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
