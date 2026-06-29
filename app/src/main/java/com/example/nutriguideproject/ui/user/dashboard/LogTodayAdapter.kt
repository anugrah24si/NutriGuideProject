package com.example.nutriguideproject.ui.user.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriguideproject.R
import com.example.nutriguideproject.data.model.FoodLog
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Adapter daftar "Log Makanan Hari Ini" pada Dashboard user.
 */
class LogTodayAdapter : RecyclerView.Adapter<LogTodayAdapter.VH>() {

    private val items = mutableListOf<FoodLog>()

    fun submit(logs: List<FoodLog>) {
        items.clear()
        items.addAll(logs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_today, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMeal: TextView = itemView.findViewById(R.id.tvMeal)
        private val tvFood: TextView = itemView.findViewById(R.id.tvFood)
        private val tvKcal: TextView = itemView.findViewById(R.id.tvKcal)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(log: FoodLog) {
            tvMeal.text = log.mealType
            tvFood.text = log.name
            tvKcal.text = "${log.calories} kkal"
            tvTime.text = timeFromIso(log.loggedAt)
        }

        private fun timeFromIso(iso: String?): String {
            if (iso.isNullOrBlank() || iso.length < 19) return "--:--"
            return runCatching {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(iso.substring(0, 19))
                val out = SimpleDateFormat("HH:mm", Locale.US) // zona lokal perangkat
                if (date != null) out.format(date) else "--:--"
            }.getOrDefault("--:--")
        }
    }
}
