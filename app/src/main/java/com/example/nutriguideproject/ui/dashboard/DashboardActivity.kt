package com.example.nutriguideproject.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.common.MainNav
import com.example.nutriguideproject.ui.article.ArticleActivity
import com.example.nutriguideproject.ui.log.LogActivity

/**
 * Layar dashboard utama (Home) NutriGuide.
 * Saat ini menampilkan data statis (tanpa sumber data nyata).
 */
class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        applySystemBarInsets()
        setupQuickActions()
        MainNav.setup(this, MainNav.Tab.HOME)
    }

    private fun setupQuickActions() {
        // Kartu "Log Makanan Harian" membuka halaman Log.
        findViewById<View>(R.id.cardLogFood).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        // Bagian "Artikel Edukasi" membuka halaman detail Artikel.
        val openArticle = View.OnClickListener {
            startActivity(Intent(this, ArticleActivity::class.java))
        }
        findViewById<View>(R.id.cardArticle).setOnClickListener(openArticle)
        findViewById<View>(R.id.seeAllArticles).setOnClickListener(openArticle)
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
