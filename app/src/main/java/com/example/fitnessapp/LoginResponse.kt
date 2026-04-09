package com.example.fitnessapp

data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val user_id: Int? = null,
    val full_name: String? = null,
    val username: String? = null,
    val email: String? = null,
    val weight_kg: Double? = null,
    val goal_weight_kg: Double? = null,
    val goal: String? = null,
    val nbr_tries: Int? = null,
    val is_paying: Boolean? = null
)