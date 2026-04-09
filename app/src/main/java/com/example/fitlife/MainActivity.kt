package com.example.fitlife

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    // استعملي الـ IP ديالك لي ف CMD
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.8.105/fitlife_api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ApiService::class.java)

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

        tvGoToSignUp.setOnClickListener { showSignUp() }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                loginToServer(email, pass)
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
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

        btnSignUp.setOnClickListener {
            val name = etFullName.text.toString()
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            val h = etHeight.text.toString()
            val w = etWeight.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                registerOnServer(name, email, pass, h, w)
            } else {
                Toast.makeText(this, "Champs obligatoires manquants", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginToServer(email: String, pass: String) {
        api.loginUser(email, pass).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val result = response.body()?.string() ?: ""
                if (result.contains("success")) {
                    Toast.makeText(this@MainActivity, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                    showProfile(email)
                } else {
                    Toast.makeText(this@MainActivity, "Identifiants incorrects", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Erreur serveur", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerOnServer(name: String, email: String, pass: String, h: String, w: String) {
        api.registerUser(name, email, pass, h, w).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Toast.makeText(this@MainActivity, "Compte créé ! Connectez-vous", Toast.LENGTH_LONG).show()
                showLogin()
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    private fun showProfile(userEmail: String) {
        setContentView(R.layout.activity_profile)

        val etName = findViewById<EditText>(R.id.etProfileName)
        val etAge = findViewById<EditText>(R.id.etProfileAge)
        val etGoal = findViewById<EditText>(R.id.etProfileGoal)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        // زر جديد باش نمشيو للذكاء الاصطناعي (زيدي هاد الزر فـ XML ديال البروفايل)
        val btnGoToAI = findViewById<Button>(R.id.btnGoToAi)

        btnGoToAI.setOnClickListener { showAiStrategy(userEmail) }

        api.getProfile(userEmail).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val jsonStr = response.body()?.string() ?: ""
                if (jsonStr.isNotEmpty() && !jsonStr.contains("error")) {
                    val jsonObj = JSONObject(jsonStr)
                    etName.setText(jsonObj.optString("full_name"))
                    etAge.setText(jsonObj.optString("age"))
                    etGoal.setText(jsonObj.optString("weekly_goal_minutes"))
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val age = etAge.text.toString()
            val goal = etGoal.text.toString()

            api.updateProfile(userEmail, name, age, goal).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    Toast.makeText(this@MainActivity, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
        }
    }

    // --- دالة الذكاء الاصطناعي (مدمجة دابا وسط الكلاس) ---
    private fun showAiStrategy(userEmail: String) {
        setContentView(R.layout.activity_ai_strategy)

        val tvResponse = findViewById<TextView>(R.id.tvAiResponse)
        val btnGenerate = findViewById<Button>(R.id.btnGenerateAi)
        val pbLoading = findViewById<ProgressBar>(R.id.pbLoading)
        val btnBackToProfile = findViewById<Button>(R.id.btnBackFromAi) // زر للرجوع

        btnBackToProfile.setOnClickListener { showProfile(userEmail) }

        btnGenerate.setOnClickListener {
            pbLoading.visibility = View.VISIBLE
            tvResponse.text = "Génération en cours..."
            btnGenerate.isEnabled = false

            api.getAiStrategy(userEmail).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    pbLoading.visibility = View.GONE
                    btnGenerate.isEnabled = true

                    val result = response.body()?.string() ?: ""
                    try {
                        val jsonObj = JSONObject(result)
                        if (jsonObj.getString("status") == "success") {
                            tvResponse.text = jsonObj.getString("strategy")
                        }
                    } catch (e: Exception) {
                        tvResponse.text = "Erreur de lecture : $result"
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    pbLoading.visibility = View.GONE
                    btnGenerate.isEnabled = true
                    Toast.makeText(this@MainActivity, "Erreur IA: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}