package com.example.nutriguideproject.ui.common

import android.app.Activity
import android.content.Intent
import android.view.View
import com.example.nutriguideproject.R
import com.example.nutriguideproject.ui.dashboard.DashboardActivity
import com.example.nutriguideproject.ui.chart.ChartActivity
import com.example.nutriguideproject.ui.log.LogActivity
import com.example.nutriguideproject.ui.menu.MenuActivity
import com.example.nutriguideproject.ui.profile.ProfileActivity

/**
 * Pengatur bottom navigation yang dipakai bersama oleh layar-layar utama
 * (Home, Log, Menu, Grafik, Profil). Menyatukan logika perpindahan tab
 * agar tidak diduplikasi di setiap Activity.
 */
object MainNav {

    enum class Tab { HOME, LOG, MENU, CHART, PROFILE }

    /**
     * Menghubungkan klik tiap item nav pada [activity].
     * [current] menandai tab yang sedang aktif (klik pada tab aktif diabaikan).
     */
    fun setup(activity: Activity, current: Tab) {
        bind(activity, R.id.navHome, Tab.HOME, current, DashboardActivity::class.java)
        bind(activity, R.id.navLog, Tab.LOG, current, LogActivity::class.java)
        bind(activity, R.id.navMenu, Tab.MENU, current, MenuActivity::class.java)
        bind(activity, R.id.navChart, Tab.CHART, current, ChartActivity::class.java)
        bind(activity, R.id.navProfile, Tab.PROFILE, current, ProfileActivity::class.java)
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
