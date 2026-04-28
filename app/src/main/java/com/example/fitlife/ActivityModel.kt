package com.example.fitlife

import com.google.gson.annotations.SerializedName

data class ActivityModel(
    val id: String?,
    val name: String?,
    val type: String?,
    val duration: String?,
    val distance: String?,
    val calories: String?,
    @SerializedName("avg_heart") val avgHeart: String?,
    @SerializedName("max_heart") val maxHeart: String?,
    val sets: String?,
    val reps: String?,
    val weight: String?,
    val intensity: String?,
    @SerializedName("mood_before") val moodBefore: String?,
    @SerializedName("mood_after") val moodAfter: String?,
    val notes: String?,
    val location: String?,
    val date: String?,
    val start: String?,
    val end: String?
)
