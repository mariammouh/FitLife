package com.example.fitlife

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fitlife.databinding.ActivityLoginBinding
import com.example.fitlife.databinding.ActivitySignupBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    private val api = RetrofitClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun showLogin() {
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvGoToSignUp.setOnClickListener { showSignUp() }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginToServer(email, pass)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSignUp() {
        val binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Date Picker for DOB
        binding.etDobSignUp.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                binding.etDobSignUp.setText(date)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Dropdowns
        val genders = arrayOf("Male", "Female")
        binding.etGenderSignUp.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, genders)
        )

        val goals = arrayOf("Lose Weight", "Build Muscle", "Maintain", "Improve Endurance")
        binding.etGoalSignUp.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, goals)
        )

        val levels = arrayOf("Beginner", "Intermediate", "Advanced")
        binding.etFitnessLevelSignUp.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, levels)
        )

        binding.btnSignUp.setOnClickListener {
            val username = binding.etUsernameSignUp.text.toString()
            val email = binding.etEmailSignUp.text.toString()
            val pass = binding.etPasswordSignUp.text.toString()
            val phone = binding.etPhoneSignUp.text.toString()
            val dob = binding.etDobSignUp.text.toString()
            val gender = binding.etGenderSignUp.text.toString()
            val height = binding.etHeight.text.toString()
            val startWeight = binding.etWeight.text.toString()
            val goalWeight = binding.etGoalWeightSignUp.text.toString()
            val goal = binding.etGoalSignUp.text.toString()
            val level = binding.etFitnessLevelSignUp.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && username.isNotEmpty()) {
                // fullName is empty, and currentWeight is set to startWeight initially
                registerOnServer("", username, email, pass, phone, gender, dob, height, startWeight, startWeight, goalWeight, goal, level)
            } else {
                Toast.makeText(this, "Missing required fields (Username, Email, Password)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginToServer(email: String, pass: String) {
        api.loginUser(email, pass).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val fullResult = response.body()?.string()?.trim() ?: ""
                Log.d("LOGIN_DEBUG", "Server: $fullResult")

                if (response.isSuccessful) {
                    val startIdx = fullResult.indexOf("{")
                    val endIdx = if (startIdx != -1) fullResult.indexOf("}", startIdx) else -1

                    if (startIdx != -1 && endIdx != -1) {
                        try {
                            val jsonStr = fullResult.substring(startIdx, endIdx + 1)
                            val jsonObj = JSONObject(jsonStr)
                            
                            val isSuccess = jsonObj.optBoolean("success", false)
                            val userId = jsonObj.optInt("user_id", jsonObj.optInt("id", -1))

                            if (isSuccess && userId > 0) {
                                val intent = Intent(this@MainActivity, DashboardActivity::class.java)
                                intent.putExtra("USER_ID", userId)
                                startActivity(intent)
                                finish()
                            } else {
                                val msg = jsonObj.optString("message", "Login failed")
                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("LOGIN_DEBUG", "Parse error", e)
                        }
                    } else {
                        if (fullResult.contains("wrong_password")) {
                            Toast.makeText(this@MainActivity, "Wrong password", Toast.LENGTH_SHORT).show()
                        } else if (fullResult.contains("not_found")) {
                            Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Error: $fullResult", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("LOGIN_DEBUG", "Network error", t)
                Toast.makeText(this@MainActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerOnServer(
        name: String, uname: String, email: String, pass: String, phone: String, 
        gender: String, dob: String, h: String, sw: String, cw: String, gw: String, 
        goal: String, level: String
    ) {
        api.registerUser(name, uname, email, pass, phone, gender, dob, h, sw, cw, gw, goal, level).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Account created! Please login", Toast.LENGTH_LONG).show()
                    showLogin()
                } else {
                    Toast.makeText(this@MainActivity, "Error creating account", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Connection Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}