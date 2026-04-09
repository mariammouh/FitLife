package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class AddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_activity)

        val btnCreateActivity = findViewById<LinearLayout>(R.id.btnCreateActivity)

        btnCreateActivity.setOnClickListener {
            val intent = Intent(this, AddActivityFormActivity::class.java)
            startActivity(intent)
        }
    }
}