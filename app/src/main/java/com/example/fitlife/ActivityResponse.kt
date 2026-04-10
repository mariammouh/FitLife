package com.example.fitlife

data class ActivityResponse(
    val success: Boolean,
    val user_id: Int,
    val count: Int,
    val activities: List<ActivityModel>
)