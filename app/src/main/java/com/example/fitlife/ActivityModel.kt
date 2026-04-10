package com.example.fitlife

data class ActivityModel(
    val id: String,
    val name: String,
    val type: String,
    val duration: String,
    val distance: String,
    val calories: String,
    val avgHeart: String,
    val maxHeart: String,
    val sets: String,
    val reps: String,
    val weight: String,
    val intensity: String,
    val moodBefore: String,
    val moodAfter: String,
    val notes: String,
    val location: String,
    val date: String,
    val start: String,
    val end: String
)
