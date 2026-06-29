package com.example.nutriguideproject.ui.user.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.Article
import com.example.nutriguideproject.data.model.FoodLog
import com.example.nutriguideproject.data.repository.ArticleRepository
import com.example.nutriguideproject.data.repository.FoodLogRepository
import com.example.nutriguideproject.ui.user.article.ArticleActivity
import com.example.nutriguideproject.ui.user.common.MainNav
import com.example.nutriguideproject.ui.user.log.LogActivity
import kotlin.math.roundToInt

/**
 * Layar dashboard utama (Home) NutriGuide.
 * Ringkasan kalori, gizi, dan daftar log diambil dari food_logs (Supabase).
 */
class DashboardActivity : AppCompatActivity() {

    private val session by lazy { SessionManager(this) }
    private val logRepository by lazy { FoodLogRepository(session) }
    private val articleRepository by lazy { ArticleRepository(session) }
    private val logAdapter = LogTodayAdapter()
    private var latestArticle: Article? = null

    /** Target kalori harian (sementara konstan; bisa diambil dari profil nanti). */
    private val calorieTarget = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        applySystemBarInsets()
        setupQuickActions()
        setupLogList()
        MainNav.setup(this, MainNav.Tab.HOME)
    }

    override fun onStart() {
        super.onStart()
        loadToday()
        loadLatestArticle()
    }

    private fun setupLogList() {
        findViewById<RecyclerView>(R.id.logRecycler).apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = logAdapter
        }
    }

    private fun loadToday() {
        logRepository.getTodayLogs { result ->
            result.onSuccess { renderSummary(it) }
                .onFailure { /* diam: biarkan tampilan default bila gagal/offline */ }
        }
    }

    private fun renderSummary(logs: List<FoodLog>) {
        val totalCalories = logs.sumOf { it.calories }
        val protein = logs.sumOf { it.proteinG }
        val carbs = logs.sumOf { it.carbsG }
        val fat = logs.sumOf { it.fatG }

        findViewById<TextView>(R.id.tvCalorieValue).text = String.format("%,d", totalCalories)

        val percent = if (calorieTarget > 0) {
            (totalCalories.toDouble() / calorieTarget * 100).coerceIn(0.0, 100.0)
        } else 0.0
        findViewById<ProgressBar>(R.id.calorieProgress).progress = percent.roundToInt()
        findViewById<TextView>(R.id.tvCaloriePercent).text = String.format("%.1f%%", percent)

        val remaining = (calorieTarget - totalCalories).coerceAtLeast(0)
        findViewById<TextView>(R.id.tvCalorieRemaining).text = "$remaining kkal lagi"

        findViewById<TextView>(R.id.tvProteinVal).text = gram(protein)
        findViewById<TextView>(R.id.tvCarbsVal).text = gram(carbs)
        findViewById<TextView>(R.id.tvFatVal).text = gram(fat)

        logAdapter.submit(logs)
        findViewById<View>(R.id.tvLogEmpty).visibility =
            if (logs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun gram(value: Double): String {
        val text = if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
        return "${text}g"
    }

    private fun loadLatestArticle() {
        articleRepository.getPublished { result ->
            result.onSuccess { articles ->
                val article = articles.firstOrNull() ?: return@onSuccess
                latestArticle = article
                findViewById<TextView>(R.id.tvDashArticleTitle).text = article.title
                findViewById<TextView>(R.id.tvDashArticleDesc).text = article.content
                findViewById<TextView>(R.id.tvDashArticleTag).text = article.category
                findViewById<TextView>(R.id.tvDashArticleRead).text = article.readTime
            }
            // gagal/offline: biarkan konten contoh statis
        }
    }

    private fun setupQuickActions() {
        findViewById<View>(R.id.cardLogFood).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }
        val openArticle = View.OnClickListener { openArticle() }
        findViewById<View>(R.id.cardArticle).setOnClickListener(openArticle)
        findViewById<View>(R.id.seeAllArticles).setOnClickListener(openArticle)
    }

    private fun openArticle() {
        val intent = Intent(this, ArticleActivity::class.java)
        latestArticle?.let {
            intent.putExtra(ArticleActivity.EXTRA_TITLE, it.title)
            intent.putExtra(ArticleActivity.EXTRA_CATEGORY, it.category)
            intent.putExtra(ArticleActivity.EXTRA_READTIME, it.readTime)
            intent.putExtra(ArticleActivity.EXTRA_AUTHOR, it.author)
            intent.putExtra(ArticleActivity.EXTRA_CONTENT, it.content)
        }
        startActivity(intent)
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.dashboardRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
