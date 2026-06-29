package com.example.nutriguideproject.ui.admin

import android.app.Activity
import android.content.Intent
import android.view.View
import com.example.nutriguideproject.R

/**
 * Pengatur bottom navigation khusus area Admin
 * (Dashboard, Data Makanan, Pengaturan).
 */
object AdminNav {

    enum class Tab { DASHBOARD, FOOD, SETTINGS }

    fun setup(activity: Activity, current: Tab) {
        bind(activity, R.id.adminNavDashboard, Tab.DASHBOARD, current, AdminDashboardActivity::class.java)
        bind(activity, R.id.adminNavFood, Tab.FOOD, current, ManageFoodActivity::class.java)
        bind(activity, R.id.adminNavSettings, Tab.SETTINGS, current, AdminSettingsActivity::class.java)
    }

    private fun bind(
        activity: Activity,
        viewId: Int,
        tab: Tab,
        current: Tab,
        target: Class<out Activity>
    ) {
        activity.findViewById<View>(viewId)?.setOnClickListener {
            if (tab == current) return@setOnClickListener
            val intent = Intent(activity, target).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }
}
