package com.example.nutriguideproject.ui.admin

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.local.SessionManager
import com.example.nutriguideproject.data.model.Food
import com.example.nutriguideproject.data.remote.RealtimeTable
import com.example.nutriguideproject.data.repository.FoodRepository

/**
 * Halaman admin "Kelola Data Makanan" — CRUD nyata ke Supabase.
 * Daftar dimuat dari tabel foods (RecyclerView). Form tambah/edit tampil inline.
 */
class ManageFoodActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var repository: FoodRepository
    private lateinit var adapter: FoodAdapter
    private var realtime: RealtimeTable? = null

    private lateinit var formCard: View
    private lateinit var formTitle: TextView
    private lateinit var formName: EditText
    private lateinit var formCategory: TextView
    private lateinit var formPortion: EditText
    private lateinit var formCalorie: EditText
    private lateinit var formProtein: EditText
    private lateinit var formCarbs: EditText
    private lateinit var formFat: EditText
    private lateinit var formFiber: EditText
    private lateinit var tvDbCount: TextView
    private lateinit var tvEmpty: TextView

    /** null = mode tambah; berisi id = mode edit. */
    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_food)
        applySystemBarInsets()

        session = SessionManager(this)
        repository = FoodRepository(session)

        bindViews()
        setupRecycler()
        setupForm()
        AdminNav.setup(this, AdminNav.Tab.FOOD)
    }

    override fun onStart() {
        super.onStart()
        loadFoods()
        // Realtime: daftar ikut ter-update saat ada perubahan di tabel foods.
        realtime = RealtimeTable("foods", session.accessToken) { loadFoods() }.also { it.connect() }
    }

    override fun onStop() {
        super.onStop()
        realtime?.disconnect()
        realtime = null
    }

    private fun bindViews() {
        formCard = findViewById(R.id.formCard)
        formTitle = findViewById(R.id.formTitle)
        formName = findViewById(R.id.formName)
        formCategory = findViewById(R.id.formCategory)
        formPortion = findViewById(R.id.formPortion)
        formCalorie = findViewById(R.id.formCalorie)
        formProtein = findViewById(R.id.formProtein)
        formCarbs = findViewById(R.id.formCarbs)
        formFat = findViewById(R.id.formFat)
        formFiber = findViewById(R.id.formFiber)
        tvDbCount = findViewById(R.id.tvDbCount)
        tvEmpty = findViewById(R.id.tvEmpty)
    }

    private fun setupRecycler() {
        adapter = FoodAdapter(
            onEdit = { showEditForm(it) },
            onDelete = { confirmDelete(it) }
        )
        findViewById<RecyclerView>(R.id.foodRecycler).apply {
            layoutManager = LinearLayoutManager(this@ManageFoodActivity)
            adapter = this@ManageFoodActivity.adapter
        }
    }

    private fun loadFoods() {
        repository.getFoods { result ->
            result.onSuccess { foods ->
                adapter.submit(foods)
                tvDbCount.text = getString(R.string.manage_count_fmt, foods.size)
                tvEmpty.visibility = if (foods.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure {
                toast(getString(R.string.manage_load_error, it.message ?: "error"))
            }
        }
    }

    // --- Form ---

    private fun setupForm() {
        findViewById<Button>(R.id.btnAddNewFood).setOnClickListener {
            if (formCard.visibility == View.VISIBLE) {
                hideForm()
            } else {
                showAddForm()
            }
        }

        formCategory.setOnClickListener {
            val options = resources.getStringArray(R.array.form_category_options)
            AlertDialog.Builder(this)
                .setTitle(R.string.form_category)
                .setItems(options) { _, which ->
                    formCategory.text = options[which]
                    formCategory.setTextColor(getColor(R.color.text_dark))
                }
                .show()
        }

        findViewById<Button>(R.id.btnCancelFood).setOnClickListener { hideForm() }
        findViewById<Button>(R.id.btnSaveFood).setOnClickListener { saveFood() }
    }

    private fun showAddForm() {
        editingId = null
        formTitle.setText(R.string.form_title)
        formName.setText("")
        formCategory.text = ""
        formPortion.setText("")
        formCalorie.setText("")
        formProtein.setText("")
        formCarbs.setText("")
        formFat.setText("")
        formFiber.setText("")
        formCard.visibility = View.VISIBLE
    }

    private fun showEditForm(food: Food) {
        editingId = food.id
        formTitle.setText(R.string.form_title_edit)
        formName.setText(food.name)
        formCategory.text = food.category
        formCategory.setTextColor(getColor(R.color.text_dark))
        formPortion.setText(food.serving)
        formCalorie.setText(food.calories.toString())
        formProtein.setText(food.proteinG.toString())
        formCarbs.setText(food.carbsG.toString())
        formFat.setText(food.fatG.toString())
        formFiber.setText(food.fiberG.toString())
        formCard.visibility = View.VISIBLE
    }

    private fun hideForm() {
        formCard.visibility = View.GONE
        editingId = null
    }

    private fun saveFood() {
        val name = formName.text.toString().trim()
        val category = formCategory.text.toString().trim()
        val serving = formPortion.text.toString().trim()
        if (name.isEmpty() || category.isEmpty()) {
            toast("Nama dan kategori wajib diisi")
            return
        }
        val food = Food(
            id = editingId,
            name = name,
            category = category,
            serving = serving,
            calories = formCalorie.text.toString().toIntOrNull() ?: 0,
            proteinG = formProtein.text.toString().toDoubleOrNull() ?: 0.0,
            carbsG = formCarbs.text.toString().toDoubleOrNull() ?: 0.0,
            fatG = formFat.text.toString().toDoubleOrNull() ?: 0.0,
            fiberG = formFiber.text.toString().toDoubleOrNull() ?: 0.0
        )

        val onDone: (Result<Unit>) -> Unit = { result ->
            result.onSuccess {
                toast(getString(R.string.form_saved))
                hideForm()
                loadFoods()
            }.onFailure { toast(it.message ?: "Gagal menyimpan") }
        }

        if (editingId == null) repository.addFood(food, onDone)
        else repository.updateFood(food, onDone)
    }

    private fun confirmDelete(food: Food) {
        AlertDialog.Builder(this)
            .setTitle(R.string.manage_delete_title)
            .setMessage(getString(R.string.manage_delete_message, food.name))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.manage_delete_title) { _, _ ->
                val id = food.id ?: return@setPositiveButton
                repository.deleteFood(id) { result ->
                    result.onSuccess {
                        toast(getString(R.string.manage_deleted, food.name))
                        loadFoods()
                    }.onFailure { toast(it.message ?: "Gagal menghapus") }
                }
            }
            .show()
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.manageFoodRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
