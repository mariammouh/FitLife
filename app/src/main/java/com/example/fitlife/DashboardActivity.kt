package com.example.fitlife

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private var userId: Int = -1
    private var currentUser: UserProfile? = null

    // Colors
    private val C_PURPLE_DEEP   = Color.parseColor("#3B1F9E")
    private val C_PURPLE_MID    = Color.parseColor("#7B4FE9")
    private val PIE_COLORS = listOf(
        Color.parseColor("#5B2FBE"), Color.parseColor("#7B4FE9"),
        Color.parseColor("#9B6FF9"), Color.parseColor("#BFA0FF"),
        Color.parseColor("#1A4FA8"), Color.parseColor("#3B6FD4"),
        Color.parseColor("#6B9FFF"), Color.parseColor("#A0C4FF")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // IMPORTANT: Get the ID from the Intent
        userId = intent.getIntExtra("USER_ID", -1)
        Log.d("DASHBOARD_DEBUG", "Dashboard opened with USER_ID: $userId")

        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadUserProfile(userId)
        loadDashboardStats(userId)

        setupNavigation()
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home     -> true
                R.id.nav_activity -> {
                    val i = Intent(this, ActivitiesListActivity::class.java)
                    i.putExtra("USER_ID", userId)
                    startActivity(i)
                    true
                }
                R.id.nav_profile  -> {
                    val i = Intent(this, AvatarActivity::class.java)
                    i.putExtra("USER_ID", userId)
                    startActivity(i)
                    true
                }
                R.id.nav_settings -> {
                    val i = Intent(this, SettingsActivity::class.java)
                    i.putExtra("USER_ID", userId)
                    i.putExtra("USERNAME", currentUser?.username ?: "")
                    startActivity(i)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserProfile(userId: Int) {
        RetrofitClient.instance.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val user = response.body()?.user
                if (user != null) {
                    currentUser = user
                    findViewById<TextView>(R.id.tvGreeting).text = "Hi, ${user.username}."
                    consumeTry(userId)
                } else {
                    Log.e("DASHBOARD_DEBUG", "User profile is NULL for ID: $userId")
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("DASHBOARD_DEBUG", "loadUserProfile FAILED: ${t.message}")
            }
        })
    }

    private fun consumeTry(userId: Int) {
        val body = mapOf("user_id" to userId.toString())
        RetrofitClient.instance.useTry(body).enqueue(object : Callback<UseTryResponse> {
            override fun onResponse(call: Call<UseTryResponse>, response: Response<UseTryResponse>) {
                if (response.body()?.locked == true) lockDashboard()
            }
            override fun onFailure(call: Call<UseTryResponse>, t: Throwable) {}
        })
    }

    private fun lockDashboard() {
        findViewById<View>(R.id.layoutLocked)?.visibility = View.VISIBLE
    }

    private fun loadDashboardStats(userId: Int) {
        RetrofitClient.instance.getDashboardStats(userId).enqueue(object : Callback<DashboardStatsResponse> {
            override fun onResponse(call: Call<DashboardStatsResponse>, response: Response<DashboardStatsResponse>) {
                val data = response.body() ?: return
                if (data.success) {
                    data.today?.let { updateTodayStats(it) }
                    data.monthly_calories?.let { drawCaloriesLineChart(it) }
                    data.activity_types?.let { drawActivityPieChart(it) }
                }
            }
            override fun onFailure(call: Call<DashboardStatsResponse>, t: Throwable) {
                Log.e("DASHBOARD_DEBUG", "loadDashboardStats FAILED: ${t.message}")
            }
        })
    }

    private fun updateTodayStats(today: TodayStats) {
        findViewById<TextView>(R.id.tvCaloriesValue).text    = "${today.total_calories} kcal"
        findViewById<TextView>(R.id.tvWorkoutTimeValue).text = "${today.total_minutes} min"
        findViewById<TextView>(R.id.tvStepsValue).text       = "${today.total_activities} sessions"
    }

    private fun drawCaloriesLineChart(data: List<DailyCalories>) {
        val chart = findViewById<LineChart>(R.id.lineChartCalories) ?: return
        if (data.isEmpty()) return
        val entries = data.mapIndexed { i, d -> Entry(i.toFloat(), d.calories.toFloat()) }
        val dataSet = LineDataSet(entries, "Calories").apply {
            color = C_PURPLE_MID
            setDrawFilled(true)
            fillColor = C_PURPLE_DEEP
        }
        chart.data = LineData(dataSet)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.day.takeLast(5) })
        chart.invalidate()
    }

    private fun drawActivityPieChart(data: List<ActivityTypeCount>) {
        val chart = findViewById<PieChart>(R.id.pieChartTypes) ?: return
        if (data.isEmpty()) return
        val entries = data.map { PieEntry(it.count.toFloat(), it.activity_type) }
        val dataSet = PieDataSet(entries, "").apply { colors = PIE_COLORS }
        chart.data = PieData(dataSet)
        chart.invalidate()
    }
}
