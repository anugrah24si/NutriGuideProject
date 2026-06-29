package com.example.nutriguideproject.ui.user.recommendation

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.user.common.MainNav
import com.example.nutriguideproject.ui.user.profile.ProfileActivity

/**
 * Halaman hasil Rekomendasi Menu.
 * Dibuka dari tombol "Lihat Rekomendasi" pada halaman Menu.
 * Menampilkan rekomendasi menu harian (statis) berdasarkan target kalori.
 */
class RecommendationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recommendation)
        applySystemBarInsets()

        // "Ubah Data Pribadi" membuka halaman Profil.
        findViewById<Button>(R.id.btnEditData).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        MainNav.setup(this, MainNav.Tab.MENU)
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.recoRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
