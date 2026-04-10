package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val api = RetrofitClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLogin()
    }

    private fun showLogin() {
        setContentView(R.layout.activity_login)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)

        tvGoToSignUp?.setOnClickListener { showSignUp() }

        btnSignIn?.setOnClickListener {
            val email = etEmail?.text.toString()
            val pass = etPassword?.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginToServer(email, pass)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSignUp() {
        setContentView(R.layout.activity_signup)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmailSignUp)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordSignUp)
        val etHeight = findViewById<TextInputEditText>(R.id.etHeight)
        val etWeight = findViewById<TextInputEditText>(R.id.etWeight)

        btnSignUp?.setOnClickListener {
            val name = etFullName?.text.toString()
            val email = etEmail?.text.toString()
            val pass = etPassword?.text.toString()
            val h = etHeight?.text.toString()
            val w = etWeight?.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                registerOnServer(name, email, pass, h, w)
            } else {
                Toast.makeText(this, "Missing required fields", Toast.LENGTH_SHORT).show()
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

    private fun registerOnServer(name: String, email: String, pass: String, h: String, w: String) {
        api.registerUser(name, email, pass, h, w).enqueue(object : Callback<ResponseBody> {
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
