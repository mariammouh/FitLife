package com.example.fitlife

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitlife.databinding.ActivityProfileBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private var userId: Int = -1
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarProfile)
        binding.toolbarProfile.setNavigationOnClickListener { finish() }

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupDropdowns()
        setupDatePicker()
        loadProfileData()

        binding.btnSaveProfile.setOnClickListener {
            saveProfileData()
        }
    }

    private fun setupDropdowns() {
        // Gender (Only Male and Female)
        val genders = arrayOf("Male", "Female")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, genders)
        binding.etProfileGender.setAdapter(genderAdapter)

        // Goal
        val goals = arrayOf("Lose Weight", "Build Muscle", "Maintain", "Improve Endurance")
        val goalAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, goals)
        binding.etProfileGoal.setAdapter(goalAdapter)

        // Fitness Level
        val levels = arrayOf("Beginner", "Intermediate", "Advanced")
        val levelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, levels)
        binding.etProfileFitnessLevel.setAdapter(levelAdapter)
    }

    private fun setupDatePicker() {
        binding.etProfileDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                val date = String.format(Locale.US, "%d-%02d-%02d", y, m + 1, d)
                binding.etProfileDob.setText(date)
            }, year, month, day).show()
        }
    }

    private fun loadProfileData() {
        RetrofitClient.instance.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val user = response.body()?.user ?: return
                binding.etProfileUsername.setText(user.username)
                binding.etProfileEmail.setText(user.email)
                binding.etProfilePhone.setText(user.phone)
                binding.etProfileDob.setText(user.date_of_birth)
                binding.etProfileGender.setText(user.gender, false)
                binding.etProfileHeight.setText(user.height_cm)
                binding.etProfileStartWeight.setText(user.start_weight_kg)
                binding.etProfileCurrentWeight.setText(user.current_weight_kg)
                binding.etProfileGoalWeight.setText(user.goal_weight_kg)
                binding.etProfileGoal.setText(user.goal, false)
                binding.etProfileFitnessLevel.setText(user.fitness_level, false)
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfileData() {
        val updates = mapOf(
            "user_id" to userId.toString(),
            "username" to binding.etProfileUsername.text.toString(),
            "email" to binding.etProfileEmail.text.toString(),
            "phone" to binding.etProfilePhone.text.toString(),
            "dob" to binding.etProfileDob.text.toString(),
            "gender" to binding.etProfileGender.text.toString(),
            "height" to binding.etProfileHeight.text.toString(),
            "start_weight" to binding.etProfileStartWeight.text.toString(),
            "current_weight" to binding.etProfileCurrentWeight.text.toString(),
            "goal_weight" to binding.etProfileGoalWeight.text.toString(),
            "goal" to binding.etProfileGoal.text.toString(),
            "fitness_level" to binding.etProfileFitnessLevel.text.toString()
        )

        RetrofitClient.instance.updateUser(updates).enqueue(object : Callback<UpdateUserResponse> {
            override fun onResponse(call: Call<UpdateUserResponse>, response: Response<UpdateUserResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@ProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
