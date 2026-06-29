package com.example.nutriguideproject.ui.user.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.model.Food

/**
 * Adapter daftar makanan (read-only) untuk halaman Log user.
 * Tiap item punya tombol "+" untuk menambah ke log.
 */
class UserFoodAdapter(
    private val onAdd: (Food) -> Unit
) : RecyclerView.Adapter<UserFoodAdapter.VH>() {

    private val items = mutableListOf<Food>()

    fun submit(foods: List<Food>) {
        items.clear()
        items.addAll(foods)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_log_db, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvServing: TextView = itemView.findViewById(R.id.tvServing)
        private val tvKkal: TextView = itemView.findViewById(R.id.tvKkal)
        private val tvProtein: TextView = itemView.findViewById(R.id.tvProtein)
        private val tvCarbs: TextView = itemView.findViewById(R.id.tvCarbs)
        private val tvFat: TextView = itemView.findViewById(R.id.tvFat)
        private val btnAdd: View = itemView.findViewById(R.id.btnAdd)

        fun bind(food: Food) {
            tvName.text = food.name
            tvServing.text = food.serving
            tvKkal.text = food.calories.toString()
            tvProtein.text = gram(food.proteinG)
            tvCarbs.text = gram(food.carbsG)
            tvFat.text = gram(food.fatG)
            btnAdd.setOnClickListener { onAdd(food) }
        }

        private fun gram(value: Double): String {
            val text = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
            return "${text}g"
        }
    }
}
