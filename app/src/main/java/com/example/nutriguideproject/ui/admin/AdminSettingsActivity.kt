package com.example.nutriguideproject.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.ui.welcome.WelcomeActivity

/**
 * Halaman "Pengaturan" pada area Admin.
 * Menampilkan identitas admin + pengaturan akun (notifikasi, keamanan, logout).
 */
class AdminSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_settings)
        applySystemBarInsets()

        findViewById<View>(R.id.rowNotif).setOnClickListener {
            Toast.makeText(this, "Notifikasi segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.rowSecurity).setOnClickListener {
            Toast.makeText(this, "Keamanan segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.rowLogout).setOnClickListener { confirmLogout() }

        AdminNav.setup(this, AdminNav.Tab.SETTINGS)
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.profile_logout)
            .setMessage(R.string.profile_logout_sub)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.profile_logout) { _, _ ->
                SessionManager(this).clear()
                val intent = Intent(this, WelcomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
            .show()
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.adminSettingsRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
