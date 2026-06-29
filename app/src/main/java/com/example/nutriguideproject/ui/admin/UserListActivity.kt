package com.example.nutriguideproject.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.repository.AdminRepository

/**
 * Halaman admin "Daftar User" — menampilkan seluruh pengguna (member & admin).
 * Dibuka dari aksi cepat "Lihat Daftar User" pada Admin Dashboard.
 */
class UserListActivity : AppCompatActivity() {

    private val repository by lazy { AdminRepository(SessionManager(this)) }
    private val adapter = UserAdapter()
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_list)
        applySystemBarInsets()

        tvCount = findViewById(R.id.tvUserCount)
        tvEmpty = findViewById(R.id.tvUserEmpty)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<RecyclerView>(R.id.userRecycler).apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = this@UserListActivity.adapter
        }
    }

    override fun onStart() {
        super.onStart()
        loadUsers()
    }

    private fun loadUsers() {
        repository.getUsers { result ->
            result.onSuccess { users ->
                adapter.submit(users)
                tvCount.text = getString(R.string.ul_count_fmt, users.size)
                tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure {
                Toast.makeText(this, getString(R.string.ul_load_error, it.message ?: "error"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.userListRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
