package com.example.nutriguideproject.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.model.Food

/**
 * Adapter daftar makanan untuk halaman admin Kelola Data Makanan.
 * Menampilkan kartu makanan dengan tombol edit & hapus.
 */
class FoodAdapter(
    private val onEdit: (Food) -> Unit,
    private val onDelete: (Food) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private val items = mutableListOf<Food>()

    fun submit(foods: List<Food>) {
        items.clear()
        items.addAll(foods)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_db, parent, false)
        return FoodViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvServing: TextView = itemView.findViewById(R.id.tvServing)
        private val tvKkal: TextView = itemView.findViewById(R.id.tvKkal)
        private val tvProtein: TextView = itemView.findViewById(R.id.tvProtein)
        private val tvCarbs: TextView = itemView.findViewById(R.id.tvCarbs)
        private val tvFat: TextView = itemView.findViewById(R.id.tvFat)
        private val tvFiber: TextView = itemView.findViewById(R.id.tvFiber)
        private val btnEdit: View = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: View = itemView.findViewById(R.id.btnDelete)

        fun bind(food: Food) {
            tvName.text = food.name
            tvCategory.text = food.category
            tvServing.text = food.serving
            tvKkal.text = food.calories.toString()
            tvProtein.text = gram(food.proteinG)
            tvCarbs.text = gram(food.carbsG)
            tvFat.text = gram(food.fatG)
            tvFiber.text = gram(food.fiberG)
            btnEdit.setOnClickListener { onEdit(food) }
            btnDelete.setOnClickListener { onDelete(food) }
        }

        /** Format gram: hilangkan ".0" bila bilangan bulat. */
        private fun gram(value: Double): String {
            val text = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
            return "${text}g"
        }
    }
}
