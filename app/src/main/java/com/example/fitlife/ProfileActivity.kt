package com.example.fitlife

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadProfileData()

        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            saveProfileData()
        }

        findViewById<Button>(R.id.btnGoToAi).setOnClickListener {
            Toast.makeText(this, "AI Coach Strategy coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileData() {
        RetrofitClient.instance.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val user = response.body()?.user ?: return
                findViewById<EditText>(R.id.etProfileName).setText(user.username)
                // Additional fields like age and goal could be loaded here if they exist in UserProfile
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileData() {
        val newName = findViewById<EditText>(R.id.etProfileName).text.toString()
        // Here you would typically call an update API
        Toast.makeText(this, "Profile updated (Simulated)", Toast.LENGTH_SHORT).show()
        finish()
    }
}