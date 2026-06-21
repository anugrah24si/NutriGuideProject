package com.example.nutriguideproject.ui.log

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.common.MainNav

/**
 * Halaman Log Makanan.
 * Pengguna memilih waktu makan, mengisi input manual, atau menambah dari
 * database makanan populer. Saat ini berupa tampilan statis tanpa penyimpanan data.
 */
class LogActivity : AppCompatActivity() {

    private lateinit var mealTabs: List<TextView>
    private lateinit var btnAddManual: Button

    /** Waktu makan yang sedang dipilih (default: Sarapan). */
    private var selectedMeal: String = "Sarapan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log)
        applySystemBarInsets()

        btnAddManual = findViewById(R.id.btnAddManual)
        setupMealTabs()
        setupFoodDatabase()
        MainNav.setup(this, MainNav.Tab.LOG)

        selectMeal("Sarapan")
    }

    private fun setupMealTabs() {
        mealTabs = listOf(
            findViewById(R.id.tabSarapan),
            findViewById(R.id.tabSiang),
            findViewById(R.id.tabMalam),
            findViewById(R.id.tabSnack)
        )
        mealTabs.forEach { tab ->
            tab.setOnClickListener { selectMeal(tab.text.toString()) }
        }
    }

    private fun selectMeal(meal: String) {
        selectedMeal = meal
        mealTabs.forEach { tab ->
            val active = tab.text.toString() == meal
            tab.setBackgroundResource(if (active) R.drawable.bg_tab_active else android.R.color.transparent)
            tab.setTextColor(getColor(if (active) R.color.text_dark else R.color.text_muted))
            tab.setTypeface(null, if (active) Typeface.BOLD else Typeface.NORMAL)
        }
        btnAddManual.text = getString(R.string.log_add_to, meal)
    }

    private fun setupFoodDatabase() {
        val foods = mapOf(
            R.id.btnAddFood1 to "Nasi Putih",
            R.id.btnAddFood2 to "Ayam Goreng",
            R.id.btnAddFood3 to "Telur Rebus",
            R.id.btnAddFood4 to "Sayur Bayam",
            R.id.btnAddFood5 to "Tempe Goreng",
            R.id.btnAddFood6 to "Pisang"
        )
        foods.forEach { (id, name) ->
            findViewById<View>(id).setOnClickListener { addFood(name) }
        }
    }

    private fun addFood(name: String) {
        val message = getString(R.string.log_added, name, selectedMeal)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.logRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
