package com.example.nutriguideproject.ui.user.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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

/**
 * Halaman ubah data pribadi (profil milik user sendiri).
 * Memuat data dari Supabase lalu menyimpan perubahan (update).
 */
class ProfileEditActivity : AppCompatActivity() {

    private val session by lazy { SessionManager(this) }
    private val repository by lazy { ProfileRepository(session) }

    private lateinit var peName: EditText
    private lateinit var peAge: EditText
    private lateinit var peWeight: EditText
    private lateinit var peHeight: EditText
    private lateinit var peActivity: TextView
    private lateinit var peGoal: TextView
    private lateinit var peTarget: EditText

    private var loaded: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_edit)
        applySystemBarInsets()

        peName = findViewById(R.id.peName)
        peAge = findViewById(R.id.peAge)
        peWeight = findViewById(R.id.peWeight)
        peHeight = findViewById(R.id.peHeight)
        peActivity = findViewById(R.id.peActivity)
        peGoal = findViewById(R.id.peGoal)
        peTarget = findViewById(R.id.peTarget)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        setupDropdowns()
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener { save() }

        loadProfile()
    }

    private fun loadProfile() {
        repository.getMyProfile { result ->
            result.onSuccess { fillForm(it) }
                .onFailure { toast(getString(R.string.profile_load_error, it.message ?: "error")) }
        }
    }

    private fun fillForm(p: UserProfile) {
        loaded = p
        peName.setText(p.fullName)
        peAge.setText(p.age?.toString() ?: "")
        peWeight.setText(p.weightKg?.let { trimNumber(it) } ?: "")
        peHeight.setText(p.heightCm?.let { trimNumber(it) } ?: "")
        peTarget.setText(p.dailyCalorieTarget?.toString() ?: "")
        if (p.activityLevel.isNotBlank()) {
            peActivity.text = p.activityLevel
            peActivity.setTextColor(getColor(R.color.text_dark))
        }
        if (p.goal.isNotBlank()) {
            peGoal.text = p.goal
            peGoal.setTextColor(getColor(R.color.text_dark))
        }
    }

    private fun setupDropdowns() {
        peActivity.setOnClickListener {
            showOptions(getString(R.string.menu_activity), R.array.menu_activity_options, peActivity)
        }
        peGoal.setOnClickListener {
            showOptions(getString(R.string.menu_goal), R.array.menu_goal_options, peGoal)
        }
    }

    private fun showOptions(title: String, arrayRes: Int, target: TextView) {
        val options = resources.getStringArray(arrayRes)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options) { _, which ->
                target.text = options[which]
                target.setTextColor(getColor(R.color.text_dark))
            }
            .show()
    }

    private fun save() {
        val base = loaded
        if (base == null) {
            toast("Data profil belum termuat")
            return
        }
        val name = peName.text.toString().trim()
        if (name.isEmpty()) {
            toast("Nama tidak boleh kosong")
            return
        }
        val updated = base.copy(
            fullName = name,
            age = peAge.text.toString().toIntOrNull(),
            weightKg = peWeight.text.toString().toDoubleOrNull(),
            heightCm = peHeight.text.toString().toDoubleOrNull(),
            activityLevel = peActivity.text.toString().trim().takeIf { it.isNotBlank() && !it.startsWith("Pilih") } ?: "",
            goal = peGoal.text.toString().trim().takeIf { it.isNotBlank() && !it.startsWith("Pilih") } ?: "",
            dailyCalorieTarget = peTarget.text.toString().toIntOrNull()
        )

        repository.updateMyProfile(updated) { result ->
            result.onSuccess {
                // Perbarui nama di sesi agar Home & Profil ikut berubah.
                session.save(
                    base.id, session.accessToken ?: "", session.role ?: "member",
                    session.email ?: base.email, name
                )
                toast(getString(R.string.profile_saved))
                finish()
            }.onFailure { toast(it.message ?: "Gagal menyimpan profil") }
        }
    }

    private fun trimNumber(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.profileEditRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
