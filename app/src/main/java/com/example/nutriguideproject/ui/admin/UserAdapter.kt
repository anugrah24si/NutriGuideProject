package com.example.nutriguideproject.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.model.Profile

/**
 * Adapter daftar pengguna untuk halaman admin Daftar User.
 */
class UserAdapter : RecyclerView.Adapter<UserAdapter.VH>() {

    private val items = mutableListOf<Profile>()

    fun submit(users: List<Profile>) {
        items.clear()
        items.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInitials: TextView = itemView.findViewById(R.id.tvInitials)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)

        fun bind(p: Profile) {
            val ctx = itemView.context
            tvInitials.text = initials(p.fullName)
            tvName.text = p.fullName
            tvEmail.text = p.email
            tvRole.text = p.role
            if (p.isAdmin) {
                tvRole.setBackgroundResource(R.drawable.bg_tag)
                tvRole.setTextColor(ContextCompat.getColor(ctx, R.color.badge_green_text))
            } else {
                tvRole.setBackgroundResource(R.drawable.bg_badge_gray)
                tvRole.setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
            }
        }

        private fun initials(name: String): String {
            val parts = name.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
            }
        }
    }
}
