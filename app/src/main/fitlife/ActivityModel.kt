package com.example.fitnessapp

data class ActivityModel(
    val activity_id: Int,
    val activity_name: String,
    val activity_type: String,
    val duration_minutes: Int,
    val calories_burned: Int,
    val avg_heart_rate: Int,
    val distance_km: Double?,
    val intensity: String,
    val mood_after: String,
    val activity_date: String,
    val start_time: String,
    val end_time: String
)