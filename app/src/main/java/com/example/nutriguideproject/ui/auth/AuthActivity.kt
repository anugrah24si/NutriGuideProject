package com.example.nutriguideproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.repository.AuthRepository
import com.example.nutriguideproject.data.repository.AuthUser
import com.example.nutriguideproject.ui.admin.AdminDashboardActivity
import com.example.nutriguideproject.ui.user.dashboard.DashboardActivity

/**
 * Layar autentikasi (Masuk & Daftar) berbasis Supabase Auth.
 * Setelah login berhasil, app membaca `role` dari tabel profiles lalu
 * mengarahkan ke halaman Admin atau User secara otomatis.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var tabLogin: TextView
    private lateinit var tabRegister: TextView
    private lateinit var nameGroup: View
    private lateinit var loginButtons: View
    private lateinit var btnRegister: Button
    private lateinit var btnLoginUser: Button
    private lateinit var btnLoginAdmin: Button

    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText

    private val authRepository = AuthRepository()
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)
        applySystemBarInsets()

        session = SessionManager(this)

        tabLogin = findViewById(R.id.tabLogin)
        tabRegister = findViewById(R.id.tabRegister)
        nameGroup = findViewById(R.id.nameGroup)
        loginButtons = findViewById(R.id.loginButtons)
        btnRegister = findViewById(R.id.btnRegister)
        btnLoginUser = findViewById(R.id.btnLoginUser)
        btnLoginAdmin = findViewById(R.id.btnLoginAdmin)
        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)

        tabLogin.setOnClickListener { showLogin() }
        tabRegister.setOnClickListener { showRegister() }

        // Login: role dari database yang menentukan tujuan (admin / user).
        btnLoginUser.setOnClickListener { doLogin() }
        btnLoginAdmin.setOnClickListener { doLogin() }
        btnRegister.setOnClickListener { doRegister() }

        showLogin()
    }

    private fun doLogin() {
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            toast("Email dan password wajib diisi")
            return
        }
        setLoading(true)
        authRepository.signIn(email, password) { result ->
            setLoading(false)
            result
                .onSuccess { onAuthSuccess(it) }
                .onFailure { toast(it.message ?: "Login gagal") }
        }
    }

    private fun doRegister() {
        val name = inputName.text.toString().trim()
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString()
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Nama, email, dan password wajib diisi")
            return
        }
        if (password.length < 6) {
            toast("Password minimal 6 karakter")
            return
        }
        setLoading(true)
        authRepository.signUp(name, email, password) { result ->
            setLoading(false)
            result
                .onSuccess { onAuthSuccess(it) }
                .onFailure { toast(it.message ?: "Pendaftaran gagal") }
        }
    }

    private fun onAuthSuccess(user: AuthUser) {
        session.save(user.userId, user.accessToken, user.role, user.email, user.fullName)
        val target = if (user.role == "admin") {
            AdminDashboardActivity::class.java
        } else {
            DashboardActivity::class.java
        }
        val intent = Intent(this, target).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        val enabled = !loading
        btnLoginUser.isEnabled = enabled
        btnLoginAdmin.isEnabled = enabled
        btnRegister.isEnabled = enabled
        if (loading) toast("Memproses…")
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

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

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.authRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
