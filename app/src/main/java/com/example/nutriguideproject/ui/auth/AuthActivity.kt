package com.example.nutriguideproject.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
 *
 * Login role-aware:
 * - "Masuk sebagai User"  -> hanya role member/user yang boleh masuk.
 * - "Masuk sebagai Admin" -> hanya role admin yang boleh masuk.
 * Kalau tombol tidak cocok dengan role akun, login ditolak (session tidak disimpan).
 *
 * Fitur password:
 * - Tombol mata di kanan field password untuk show/hide teks.
 * - Tap di luar field input -> keyboard otomatis ditutup.
 */
class AuthActivity : AppCompatActivity() {

    companion object {
        /** Role yang diharapkan saat menekan tombol login. */
        private const val ROLE_ADMIN = "admin"
        private const val ROLE_MEMBER = "member"
    }

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
    private lateinit var btnTogglePassword: ImageButton

    /** true = password ditampilkan sebagai teks biasa; false = disembunyikan. */
    private var isPasswordVisible = false

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
        btnTogglePassword = findViewById(R.id.btnTogglePassword)

        tabLogin.setOnClickListener { showLogin() }
        tabRegister.setOnClickListener { showRegister() }

        // Tombol menentukan role yang DIHARAPKAN; role asli dicek dari database.
        // Bug lama: kedua tombol sama-sama doLogin() tanpa cek role.
        btnLoginUser.setOnClickListener { doLogin(expectedRole = ROLE_MEMBER) }
        btnLoginAdmin.setOnClickListener { doLogin(expectedRole = ROLE_ADMIN) }
        btnRegister.setOnClickListener { doRegister() }

        // Tombol mata: bolak-balik tampilkan / sembunyikan password.
        btnTogglePassword.setOnClickListener { togglePasswordVisibility() }

        showLogin()
    }

    /**
     * Show/hide password di field inputPassword.
     * - Saat hidden: inputType = textPassword + ikon mata tertutup
     * - Saat visible: inputType = textVisiblePassword + ikon mata terbuka
     * Posisi kursor dipertahankan di ujung teks supaya UX tetap nyaman.
     */
    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            inputPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility)
            btnTogglePassword.contentDescription = getString(R.string.auth_hide_password)
        } else {
            inputPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            btnTogglePassword.contentDescription = getString(R.string.auth_show_password)
        }

        inputPassword.setSelection(inputPassword.text?.length ?: 0)
    }

    /**
     * Login dengan email/password, lalu validasi role vs tombol yang diklik.
     * @param expectedRole ROLE_MEMBER untuk tombol User, ROLE_ADMIN untuk tombol Admin.
     */
    private fun doLogin(expectedRole: String) {
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
                .onSuccess { user -> handleLoginSuccess(user, expectedRole) }
                .onFailure { toast(it.message ?: getString(R.string.auth_login_failed)) }
        }
    }

    /**
     * Setelah email/password valid, cek apakah role akun cocok dengan tombol.
     * - expectedRole = admin  -> hanya admin yang lolos
     * - expectedRole = member -> admin DITOLAK (harus pakai tombol admin)
     * Session hanya disimpan kalau role cocok.
     */
    private fun handleLoginSuccess(user: AuthUser, expectedRole: String) {
        val actualRole = normalizeRole(user.role)

        val roleMatches = when (expectedRole) {
            ROLE_ADMIN -> actualRole == ROLE_ADMIN
            ROLE_MEMBER -> actualRole != ROLE_ADMIN // member / user / default
            else -> false
        }

        if (!roleMatches) {
            // Jangan simpan session — pengguna tetap di halaman login.
            val message = if (expectedRole == ROLE_ADMIN) {
                getString(R.string.auth_role_mismatch_admin)
            } else {
                getString(R.string.auth_role_mismatch_user)
            }
            toast(message)
            return
        }

        onAuthSuccess(user.copy(role = actualRole))
    }

    /** Samakan penulisan role dari DB (admin / member / user). */
    private fun normalizeRole(role: String): String {
        return when (role.trim().lowercase()) {
            "admin" -> ROLE_ADMIN
            "member", "user" -> ROLE_MEMBER
            else -> role.trim().lowercase().ifBlank { ROLE_MEMBER }
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
        val target = if (normalizeRole(user.role) == ROLE_ADMIN) {
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

    /**
     * Saat user menyentuh layar:
     * - jika sentuhan TIDAK di atas EditText (field input),
     *   maka clear focus + tutup soft keyboard.
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val focused = currentFocus
            if (focused is EditText && !isTouchInsideView(focused, ev)) {
                focused.clearFocus()
                hideKeyboard(focused)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isTouchInsideView(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX
        val y = event.rawY
        return x >= location[0] &&
            x <= location[0] + view.width &&
            y >= location[1] &&
            y <= location[1] + view.height
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
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
