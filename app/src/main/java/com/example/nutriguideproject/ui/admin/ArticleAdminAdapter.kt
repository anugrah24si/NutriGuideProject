package com.example.nutriguideproject.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.model.Article

/**
 * Adapter daftar artikel untuk halaman admin Kelola Artikel.
 */
class ArticleAdminAdapter(
    private val onEdit: (Article) -> Unit,
    private val onDelete: (Article) -> Unit
) : RecyclerView.Adapter<ArticleAdminAdapter.VH>() {

    private val items = mutableListOf<Article>()

    fun submit(articles: List<Article>) {
        items.clear()
        items.addAll(articles)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article_admin, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvPreview: TextView = itemView.findViewById(R.id.tvPreview)
        private val tvReadTime: TextView = itemView.findViewById(R.id.tvReadTime)
        private val btnEdit: View = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: View = itemView.findViewById(R.id.btnDelete)

        fun bind(article: Article) {
            val ctx = itemView.context
            tvTitle.text = article.title
            tvCategory.text = article.category
            tvPreview.text = article.content
            tvReadTime.text = article.readTime

            if (article.isPublished) {
                tvStatus.text = "Published"
                tvStatus.setBackgroundResource(R.drawable.bg_tag)
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.badge_green_text))
            } else {
                tvStatus.text = "Draft"
                tvStatus.setBackgroundResource(R.drawable.bg_badge_gray)
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
            }

            btnEdit.setOnClickListener { onEdit(article) }
            btnDelete.setOnClickListener { onDelete(article) }
        }
    }
}
