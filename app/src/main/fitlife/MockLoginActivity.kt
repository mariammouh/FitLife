package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MockLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No layout needed — this screen is invisible, just fetches user and redirects

        loginUser("jane@email.com", "hashed_pw_1")
    }

    private fun loginUser(email: String, password: String) {
        RetrofitClient.instance.login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {

                    val intent = Intent(this@MockLoginActivity, DashboardActivity::class.java)
                    intent.putExtra("USER_ID",     body.user_id ?: 1)
                    intent.putExtra("USERNAME",    body.username ?: "User")
                    intent.putExtra("FULL_NAME",   body.full_name ?: "User")
                    intent.putExtra("WEIGHT",      body.weight_kg?.toFloat() ?: 0f)
                    intent.putExtra("GOAL_WEIGHT", body.goal_weight_kg?.toFloat() ?: 0f)
                    intent.putExtra("GOAL",        body.goal ?: "maintain")
                    intent.putExtra("NBR_TRIES",   body.nbr_tries ?: 0)
                    intent.putExtra("IS_PAYING",   body.is_paying ?: false)

                    // ── Go to dashboard and remove MockLogin from back stack ──
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } else {
                    Log.e("LOGIN", "Failed: ${body?.message}")
                    // If login fails, still go to dashboard with default test data
                    goToDashboardWithDefaults()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LOGIN", "API Error: ${t.message}")
                // If API is unreachable, go to dashboard with hardcoded test data
                goToDashboardWithDefaults()
            }
        })
    }

    private fun goToDashboardWithDefaults() {
        val intent = Intent(this@MockLoginActivity, DashboardActivity::class.java)
        intent.putExtra("USER_ID", 1)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
