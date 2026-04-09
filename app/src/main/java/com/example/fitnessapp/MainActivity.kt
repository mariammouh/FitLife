package com.example.fitnessapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("USERNAME", "maria mimi") // replace with real username from DB/login
        startActivity(intent)
    }
}
