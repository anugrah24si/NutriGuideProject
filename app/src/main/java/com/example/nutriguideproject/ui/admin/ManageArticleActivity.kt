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
import com.example.nutriguideproject.data.model.Article
import com.example.nutriguideproject.data.repository.ArticleRepository

/**
 * Halaman admin "Kelola Artikel" — CRUD artikel edukasi ke Supabase.
 * Dibuka dari aksi cepat "Buat Artikel Edukasi" pada Admin Dashboard.
 */
class ManageArticleActivity : AppCompatActivity() {

    private val session by lazy { SessionManager(this) }
    private val repository by lazy { ArticleRepository(session) }
    private lateinit var adapter: ArticleAdminAdapter

    private lateinit var formCard: View
    private lateinit var formTitle: TextView
    private lateinit var fTitle: EditText
    private lateinit var fCategory: EditText
    private lateinit var fContent: EditText
    private lateinit var fReadtime: EditText
    private lateinit var fAuthor: EditText
    private lateinit var fStatus: TextView
    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView

    private var editingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_article)
        applySystemBarInsets()

        bindViews()
        setupRecycler()
        setupForm()
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    override fun onStart() {
        super.onStart()
        loadArticles()
    }

    private fun bindViews() {
        formCard = findViewById(R.id.maFormCard)
        formTitle = findViewById(R.id.maFormTitle)
        fTitle = findViewById(R.id.formMaTitle)
        fCategory = findViewById(R.id.formMaCategory)
        fContent = findViewById(R.id.formMaContent)
        fReadtime = findViewById(R.id.formMaReadtime)
        fAuthor = findViewById(R.id.formMaAuthor)
        fStatus = findViewById(R.id.formMaStatus)
        tvCount = findViewById(R.id.tvMaCount)
        tvEmpty = findViewById(R.id.tvMaEmpty)
    }

    private fun setupRecycler() {
        adapter = ArticleAdminAdapter(
            onEdit = { showEditForm(it) },
            onDelete = { confirmDelete(it) }
        )
        findViewById<RecyclerView>(R.id.articleRecycler).apply {
            layoutManager = LinearLayoutManager(this@ManageArticleActivity)
            adapter = this@ManageArticleActivity.adapter
        }
    }

    private fun loadArticles() {
        repository.getAll { result ->
            result.onSuccess { articles ->
                adapter.submit(articles)
                tvCount.text = getString(R.string.ma_count_fmt, articles.size)
                tvEmpty.visibility = if (articles.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure {
                toast(getString(R.string.ma_load_error, it.message ?: "error"))
            }
        }
    }

    private fun setupForm() {
        findViewById<Button>(R.id.btnAddNewArticle).setOnClickListener {
            if (formCard.visibility == View.VISIBLE) hideForm() else showAddForm()
        }

        fStatus.setOnClickListener {
            val options = resources.getStringArray(R.array.ma_status_options)
            AlertDialog.Builder(this)
                .setTitle(R.string.ma_field_status)
                .setItems(options) { _, which -> fStatus.text = options[which] }
                .show()
        }

        findViewById<Button>(R.id.btnMaCancel).setOnClickListener { hideForm() }
        findViewById<Button>(R.id.btnMaSave).setOnClickListener { save() }
    }

    private fun showAddForm() {
        editingId = null
        formTitle.setText(R.string.ma_form_title)
        fTitle.setText("")
        fCategory.setText("")
        fContent.setText("")
        fReadtime.setText("")
        fAuthor.setText("")
        fStatus.text = "Draft"
        formCard.visibility = View.VISIBLE
    }

    private fun showEditForm(article: Article) {
        editingId = article.id
        formTitle.setText(R.string.ma_form_title_edit)
        fTitle.setText(article.title)
        fCategory.setText(article.category)
        fContent.setText(article.content)
        fReadtime.setText(article.readTime)
        fAuthor.setText(article.author)
        fStatus.text = if (article.isPublished) "Published" else "Draft"
        formCard.visibility = View.VISIBLE
    }

    private fun hideForm() {
        formCard.visibility = View.GONE
        editingId = null
    }

    private fun save() {
        val title = fTitle.text.toString().trim()
        if (title.isEmpty()) {
            toast("Judul wajib diisi")
            return
        }
        val status = if (fStatus.text.toString() == "Published") "published" else "draft"
        val article = Article(
            id = editingId,
            title = title,
            category = fCategory.text.toString().trim().ifBlank { "Nutrisi" },
            content = fContent.text.toString().trim(),
            readTime = fReadtime.text.toString().trim().ifBlank { "1 min read" },
            author = fAuthor.text.toString().trim().ifBlank { "Admin" },
            status = status,
            createdBy = if (editingId == null) session.userId else null
        )

        val onDone: (Result<Unit>) -> Unit = { result ->
            result.onSuccess {
                toast(getString(R.string.ma_saved))
                hideForm()
                loadArticles()
            }.onFailure { toast(it.message ?: "Gagal menyimpan") }
        }

        if (editingId == null) repository.addArticle(article, onDone)
        else repository.updateArticle(article, onDone)
    }

    private fun confirmDelete(article: Article) {
        AlertDialog.Builder(this)
            .setTitle(R.string.ma_delete_title)
            .setMessage(getString(R.string.ma_delete_msg, article.title))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.ma_delete_title) { _, _ ->
                val id = article.id ?: return@setPositiveButton
                repository.deleteArticle(id) { result ->
                    result.onSuccess {
                        toast(getString(R.string.ma_deleted, article.title))
                        loadArticles()
                    }.onFailure { toast(it.message ?: "Gagal menghapus") }
                }
            }
            .show()
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun applySystemBarInsets() {
        val root = findViewById<View>(R.id.manageArticleRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, bars.bottom)
            insets
        }
    }
}
