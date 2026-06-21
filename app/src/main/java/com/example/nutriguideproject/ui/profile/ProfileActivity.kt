package com.example.nutriguideproject.ui.profile

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
import com.example.nutriguideproject.ui.common.MainNav
import com.example.nutriguideproject.ui.welcome.WelcomeActivity

/**
 * Halaman Profil pengguna.
 * Menampilkan informasi pribadi, data kesehatan, dan pengaturan.
 * Dibuka dari bottom nav "Profil" atau tombol "Ubah Data Pribadi" pada Rekomendasi.
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        applySystemBarInsets()

        findViewById<View>(R.id.rowNotif).setOnClickListener {
            Toast.makeText(this, "Notifikasi segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.rowSecurity).setOnClickListener {
            Toast.makeText(this, "Keamanan segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.rowLogout).setOnClickListener { confirmLogout() }

        MainNav.setup(this, MainNav.Tab.PROFILE)
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.profile_logout)
            .setMessage(R.string.profile_logout_sub)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.profile_logout) { _, _ ->
                val intent = Intent(this, WelcomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
            .show()
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.profileRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
