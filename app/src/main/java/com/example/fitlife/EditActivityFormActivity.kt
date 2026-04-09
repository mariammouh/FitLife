package com.example.fitlife

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class EditActivityFormActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var actType: AutoCompleteTextView
    private lateinit var etDuration: TextInputEditText
    private lateinit var etDistance: TextInputEditText
    private lateinit var etCalories: TextInputEditText
    private lateinit var etAvgHeart: TextInputEditText
    private lateinit var etMaxHeart: TextInputEditText
    private lateinit var etSets: TextInputEditText
    private lateinit var etReps: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var actIntensity: AutoCompleteTextView
    private lateinit var actMoodBefore: AutoCompleteTextView
    private lateinit var actMoodAfter: AutoCompleteTextView
    private lateinit var etLocation: TextInputEditText
    private lateinit var etNotes: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etStartTime: TextInputEditText
    private lateinit var etEndTime: TextInputEditText
    private lateinit var btnUpdate: Button

    private var activityId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit_activity)

        etName = findViewById(R.id.etName)
        actType = findViewById(R.id.actType)
        etDuration = findViewById(R.id.etDuration)
        etDistance = findViewById(R.id.etDistance)
        etCalories = findViewById(R.id.etCalories)
        etAvgHeart = findViewById(R.id.etAvgHeart)
        etMaxHeart = findViewById(R.id.etMaxHeart)
        etSets = findViewById(R.id.etSets)
        etReps = findViewById(R.id.etReps)
        etWeight = findViewById(R.id.etWeight)
        actIntensity = findViewById(R.id.actIntensity)
        actMoodBefore = findViewById(R.id.actMoodBefore)
        actMoodAfter = findViewById(R.id.actMoodAfter)
        etLocation = findViewById(R.id.etLocation)
        etNotes = findViewById(R.id.etNotes)
        etDate = findViewById(R.id.etDate)
        etStartTime = findViewById(R.id.etStartTime)
        etEndTime = findViewById(R.id.etEndTime)
        btnUpdate = findViewById(R.id.btnUpdate)

        activityId = intent.getStringExtra("activity_id") ?: ""

        setupDropdowns()
        fillData()
        setupDatePicker()
        setupTimePickers()

        btnUpdate.setOnClickListener {
            updateActivity()
        }
    }

    private fun setupDropdowns() {
        val types = arrayOf("Cardio", "Yoga", "HIIT", "Strength", "Flexibility", "Other")
        val intensityList = arrayOf("low", "moderate", "high")
        val moods = arrayOf("neutral", "good", "great")

        actType.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, types))
        actIntensity.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, intensityList))
        actMoodBefore.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, moods))
        actMoodAfter.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, moods))
    }

    private fun fillData() {
        etName.setText(intent.getStringExtra("name") ?: "")
        actType.setText(intent.getStringExtra("type") ?: "", false)
        etDuration.setText(intent.getStringExtra("duration") ?: "")
        etDistance.setText(intent.getStringExtra("distance") ?: "")
        etCalories.setText(intent.getStringExtra("calories") ?: "")
        etAvgHeart.setText(intent.getStringExtra("avg_heart") ?: "")
        etMaxHeart.setText(intent.getStringExtra("max_heart") ?: "")
        etSets.setText(intent.getStringExtra("sets") ?: "")
        etReps.setText(intent.getStringExtra("reps") ?: "")
        etWeight.setText(intent.getStringExtra("weight") ?: "")
        actIntensity.setText(intent.getStringExtra("intensity") ?: "", false)
        actMoodBefore.setText(intent.getStringExtra("mood_before") ?: "", false)
        actMoodAfter.setText(intent.getStringExtra("mood_after") ?: "", false)
        etLocation.setText(intent.getStringExtra("location") ?: "")
        etNotes.setText(intent.getStringExtra("notes") ?: "")
        etDate.setText(intent.getStringExtra("date") ?: "")
        etStartTime.setText(intent.getStringExtra("start") ?: "")
        etEndTime.setText(intent.getStringExtra("end") ?: "")
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val date = "%04d-%02d-%02d".format(year, month + 1, day)
                    etDate.setText(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun setupTimePickers() {
        etStartTime.setOnClickListener { showTimePicker(etStartTime) }
        etEndTime.setOnClickListener { showTimePicker(etEndTime) }
    }

    private fun showTimePicker(field: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, hour, minute ->
                val time = "%02d:%02d:00".format(hour, minute)
                field.setText(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun updateActivity() {
        if (activityId.isEmpty()) {
            Toast.makeText(this, "Activity ID introuvable", Toast.LENGTH_LONG).show()
            return
        }

        if (etName.text.toString().trim().isEmpty()) {
            etName.error = "Required"
            etName.requestFocus()
            return
        }

        if (actType.text.toString().trim().isEmpty()) {
            actType.error = "Required"
            actType.requestFocus()
            return
        }

        if (etDuration.text.toString().trim().isEmpty()) {
            etDuration.error = "Required"
            etDuration.requestFocus()
            return
        }

        val url = "http://10.0.2.2/fitlife/update_activity.php"

        val request = object : StringRequest(
            Request.Method.POST,
            url,
            { response ->
                val result = response.trim()

                if (result.equals("success", ignoreCase = true)) {
                    Toast.makeText(
                        this,
                        "Modification enregistrée avec succès",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, ActivitiesListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Échec de modification : $result", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Erreur réseau : ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["activity_id"] = activityId
                params["name"] = etName.text.toString().trim()
                params["type"] = actType.text.toString().trim()
                params["duration"] = etDuration.text.toString().trim()
                params["distance"] = etDistance.text.toString().trim()
                params["calories"] = etCalories.text.toString().trim()
                params["avg_heart"] = etAvgHeart.text.toString().trim()
                params["max_heart"] = etMaxHeart.text.toString().trim()
                params["sets"] = etSets.text.toString().trim()
                params["reps"] = etReps.text.toString().trim()
                params["weight"] = etWeight.text.toString().trim()
                params["intensity"] = actIntensity.text.toString().trim()
                params["mood_before"] = actMoodBefore.text.toString().trim()
                params["mood_after"] = actMoodAfter.text.toString().trim()
                params["location"] = etLocation.text.toString().trim()
                params["notes"] = etNotes.text.toString().trim()
                params["date"] = etDate.text.toString().trim()
                params["start"] = etStartTime.text.toString().trim()
                params["end"] = etEndTime.text.toString().trim()
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }
}