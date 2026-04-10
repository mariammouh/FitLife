package com.example.fitlife

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import kotlin.math.abs

class DashboardActivity : AppCompatActivity() {

    private var userId = 0
    private var currentUser: UserProfile? = null

    // ── Purple/Blue palette — consistent throughout ──
    private val C_PURPLE_DEEP   = Color.parseColor("#5B2FBE")
    private val C_PURPLE_MID    = Color.parseColor("#7B4FE9")
    private val C_PURPLE_LIGHT  = Color.parseColor("#9B6FF9")
    private val C_PURPLE_SOFT   = Color.parseColor("#BFA0FF")
    private val C_BLUE_DEEP     = Color.parseColor("#1A4FA8")
    private val C_BLUE_MID      = Color.parseColor("#3B6FD4")
    private val C_BLUE_LIGHT    = Color.parseColor("#6B9FFF")
    private val C_BLUE_SOFT     = Color.parseColor("#A0C4FF")
    private val C_INDIGO        = Color.parseColor("#4B0082")
    private val C_VIOLET        = Color.parseColor("#8A2BE2")

    // Chart color sets
    private val PIE_COLORS = listOf(
        Color.parseColor("#5B2FBE"), Color.parseColor("#7B4FE9"),
        Color.parseColor("#9B6FF9"), Color.parseColor("#BFA0FF"),
        Color.parseColor("#1A4FA8"), Color.parseColor("#3B6FD4"),
        Color.parseColor("#6B9FFF"), Color.parseColor("#A0C4FF")
    )
    private val BAR_COLORS = listOf(
        Color.parseColor("#7B4FE9"), Color.parseColor("#5B2FBE"),
        Color.parseColor("#9B6FF9"), Color.parseColor("#3B6FD4"),
        Color.parseColor("#6B9FFF"), Color.parseColor("#BFA0FF")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        userId = intent.getIntExtra("USER_ID", 1)

        loadUserProfile(userId)
        loadDashboardStats(userId)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home     -> true
                R.id.nav_activity -> true // TODO
                R.id.nav_profile  -> {
                    try {
                        val i = Intent(this, AvatarActivity::class.java)
                        i.putExtra("USER_ID", currentUser?.user_id ?: userId)
                        startActivity(i)
                    } catch (e: Exception) {
                        Log.e("DASH", "Failed to start AvatarActivity: ${e.message}")
                        AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Could not load Avatar page. Your device might not support the required 3D features.")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                    true
                }
                R.id.nav_settings -> {
                    val i = Intent(this, SettingsActivity::class.java)
                    i.putExtra("USER_ID",      currentUser?.user_id ?: userId)
                    i.putExtra("USERNAME",     currentUser?.username ?: "User")
                    i.putExtra("NBR_TRIES",    currentUser?.nbr_tries ?: 0)
                    i.putExtra("IS_PAYING",    (currentUser?.is_paying ?: 0) == 1)  // Int to Boolean
                    i.putExtra("GOAL_WEIGHT",  currentUser?.goal_weight_kg?.toFloatOrNull() ?: 0f)
                    i.putExtra("FITNESS_GOAL", currentUser?.goal ?: "lose_weight")
                    startActivity(i)
                    true
                }
                else -> false
            }
        }
    }

    // ════════════════════════════════════════
    // LOAD USER → check tries/paying
    // ════════════════════════════════════════
    private fun loadUserProfile(userId: Int) {
        RetrofitClient.instance.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                Log.d("DASH", "getUser HTTP ${response.code()} body=${response.body()}")
                val user = response.body()?.user ?: return
                runOnUiThread {
                    // ── Greeting ──
                    findViewById<TextView>(R.id.tvGreeting).text = "Hi, ${user.username}."

                    // ── Weight progress ──
                    val current = user.weight_kg?.toFloatOrNull() ?: 0f
                    val goal    = user.goal_weight_kg?.toFloatOrNull() ?: 0f
                    updateWeightProgress(current, current, goal)

                    // Store user data for nav use
                    currentUser = user

                    // ── Consume one try (skipped if paying) ──
                    consumeTry(userId, user)
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("DASH", "getUser FAILED: ${t.message}")
            }
        })
    }

    // ════════════════════════════════════════
    // CONSUME ONE TRY ON EACH DASHBOARD VISIT
    // ════════════════════════════════════════
    private fun consumeTry(userId: Int, user: UserProfile) {
        val body = mapOf("user_id" to userId.toString())
        RetrofitClient.instance.useTry(body).enqueue(object : Callback<UseTryResponse> {
            override fun onResponse(call: Call<UseTryResponse>, response: Response<UseTryResponse>) {
                val result = response.body() ?: return
                Log.d("DASH", "useTry → tries=${result.nbr_tries} locked=${result.locked}")
                runOnUiThread {
                    if (result.locked) {
                        lockDashboard()
                    }
                    // Update currentUser with fresh nbr_tries for Settings display
                    currentUser = user
                }
            }
            override fun onFailure(call: Call<UseTryResponse>, t: Throwable) {
                Log.e("DASH", "useTry FAILED: ${t.message}")
                // Fail open — don't lock if network fails
            }
        })
    }

    // ════════════════════════════════════════
    // LOCK DASHBOARD — tries exceeded
    // ════════════════════════════════════════
    private fun lockDashboard() {
        // Hide all chart/stats cards
        val idsToHide = listOf(
            R.id.cardTodayStats, R.id.cardWeekComparison, R.id.cardStreak,
            R.id.cardCaloriesChart, R.id.cardPieChart, R.id.cardMoodChart,
            R.id.cardAvgCalories, R.id.cardBestWorkout, R.id.cardWeightProgress
        )
        idsToHide.forEach { id ->
            findViewById<View>(id)?.visibility = View.GONE
        }

        // Show locked overlay
        findViewById<View>(R.id.layoutLocked)?.visibility = View.VISIBLE

        // Show dialog
        AlertDialog.Builder(this)
            .setTitle("⚠️ Trials Exhausted")
            .setMessage("You have used all your free trials.\n\nUpgrade to Premium to keep accessing all your stats and features.")
            .setPositiveButton("See Premium") { _, _ ->
                val i = Intent(this, SettingsActivity::class.java)
                i.putExtra("USER_ID", userId)
                i.putExtra("IS_PAYING", false)
                startActivity(i)
            }
            .setNegativeButton("Later", null)
            .setCancelable(false)
            .show()
    }

    // ════════════════════════════════════════
    // LOAD STATS
    // ════════════════════════════════════════
    private fun loadDashboardStats(userId: Int) {
        RetrofitClient.instance.getDashboardStats(userId).enqueue(object : Callback<DashboardStatsResponse> {
            override fun onResponse(call: Call<DashboardStatsResponse>, response: Response<DashboardStatsResponse>) {
                Log.d("DASH", "getStats HTTP ${response.code()} body=${response.body()}")
                val data = response.body() ?: return
                if (!data.success) { Log.e("DASH", "stats failed: ${data.message}"); return }
                runOnUiThread {
                    data.today?.let             { updateTodayStats(it) }
                    data.monthly_calories?.let  { drawCaloriesLineChart(it) }
                    data.activity_types?.let    { drawActivityPieChart(it) }
                    data.mood_data?.let         { drawMoodChart(it) }
                    data.streak?.let            { updateStreak(it) }
                    data.best_workout?.let      { updateBestWorkout(it) }
                    data.week_comparison?.let   { updateWeekComparison(it) }
                    data.avg_by_type?.let       { drawAvgCaloriesBarChart(it) }
                }
            }
            override fun onFailure(call: Call<DashboardStatsResponse>, t: Throwable) {
                Log.e("DASH", "getStats FAILED: ${t.message}")
            }
        })
    }

    // ════════════════════════════════════════
    // TODAY STATS
    // ════════════════════════════════════════
    private fun updateTodayStats(today: TodayStats) {
        findViewById<TextView>(R.id.tvCaloriesValue).text    = "${today.total_calories} kcal"
        findViewById<TextView>(R.id.tvWorkoutTimeValue).text = "${today.total_minutes} min"
        findViewById<TextView>(R.id.tvStepsValue).text       = "${today.total_activities} sessions"
    }

    // ════════════════════════════════════════
    // LINE CHART — Monthly Calories
    // ════════════════════════════════════════
    private fun drawCaloriesLineChart(data: List<DailyCalories>) {
        val chart = findViewById<LineChart>(R.id.lineChartCalories)
        if (data.isEmpty()) { chart.visibility = View.GONE; return }

        val entries = data.mapIndexed { i, d -> Entry(i.toFloat(), d.calories.toFloat()) }
        val labels  = data.map { it.day.takeLast(5) }

        val dataSet = LineDataSet(entries, "Calories / day").apply {
            color        = C_PURPLE_MID
            setCircleColor(C_PURPLE_LIGHT)
            lineWidth    = 2.5f
            circleRadius = 4f
            setDrawFilled(true)
            fillColor    = C_PURPLE_DEEP
            fillAlpha    = 60
            valueTextSize = 0f
            mode         = LineDataSet.Mode.CUBIC_BEZIER
            highLightColor = C_BLUE_LIGHT  // valid property in MPAndroidChart
        }
        chart.apply {
            this.data = LineData(dataSet)
            description.isEnabled = false
            legend.textColor      = Color.WHITE
            legend.textSize       = 11f
            setBackgroundColor(Color.TRANSPARENT)
            axisLeft.apply {
                textColor   = Color.parseColor("#CCCCCC")
                gridColor   = Color.parseColor("#2A2A40")
                axisLineColor = Color.TRANSPARENT
                textSize    = 10f
            }
            axisRight.isEnabled = false
            xAxis.apply {
                textColor       = Color.parseColor("#CCCCCC")
                gridColor       = Color.TRANSPARENT
                position        = XAxis.XAxisPosition.BOTTOM
                valueFormatter  = IndexAxisValueFormatter(labels)
                granularity     = 1f
                labelRotationAngle = -45f
                textSize        = 9f
            }
            animateX(900)
            invalidate()
        }
    }

    // ════════════════════════════════════════
    // PIE CHART — Activity Types
    // ════════════════════════════════════════
    private fun drawActivityPieChart(data: List<ActivityTypeCount>) {
        val chart = findViewById<PieChart>(R.id.pieChartTypes)
        if (data.isEmpty()) { chart.visibility = View.GONE; return }

        val entries = data.map { PieEntry(it.count.toFloat(), it.activity_type.replaceFirstChar { c -> c.uppercase() }) }
        val dataSet = PieDataSet(entries, "").apply {
            colors         = PIE_COLORS
            valueTextColor = Color.WHITE
            valueTextSize  = 11f
            sliceSpace     = 3f
            selectionShift = 8f
        }
        chart.apply {
            this.data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled     = true
            setHoleColor(Color.parseColor("#1A1A2E"))
            holeRadius            = 48f
            transparentCircleRadius = 52f
            setTransparentCircleColor(Color.parseColor("#2A2A3E"))
            setDrawCenterText(true)
            centerText            = "Types"
            setCenterTextColor(Color.WHITE)
            setCenterTextSize(14f)
            legend.textColor      = Color.WHITE
            legend.textSize       = 10f
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(10f)
            animateY(1000)
            invalidate()
        }
    }

    // ════════════════════════════════════════
    // BAR CHART — Mood After Workout
    // ════════════════════════════════════════
    private fun drawMoodChart(data: List<MoodEntry>) {
        val chart = findViewById<BarChart>(R.id.barChartMood)
        if (data.isEmpty()) { chart.visibility = View.GONE; return }

        val moodScore = mapOf("great" to 5f, "good" to 4f, "ok" to 3f, "tired" to 2f, "bad" to 1f)
        val grouped   = data.groupBy { it.activity_type }
        val types     = grouped.keys.toList()

        val entries = types.mapIndexed { i, type ->
            val list  = grouped[type] ?: emptyList()
            val total = list.sumOf { (moodScore[it.mood_after] ?: 3f).toDouble() * it.count }
            val count = list.sumOf { it.count.toDouble() }
            BarEntry(i.toFloat(), if (count > 0) (total / count).toFloat() else 3f)
        }

        val dataSet = BarDataSet(entries, "Avg mood (5 = great)").apply {
            colors         = BAR_COLORS
            valueTextColor = Color.WHITE
            valueTextSize  = 10f
        }
        chart.apply {
            this.data = BarData(dataSet)
            applyChartStyle(this)
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 5.5f
                textColor   = Color.parseColor("#CCCCCC")
                gridColor   = Color.parseColor("#2A2A40")
            }
            xAxis.valueFormatter = IndexAxisValueFormatter(types.map { it.take(7) })
            animateY(800); invalidate()
        }
    }

    // ════════════════════════════════════════
    // BAR CHART — Avg Calories by Type
    // ════════════════════════════════════════
    private fun drawAvgCaloriesBarChart(data: List<AvgByType>) {
        val chart = findViewById<BarChart>(R.id.barChartAvgCalories)
        if (data.isEmpty()) { chart.visibility = View.GONE; return }

        val types   = data.map { it.activity_type.take(7) }
        val entries = data.mapIndexed { i, d -> BarEntry(i.toFloat(), d.avg_calories) }

        val dataSet = BarDataSet(entries, "Avg kcal / type").apply {
            // Gradient shades of purple
            colors = listOf(
                C_PURPLE_DEEP, C_PURPLE_MID, C_PURPLE_LIGHT,
                C_BLUE_DEEP, C_BLUE_MID, C_BLUE_LIGHT
            )
            valueTextColor = Color.WHITE
            valueTextSize  = 10f
        }
        chart.apply {
            this.data = BarData(dataSet)
            applyChartStyle(this)
            axisLeft.apply {
                axisMinimum = 0f
                textColor   = Color.parseColor("#CCCCCC")
                gridColor   = Color.parseColor("#2A2A40")
            }
            xAxis.valueFormatter = IndexAxisValueFormatter(types)
            animateY(800); invalidate()
        }
    }

    // Shared chart style helper
    private fun applyChartStyle(chart: BarChart) {
        chart.apply {
            description.isEnabled = false
            legend.textColor      = Color.WHITE
            legend.textSize       = 11f
            setBackgroundColor(Color.TRANSPARENT)
            axisRight.isEnabled   = false
            xAxis.apply {
                textColor       = Color.parseColor("#CCCCCC")
                gridColor       = Color.TRANSPARENT
                position        = XAxis.XAxisPosition.BOTTOM
                granularity     = 1f
                textSize        = 10f
            }
        }
    }

    // ════════════════════════════════════════
    // STREAK / BEST / WEEK COMPARISON
    // ════════════════════════════════════════
    private fun updateStreak(streak: StreakData) {
        findViewById<TextView>(R.id.tvStreakValue).text = "${streak.active_days} / 7 days 🔥"
    }

    private fun updateBestWorkout(best: BestWorkout) {
        findViewById<TextView>(R.id.tvBestWorkoutName).text     = best.activity_name
        findViewById<TextView>(R.id.tvBestWorkoutCalories).text = "${best.calories_burned} kcal"
        findViewById<TextView>(R.id.tvBestWorkoutDate).text     = best.activity_date
    }

    private fun updateWeekComparison(week: WeekComparison) {
        val diff  = week.this_week - week.last_week
        val arrow = if (diff >= 0) "↑" else "↓"
        val color = if (diff >= 0) C_PURPLE_LIGHT else C_BLUE_LIGHT
        val tv    = findViewById<TextView>(R.id.tvWeekComparison)
        tv.text = "$arrow ${abs(diff)} kcal vs semaine dernière"
        tv.setTextColor(color)
    }

    // ════════════════════════════════════════
    // WEIGHT PROGRESS
    // ════════════════════════════════════════
    private fun updateWeightProgress(startWeight: Float, currentWeight: Float, goalWeight: Float) {
        val isLosing = goalWeight < startWeight
        findViewById<TextView>(R.id.tvGoalType).text = if (isLosing) "🔥 Losing Weight" else "💪 Building Muscle"
        findViewById<TextView>(R.id.tvKgRemaining).text = if (isLosing)
            "${String.format("%.1f", currentWeight - goalWeight)} kg à perdre"
        else
            "${String.format("%.1f", goalWeight - currentWeight)} kg à gagner"

        val totalChange   = abs(startWeight - goalWeight)
        val currentChange = abs(startWeight - currentWeight)
        val progress = if (totalChange > 0) ((currentChange / totalChange) * 100).toInt().coerceIn(0, 100) else 0

        findViewById<ProgressBar>(R.id.progressWeight).progress = progress
        findViewById<TextView>(R.id.tvProgressPercent).text     = "$progress%"
        findViewById<TextView>(R.id.tvStartWeight).text         = "${startWeight.toInt()} kg"
        findViewById<TextView>(R.id.tvCurrentWeight).text       = "${currentWeight.toInt()} kg"
        findViewById<TextView>(R.id.tvGoalWeight).text          = "${goalWeight.toInt()} kg"
    }
}
