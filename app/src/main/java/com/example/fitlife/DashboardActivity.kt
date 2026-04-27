package com.example.fitlife

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.abs

class DashboardActivity : AppCompatActivity() {

    private var userId: Int = -1
    private var currentUser: UserProfile? = null

    // Colors
    private val C_PURPLE_DEEP   = Color.parseColor("#3B1F9E")
    private val C_PURPLE_MID    = Color.parseColor("#7B4FE9")
    private val C_VIOLET_SOFT   = Color.parseColor("#9B6FF9")
    private val C_LAVENDER_BRIGHT = Color.parseColor("#BFA0FF")

    private val PIE_COLORS = listOf(
        Color.parseColor("#5B2FBE"), Color.parseColor("#7B4FE9"),
        Color.parseColor("#9B6FF9"), Color.parseColor("#BFA0FF"),
        Color.parseColor("#1A4FA8"), Color.parseColor("#3B6FD4"),
        Color.parseColor("#6B9FFF"), Color.parseColor("#A0C4FF")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

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
                    i.putExtra("NBR_TRIES", currentUser?.nbr_tries ?: 0)
                    i.putExtra("IS_PAYING", currentUser?.is_paying == 1)
                    i.putExtra("GOAL_WEIGHT", currentUser?.goal_weight_kg?.toFloatOrNull() ?: 0f)
                    i.putExtra("FITNESS_GOAL", currentUser?.goal ?: "")
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
                    findViewById<TextView>(R.id.tvGreeting).text = "Ready to track your progress, ${user.username}?"
                    updateWeightProgress(user)
                    
                    // Logic fix: Only consume try (and potentially lock) if NOT paying
                    if (user.is_paying == 1) {
                        findViewById<View>(R.id.layoutLocked)?.visibility = View.GONE
                        findViewById<View>(R.id.dashboardContent)?.visibility = View.VISIBLE
                    } else {
                        consumeTry(userId)
                    }
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("DASHBOARD_DEBUG", "loadUserProfile FAILED: ${t.message}")
            }
        })
    }

    private fun updateWeightProgress(user: UserProfile) {
        val current = user.current_weight_kg?.toFloatOrNull() ?: 0f
        val start = user.start_weight_kg?.toFloatOrNull() ?: 0f
        val goal = user.goal_weight_kg?.toFloatOrNull() ?: 0f
        
        findViewById<TextView>(R.id.tvCurrentWeight).text = String.format(Locale.US, "%.1f kg", current)
        findViewById<TextView>(R.id.tvGoalWeight).text = String.format(Locale.US, "%.1f kg", goal)
        findViewById<TextView>(R.id.tvStartWeight).text = String.format(Locale.US, "Start: %.1f kg", start)
        
        val totalToLose = start - goal
        val currentLost = start - current
        
        val progress = if (totalToLose > 0) {
            ((currentLost / totalToLose) * 100).toInt().coerceIn(0, 100)
        } else if (current <= goal && goal > 0) {
            100
        } else 0

        findViewById<ProgressBar>(R.id.progressWeight).progress = progress
        findViewById<TextView>(R.id.tvProgressPercent).text = "$progress%"
        
        val remaining = abs(current - goal)
        findViewById<TextView>(R.id.tvKgRemaining).text = String.format(Locale.US, "%.1f kg to go", remaining)
    }

    private fun consumeTry(userId: Int) {
        val body = mapOf("user_id" to userId.toString())
        RetrofitClient.instance.useTry(body).enqueue(object : Callback<UseTryResponse> {
            override fun onResponse(call: Call<UseTryResponse>, response: Response<UseTryResponse>) {
                val data = response.body()
                // Only lock if the backend says so AND user is not premium
                if (data?.locked == true && data.is_paying == false) {
                    lockDashboard()
                } else {
                    findViewById<View>(R.id.layoutLocked)?.visibility = View.GONE
                    findViewById<View>(R.id.dashboardContent)?.visibility = View.VISIBLE
                }
            }
            override fun onFailure(call: Call<UseTryResponse>, t: Throwable) {}
        })
    }

    private fun lockDashboard() {
        findViewById<View>(R.id.layoutLocked)?.visibility = View.VISIBLE
        findViewById<View>(R.id.dashboardContent)?.visibility = View.GONE
    }

    private fun loadDashboardStats(userId: Int) {
        RetrofitClient.instance.getDashboardStats(userId).enqueue(object : Callback<DashboardStatsResponse> {
            override fun onResponse(call: Call<DashboardStatsResponse>, response: Response<DashboardStatsResponse>) {
                val data = response.body() ?: return
                if (data.success) {
                    data.today?.let { updateTodayStats(it) }
                    data.monthly_calories?.let { drawCaloriesLineChart(it) }
                    data.activity_types?.let { drawActivityPieChart(it) }
                    data.mood_data?.let { drawMoodBarChart(it) }
                    data.avg_by_type?.let { drawAvgCaloriesBarChart(it) }
                    
                    data.streak?.let { 
                        findViewById<TextView>(R.id.tvStreakValue).text = "${it.active_days} / 7 days" 
                    }
                    data.week_comparison?.let { 
                        findViewById<TextView>(R.id.tvWeekComparison).text = "${it.this_week} kcal" 
                    }
                    data.best_workout?.let { updateBestWorkout(it) }
                }
            }
            override fun onFailure(call: Call<DashboardStatsResponse>, t: Throwable) {
                Log.e("DASHBOARD_DEBUG", "loadDashboardStats FAILED: ${t.message}")
            }
        })
    }

    private fun updateBestWorkout(best: BestWorkout) {
        findViewById<TextView>(R.id.tvBestWorkoutName).text = best.activity_name
        findViewById<TextView>(R.id.tvBestWorkoutCalories).text = "${best.calories_burned} kcal"
        findViewById<TextView>(R.id.tvBestWorkoutDate).text = best.activity_date
    }

    private fun updateTodayStats(today: TodayStats) {
        findViewById<TextView>(R.id.tvCaloriesValue).text    = "${today.total_calories} kcal"
        findViewById<TextView>(R.id.tvWorkoutTimeValue).text = "${today.total_minutes} min"
        findViewById<TextView>(R.id.tvStepsValue).text       = "${today.total_activities}"
    }

    private fun drawCaloriesLineChart(data: List<DailyCalories>) {
        val chart = findViewById<LineChart>(R.id.lineChartCalories) ?: return
        if (data.isEmpty()) return
        val entries = data.mapIndexed { i, d -> Entry(i.toFloat(), d.calories.toFloat()) }
        val dataSet = LineDataSet(entries, "Calories").apply {
            color = C_PURPLE_MID
            setDrawFilled(true)
            fillColor = C_PURPLE_DEEP
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        chart.data = LineData(dataSet)
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(data.map { it.day.takeLast(5) })
            textColor = Color.WHITE
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
        }
        chart.axisLeft.apply {
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }

    private fun drawActivityPieChart(data: List<ActivityTypeCount>) {
        val chart = findViewById<PieChart>(R.id.pieChartTypes) ?: return
        if (data.isEmpty()) return
        val entries = data.map { PieEntry(it.count.toFloat(), it.activity_type.replaceFirstChar { it.uppercase() }) }
        val dataSet = PieDataSet(entries, "").apply { 
            colors = PIE_COLORS 
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }
        chart.data = PieData(dataSet)
        chart.description.isEnabled = false
        chart.centerText = "Sessions"
        chart.setCenterTextColor(Color.WHITE)
        chart.setHoleColor(Color.TRANSPARENT)
        chart.legend.textColor = Color.WHITE
        chart.invalidate()
    }

    private fun drawMoodBarChart(data: List<MoodEntry>) {
        val chart = findViewById<BarChart>(R.id.barChartMood) ?: return
        if (data.isEmpty()) return

        val avgMoods = data.groupBy { it.activity_type }.mapValues { entry ->
            val totalScore = entry.value.sumOf { moodToScore(it.mood_after) * it.count }
            val totalCount = entry.value.sumOf { it.count }
            totalScore.toFloat() / totalCount
        }

        val entries = avgMoods.values.mapIndexed { i, score -> BarEntry(i.toFloat(), score) }
        val dataSet = BarDataSet(entries, "Mood Score").apply {
            color = C_VIOLET_SOFT
            valueTextColor = Color.WHITE
            setDrawValues(false)
        }
        chart.data = BarData(dataSet)
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(avgMoods.keys.map { it.replaceFirstChar { it.uppercase() } })
            textColor = Color.WHITE
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            isGranularityEnabled = true
        }
        chart.axisLeft.apply {
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }

    private fun moodToScore(mood: String): Int = when(mood.lowercase()) {
        "great" -> 5
        "good" -> 4
        "neutral" -> 3
        "tired" -> 2
        "exhausted" -> 1
        else -> 3
    }

    private fun drawAvgCaloriesBarChart(data: List<AvgByType>) {
        val chart = findViewById<BarChart>(R.id.barChartAvgCalories) ?: return
        if (data.isEmpty()) return

        val entries = data.mapIndexed { i, it -> BarEntry(i.toFloat(), it.avg_calories) }
        val dataSet = BarDataSet(entries, "Avg Kcal").apply {
            color = C_LAVENDER_BRIGHT
            valueTextColor = Color.WHITE
            setDrawValues(false)
        }
        chart.data = BarData(dataSet)
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(data.map { it.activity_type.replaceFirstChar { it.uppercase() } })
            textColor = Color.WHITE
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            isGranularityEnabled = true
        }
        chart.axisLeft.apply {
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.invalidate()
    }
}
