package com.example.nutriguideproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.dashboard.DashboardActivity

/**
 * Layar autentikasi dengan dua tab: Masuk dan Daftar.
 * Tidak ada validasi data — tombol mana pun langsung menuju Dashboard.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var tabLogin: TextView
    private lateinit var tabRegister: TextView
    private lateinit var nameGroup: View
    private lateinit var loginButtons: View
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        applySystemBarInsets()

        tabLogin = findViewById(R.id.tabLogin)
        tabRegister = findViewById(R.id.tabRegister)
        nameGroup = findViewById(R.id.nameGroup)
        loginButtons = findViewById(R.id.loginButtons)
        btnRegister = findViewById(R.id.btnRegister)

        tabLogin.setOnClickListener { showLogin() }
        tabRegister.setOnClickListener { showRegister() }

        // Semua tombol langsung membuka Dashboard.
        findViewById<Button>(R.id.btnLoginUser).setOnClickListener { goToDashboard() }
        findViewById<Button>(R.id.btnLoginAdmin).setOnClickListener { goToDashboard() }
        btnRegister.setOnClickListener { goToDashboard() }

        showLogin()
    }

    private fun showLogin() {
        tabLogin.setBackgroundResource(R.drawable.bg_tab_active)
        tabLogin.setTextColor(getColor(R.color.text_dark))
        tabLogin.setTypeface(null, android.graphics.Typeface.BOLD)

        tabRegister.setBackgroundColor(getColor(android.R.color.transparent))
        tabRegister.setTextColor(getColor(R.color.text_muted))
        tabRegister.setTypeface(null, android.graphics.Typeface.NORMAL)

        nameGroup.visibility = View.GONE
        loginButtons.visibility = View.VISIBLE
        btnRegister.visibility = View.GONE
    }

    private fun showRegister() {
        tabRegister.setBackgroundResource(R.drawable.bg_tab_active)
        tabRegister.setTextColor(getColor(R.color.text_dark))
        tabRegister.setTypeface(null, android.graphics.Typeface.BOLD)

        tabLogin.setBackgroundColor(getColor(android.R.color.transparent))
        tabLogin.setTextColor(getColor(R.color.text_muted))
        tabLogin.setTypeface(null, android.graphics.Typeface.NORMAL)

        nameGroup.visibility = View.VISIBLE
        loginButtons.visibility = View.GONE
        btnRegister.visibility = View.VISIBLE
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.authRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
