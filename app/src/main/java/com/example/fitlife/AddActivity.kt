package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class AddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_activity)

        // Retrieve user ID from intent
        val userId = intent.getIntExtra("USER_ID", -1)

        val btnCreateActivity = findViewById<LinearLayout>(R.id.btnCreateActivity)

        btnCreateActivity.setOnClickListener {
            val intent = Intent(this, AddActivityFormActivity::class.java)
            // Pass the user ID forward
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
    }
}