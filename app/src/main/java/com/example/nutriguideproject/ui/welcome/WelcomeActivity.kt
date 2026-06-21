package com.example.nutriguideproject.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.auth.AuthActivity

/**
 * Layar pembuka (welcome screen) NutriGuide.
 * Menampilkan logo, tagline, dan tombol untuk memulai aplikasi.
 */
class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        applySystemBarInsets()

        findViewById<Button>(R.id.startButton).setOnClickListener {
            startActivity(Intent(this, AuthActivity::class.java))
        }
    }

    /** Memberi padding sesuai status bar / navigation bar agar konten tidak tertutup. */
    private fun applySystemBarInsets() {
        val root = findViewById<android.view.View>(R.id.welcomeRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
