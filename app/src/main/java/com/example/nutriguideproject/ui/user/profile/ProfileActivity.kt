package com.example.nutriguideproject.ui.user.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.UserProfile
import com.example.nutriguideproject.data.repository.ProfileRepository
import com.example.nutriguideproject.ui.user.common.MainNav
import com.example.nutriguideproject.ui.welcome.WelcomeActivity

/**
 * Halaman Profil pengguna.
 * Identitas (nama, email, inisial) diisi dari akun yang sedang login.
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        applySystemBarInsets()

        showIdentity()

        findViewById<View>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
        findViewById<View>(R.id.rowNotif).setOnClickListener {
            Toast.makeText(this, "Notifikasi segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.rowSecurity).setOnClickListener {
            Toast.makeText(this, "Keamanan segera hadir", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.rowLogout).setOnClickListener { confirmLogout() }

        MainNav.setup(this, MainNav.Tab.PROFILE)
    }

    override fun onStart() {
        super.onStart()
        loadProfile()
    }

    /** Memuat data profil dari Supabase dan menampilkannya. */
    private fun loadProfile() {
        ProfileRepository(SessionManager(this)).getMyProfile { result ->
            result.onSuccess { render(it) }
            // gagal/offline: biarkan nilai dari sesi (header) tetap tampil
        }
    }

    private fun render(p: UserProfile) {
        val name = p.fullName.ifBlank { SessionManager(this).displayName() }
        findViewById<TextView>(R.id.tvProfileName).text = name
        findViewById<TextView>(R.id.tvInfoName).text = name
        findViewById<TextView>(R.id.tvProfileEmail).text = p.email
        findViewById<TextView>(R.id.tvInfoEmail).text = p.email

        findViewById<TextView>(R.id.tvInfoAge).text =
            p.age?.let { getString(R.string.profile_age_fmt, it) } ?: "-"
        findViewById<TextView>(R.id.tvInfoGoal).text = p.goal.ifBlank { "-" }
        findViewById<TextView>(R.id.tvHealthWeight).text =
            p.weightKg?.let { "${trimNumber(it)} kg" } ?: "-"
        findViewById<TextView>(R.id.tvHealthHeight).text =
            p.heightCm?.let { "${trimNumber(it)} cm" } ?: "-"
        findViewById<TextView>(R.id.tvHealthNutrition).text =
            p.dailyCalorieTarget?.let { "$it kkal/hari" } ?: "-"
        findViewById<TextView>(R.id.tvHealthActivity).text = p.activityLevel.ifBlank { "-" }
    }

    private fun trimNumber(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    /** Mengisi nama, email, dan inisial dari akun yang login. */
    private fun showIdentity() {
        val session = SessionManager(this)
        val name = session.displayName()
        val email = session.email ?: "-"
        val initials = name.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.take(1).uppercase() }
            .ifBlank { "?" }

        findViewById<TextView>(R.id.tvProfileInitials).text = initials
        findViewById<TextView>(R.id.tvProfileName).text = name
        findViewById<TextView>(R.id.tvProfileEmail).text = email
        findViewById<TextView>(R.id.tvInfoName).text = name
        findViewById<TextView>(R.id.tvInfoEmail).text = email
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
        val root = findViewById<View>(R.id.profileRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
