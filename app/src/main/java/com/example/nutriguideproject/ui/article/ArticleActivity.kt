package com.example.nutriguideproject.ui.article

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R

/**
 * Halaman detail Artikel Edukasi.
 * Dibuka dari bagian "Artikel Edukasi" pada halaman Home.
 * Konten saat ini statis sesuai desain.
 */
class ArticleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_article)
        applySystemBarInsets()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnShareTop).setOnClickListener { shareArticle() }
        findViewById<Button>(R.id.btnShare).setOnClickListener { shareArticle() }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, R.string.article_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareArticle() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.article_title))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.article_title))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.article_share)))
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.articleRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
