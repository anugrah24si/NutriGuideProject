package com.example.nutriguideproject.ui.user.menu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.user.common.MainNav
import com.example.nutriguideproject.ui.user.recommendation.RecommendationActivity

/**
 * Halaman Rekomendasi Menu.
 * Pengguna mengisi data pribadi lalu menekan "Lihat Rekomendasi".
 * Saat ini berupa tampilan statis tanpa logika rekomendasi nyata.
 */
class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        applySystemBarInsets()

        setupSelectors()
        setupActions()
        MainNav.setup(this, MainNav.Tab.MENU)
    }

    private fun setupSelectors() {
        val activity = findViewById<TextView>(R.id.selectActivity)
        val goal = findViewById<TextView>(R.id.selectGoal)

        activity.setOnClickListener {
            showOptions(
                title = getString(R.string.menu_activity),
                options = resources.getStringArray(R.array.menu_activity_options),
                target = activity
            )
        }
        goal.setOnClickListener {
            showOptions(
                title = getString(R.string.menu_goal),
                options = resources.getStringArray(R.array.menu_goal_options),
                target = goal
            )
        }
    }

    private fun showOptions(title: String, options: Array<String>, target: TextView) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(options) { _, which ->
                target.text = options[which]
                target.setTextColor(getColor(R.color.text_dark))
            }
            .show()
    }

    private fun setupActions() {
        findViewById<Button>(R.id.btnSeeReco).setOnClickListener {
            startActivity(Intent(this, RecommendationActivity::class.java))
        }
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.menuRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
