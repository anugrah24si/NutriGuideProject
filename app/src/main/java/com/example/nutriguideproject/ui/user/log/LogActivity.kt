package com.example.nutriguideproject.ui.user.log

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.example.nutriguideproject.data.model.Food
import com.example.nutriguideproject.data.model.FoodLog
import com.example.nutriguideproject.data.remote.RealtimeTable
import com.example.nutriguideproject.data.repository.FoodLogRepository
import com.example.nutriguideproject.data.repository.FoodRepository
import com.example.nutriguideproject.ui.user.common.MainNav
import com.example.nutriguideproject.ui.user.dashboard.LogTodayAdapter

/**
 * Halaman Log Makanan.
 * Daftar "Database Makanan" dimuat dari Supabase (tabel foods) sehingga
 * makanan yang ditambahkan admin otomatis muncul di sini.
 */
class LogActivity : AppCompatActivity() {

    private lateinit var mealTabs: List<TextView>
    private lateinit var btnAddManual: Button
    private lateinit var adapter: UserFoodAdapter
    private lateinit var logTodayAdapter: LogTodayAdapter
    private lateinit var tvEmpty: TextView
    private lateinit var tvEmptyTodayLog: TextView
    private lateinit var repository: FoodRepository
    private var realtime: RealtimeTable? = null
    private val session by lazy { SessionManager(this) }
    private val logRepository by lazy { FoodLogRepository(session) }

    private val allFoods = mutableListOf<Food>()
    private var selectedMeal: String = "Sarapan"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log)
        applySystemBarInsets()

        repository = FoodRepository(session)
        btnAddManual = findViewById(R.id.btnAddManual)
        tvEmpty = findViewById(R.id.tvEmptyLog)
        tvEmptyTodayLog = findViewById(R.id.tvEmptyTodayLog)

        setupMealTabs()
        setupFoodList()
        setupTodayLogList()
        setupSearch()
        setupManualInput()
        MainNav.setup(this, MainNav.Tab.LOG)

        selectMeal("Sarapan")
    }

    override fun onStart() {
        super.onStart()
        loadFoods()
        loadTodayLogs()
        realtime = RealtimeTable("foods", session.accessToken) { loadFoods() }.also { it.connect() }
    }

    override fun onStop() {
        super.onStop()
        realtime?.disconnect()
        realtime = null
    }

    private fun setupFoodList() {
        adapter = UserFoodAdapter(onAdd = { addFoodToLog(it) })
        findViewById<RecyclerView>(R.id.foodRecyclerLog).apply {
            layoutManager = LinearLayoutManager(this@LogActivity)
            adapter = this@LogActivity.adapter
        }
    }

    private fun setupTodayLogList() {
        logTodayAdapter = LogTodayAdapter()
        findViewById<RecyclerView>(R.id.logTodayRecycler).apply {
            layoutManager = LinearLayoutManager(this@LogActivity)
            adapter = logTodayAdapter
        }
    }

    private fun loadTodayLogs() {
        logRepository.getTodayLogs { result ->
            result.onSuccess { logs ->
                logTodayAdapter.submit(logs)
                tvEmptyTodayLog.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
            }
            // gagal/offline: biarkan tampilan seadanya
        }
    }

    private fun loadFoods() {
        repository.getFoods { result ->
            result.onSuccess { foods ->
                allFoods.clear()
                allFoods.addAll(foods)
                applyFilter(currentQuery())
            }.onFailure {
                toast(getString(R.string.log_load_error, it.message ?: "error"))
            }
        }
    }

    private fun setupSearch() {
        findViewById<EditText>(R.id.inputSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter(s?.toString().orEmpty())
            }
        })
    }

    private fun currentQuery(): String = findViewById<EditText>(R.id.inputSearch).text?.toString().orEmpty()

    private fun applyFilter(query: String) {
        val filtered = if (query.isBlank()) {
            allFoods
        } else {
            allFoods.filter { it.name.contains(query.trim(), ignoreCase = true) }
        }
        adapter.submit(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
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

    /** Menambah makanan dari database ke log harian (tersimpan di Supabase). */
    private fun addFoodToLog(food: Food) {
        val uid = session.userId
        if (uid == null) {
            toast("Sesi tidak ditemukan. Silakan login ulang.")
            return
        }
        val log = FoodLog(
            userId = uid,
            foodId = food.id,
            mealType = selectedMeal,
            name = food.name,
            portion = food.serving,
            calories = food.calories,
            proteinG = food.proteinG,
            carbsG = food.carbsG,
            fatG = food.fatG
        )
        logRepository.addLog(log) { result ->
            result.onSuccess {
                toast(getString(R.string.log_added, food.name, selectedMeal))
                loadTodayLogs()
            }
                .onFailure { toast(it.message ?: "Gagal menyimpan log") }
        }
    }

    private fun setupManualInput() {
        btnAddManual.setOnClickListener { addManualLog() }
    }

    private fun addManualLog() {
        val uid = session.userId
        if (uid == null) {
            toast("Sesi tidak ditemukan. Silakan login ulang.")
            return
        }
        val name = findViewById<EditText>(R.id.inputFoodName).text.toString().trim()
        if (name.isEmpty()) {
            toast("Nama makanan wajib diisi")
            return
        }
        val portion = findViewById<EditText>(R.id.inputPortion).text.toString().trim()
        val log = FoodLog(
            userId = uid,
            mealType = selectedMeal,
            name = name,
            portion = portion,
            calories = findViewById<EditText>(R.id.inputCalorie).text.toString().toIntOrNull() ?: 0,
            proteinG = findViewById<EditText>(R.id.inputProtein).text.toString().toDoubleOrNull() ?: 0.0,
            carbsG = findViewById<EditText>(R.id.inputCarbs).text.toString().toDoubleOrNull() ?: 0.0,
            fatG = findViewById<EditText>(R.id.inputFat).text.toString().toDoubleOrNull() ?: 0.0
        )
        logRepository.addLog(log) { result ->
            result.onSuccess {
                toast(getString(R.string.log_added, name, selectedMeal))
                clearManualInput()
                loadTodayLogs()
            }.onFailure { toast(it.message ?: "Gagal menyimpan log") }
        }
    }

    private fun clearManualInput() {
        findViewById<EditText>(R.id.inputFoodName).setText("")
        findViewById<EditText>(R.id.inputCalorie).setText("")
        findViewById<EditText>(R.id.inputPortion).setText("")
        findViewById<EditText>(R.id.inputProtein).setText("")
        findViewById<EditText>(R.id.inputCarbs).setText("")
        findViewById<EditText>(R.id.inputFat).setText("")
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.logRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
