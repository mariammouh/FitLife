package com.example.fitlife

data class UserProfile(
    val user_id: Int,
    val full_name: String,
    val username: String,
    val email: String,
    val height_cm: String?,       // JSON returns "167.00" as string
    val weight_kg: String?,       // JSON returns "60.50" as string
    val goal_weight_kg: String?,  // JSON returns "55.00" as string
    val goal: String?,
    val fitness_level: String?,
    val gender: String?,
    val fitness_state: String?,  // underworked/beginner/active/healthy/athletic/overworked
    val nbr_tries: Int,
    val is_paying: Int,           // JSON returns 1 or 0
    val created_at: String?
)

data class UserResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserProfile? = null
)

data class TodayStats(
    val total_minutes: Int,
    val total_calories: Int,
    val total_activities: Int
)

data class DailyCalories(
    val day: String,
    val calories: Int
)

data class ActivityTypeCount(
    val activity_type: String,
    val count: Int
)

data class MoodEntry(
    val activity_type: String,
    val mood_after: String,
    val count: Int
)

data class StreakData(
    val active_days: Int
)

data class BestWorkout(
    val activity_name: String,
    val calories_burned: Int,
    val duration_minutes: Int,
    val activity_date: String
)

data class WeekComparison(
    val this_week: Int,
    val last_week: Int
)

data class AvgByType(
    val activity_type: String,
    val avg_duration: Float,
    val avg_calories: Float
)

data class DashboardStatsResponse(
    val success: Boolean,
    val message: String? = null,
    val today: TodayStats?,
    val monthly_calories: List<DailyCalories>?,
    val activity_types: List<ActivityTypeCount>?,
    val mood_data: List<MoodEntry>?,
    val streak: StreakData?,
    val best_workout: BestWorkout?,
    val week_comparison: WeekComparison?,
    val avg_by_type: List<AvgByType>?
)

data class UpdateUserResponse(
    val success: Boolean,
    val message: String? = null
)

data class UseTryResponse(
    val success: Boolean,
    val nbr_tries: Int,
    val is_paying: Boolean,
    val locked: Boolean,
    val message: String? = null
)
