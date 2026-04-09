package com.example.fitlife

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActivityDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val txtDetailName = findViewById<TextView>(R.id.txtDetailName)
        val txtDetailType = findViewById<TextView>(R.id.txtDetailType)
        val txtDetailDate = findViewById<TextView>(R.id.txtDetailDate)
        val txtDetailDuration = findViewById<TextView>(R.id.txtDetailDuration)
        val txtDetailDistance = findViewById<TextView>(R.id.txtDetailDistance)
        val txtDetailCalories = findViewById<TextView>(R.id.txtDetailCalories)
        val txtDetailHeart = findViewById<TextView>(R.id.txtDetailHeart)
        val txtDetailStrength = findViewById<TextView>(R.id.txtDetailStrength)
        val txtDetailIntensity = findViewById<TextView>(R.id.txtDetailIntensity)
        val txtDetailMood = findViewById<TextView>(R.id.txtDetailMood)
        val txtDetailLocation = findViewById<TextView>(R.id.txtDetailLocation)
        val txtDetailTime = findViewById<TextView>(R.id.txtDetailTime)
        val txtDetailNotes = findViewById<TextView>(R.id.txtDetailNotes)

        txtDetailName.text = intent.getStringExtra("name") ?: ""

        txtDetailType.text = "Type: ${intent.getStringExtra("type") ?: ""}"

        txtDetailDate.text = "Date: ${intent.getStringExtra("date") ?: ""}"

        txtDetailDuration.text = "Duration: ${intent.getStringExtra("duration") ?: ""} min"

        txtDetailDistance.text = "Distance: ${intent.getStringExtra("distance") ?: ""} km"

        txtDetailCalories.text = "Calories: ${intent.getStringExtra("calories") ?: ""}"

        txtDetailHeart.text =
            "Heart Rate: Avg ${intent.getStringExtra("avg_heart") ?: ""}, Max ${intent.getStringExtra("max_heart") ?: ""}"

        txtDetailStrength.text =
            "Sets / Reps / Weight: ${intent.getStringExtra("sets") ?: ""} / ${intent.getStringExtra("reps") ?: ""} / ${intent.getStringExtra("weight") ?: ""} kg"

        txtDetailIntensity.text = "Intensity: ${intent.getStringExtra("intensity") ?: ""}"

        txtDetailMood.text =
            "Mood: ${intent.getStringExtra("mood_before") ?: ""} → ${intent.getStringExtra("mood_after") ?: ""}"

        txtDetailLocation.text = "Location: ${intent.getStringExtra("location") ?: ""}"

        txtDetailTime.text =
            "Time: ${intent.getStringExtra("start") ?: ""} - ${intent.getStringExtra("end") ?: ""}"

        txtDetailNotes.text = "Notes: ${intent.getStringExtra("notes") ?: ""}"
    }
}