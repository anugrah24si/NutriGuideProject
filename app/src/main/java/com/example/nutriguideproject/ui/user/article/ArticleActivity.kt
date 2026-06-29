package com.example.nutriguideproject.ui.user.article

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R

/**
 * Halaman detail Artikel Edukasi.
 * Jika dibuka dengan data artikel (intent extras), menampilkan artikel nyata
 * dari Supabase; jika tidak, menampilkan konten contoh statis.
 */
class ArticleActivity : AppCompatActivity() {

    private var shareTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_article)
        applySystemBarInsets()

        shareTitle = getString(R.string.article_title)
        bindDynamicArticleIfAny()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnShareTop).setOnClickListener { shareArticle() }
        findViewById<Button>(R.id.btnShare).setOnClickListener { shareArticle() }
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, R.string.article_saved, Toast.LENGTH_SHORT).show()
        }
    }

    /** Mengisi tampilan dari data artikel yang dikirim lewat intent (bila ada). */
    private fun bindDynamicArticleIfAny() {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        shareTitle = title

        findViewById<TextView>(R.id.tvArticleTitle).text = title
        intent.getStringExtra(EXTRA_CATEGORY)?.takeIf { it.isNotBlank() }?.let {
            findViewById<TextView>(R.id.tvArticleTag).text = it
        }
        intent.getStringExtra(EXTRA_READTIME)?.takeIf { it.isNotBlank() }?.let {
            findViewById<TextView>(R.id.tvArticleReadTime).text = it
        }
        intent.getStringExtra(EXTRA_AUTHOR)?.takeIf { it.isNotBlank() }?.let {
            findViewById<TextView>(R.id.tvArticleAuthor).text = it
        }
        val content = intent.getStringExtra(EXTRA_CONTENT).orEmpty()
        findViewById<TextView>(R.id.tvSec1Title).setText(R.string.article_content_heading)
        findViewById<TextView>(R.id.tvSec1Body).text = content
    }

    private fun shareArticle() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, shareTitle)
            putExtra(Intent.EXTRA_TEXT, shareTitle)
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

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_READTIME = "extra_readtime"
        const val EXTRA_AUTHOR = "extra_author"
        const val EXTRA_CONTENT = "extra_content"
    }
}
