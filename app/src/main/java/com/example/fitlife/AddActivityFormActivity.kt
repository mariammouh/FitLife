package com.example.fitlife

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

import java.util.Calendar

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import android.content.Intent
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddActivityFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_form)

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarForm)
        toolbar.setNavigationOnClickListener { finish() }

        // Champs formulaire
        val etActivityName = findViewById<TextInputEditText>(R.id.etActivityName)
        val etDurationMinutes = findViewById<TextInputEditText>(R.id.etDurationMinutes)
        val etDistanceKm = findViewById<TextInputEditText>(R.id.etDistanceKm)
        val etCaloriesBurned = findViewById<TextInputEditText>(R.id.etCaloriesBurned)
        val etActivityDate = findViewById<TextInputEditText>(R.id.etActivityDate)
        val etStartTime = findViewById<TextInputEditText>(R.id.etStartTime)
        val etEndTime = findViewById<TextInputEditText>(R.id.etEndTime)
        val etLocation = findViewById<TextInputEditText>(R.id.etLocation)
        val etNotes = findViewById<TextInputEditText>(R.id.etNotes)
        val etAvgHeartRate = findViewById<TextInputEditText>(R.id.etAvgHeartRate)
        val etMaxHeartRate = findViewById<TextInputEditText>(R.id.etMaxHeartRate)
        val etSets = findViewById<TextInputEditText>(R.id.etSets)
        val etRepsPerSet = findViewById<TextInputEditText>(R.id.etRepsPerSet)
        val etWeightUsedKg = findViewById<TextInputEditText>(R.id.etWeightUsedKg)

        val actActivityType = findViewById<AutoCompleteTextView>(R.id.actActivityType)
        val actIntensity = findViewById<AutoCompleteTextView>(R.id.actIntensity)
        val actMoodBefore = findViewById<AutoCompleteTextView>(R.id.actMoodBefore)
        val actMoodAfter = findViewById<AutoCompleteTextView>(R.id.actMoodAfter)

        val btnSave = findViewById<Button>(R.id.btnSaveActivity)

        // Date picker
        etActivityDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->

                    val date = "%04d-%02d-%02d".format(year, month + 1, day)
                    etActivityDate.setText(date)

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        // Start Time
        etStartTime.setOnClickListener {

            val calendar = Calendar.getInstance()

            val timePicker = TimePickerDialog(
                this,
                { _, hour, minute ->

                    val time = "%02d:%02d:00".format(hour, minute)
                    etStartTime.setText(time)

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )

            timePicker.show()
        }

        // End Time
        etEndTime.setOnClickListener {

            val calendar = Calendar.getInstance()

            val timePicker = TimePickerDialog(
                this,
                { _, hour, minute ->

                    val time = "%02d:%02d:00".format(hour, minute)
                    etEndTime.setText(time)

                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )

            timePicker.show()
        }

        // Dropdown Activity Type
        val types = arrayOf(
            "Cardio",
            "Yoga",
            "HIIT",
            "Strength",
            "Flexibility",
            "Other"
        )

        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
        actActivityType.setAdapter(typeAdapter)

        // Dropdown Intensity
        val intensityList = arrayOf("low","moderate","high")
        val intensityAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, intensityList)
        actIntensity.setAdapter(intensityAdapter)

        // Dropdown Mood
        val moods = arrayOf("neutral","good","great")
        val moodAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, moods)
        actMoodBefore.setAdapter(moodAdapter)
        actMoodAfter.setAdapter(moodAdapter)

        // Save Activity
        btnSave.setOnClickListener {

            if (etActivityName.text.toString().trim().isEmpty()) {
                etActivityName.error = "Required field"
                etActivityName.requestFocus()
                return@setOnClickListener
            }

            if (actActivityType.text.toString().trim().isEmpty()) {
                actActivityType.error = "Select activity type"
                actActivityType.requestFocus()
                return@setOnClickListener
            }

            if (etDurationMinutes.text.toString().trim().isEmpty()) {
                etDurationMinutes.error = "Enter duration"
                etDurationMinutes.requestFocus()
                return@setOnClickListener
            }

            if (etActivityDate.text.toString().trim().isEmpty()) {
                etActivityDate.error = "Select date"
                etActivityDate.requestFocus()
                return@setOnClickListener
            }

            if (etStartTime.text.toString().trim().isEmpty()) {
                etStartTime.error = "Select start time"
                etStartTime.requestFocus()
                return@setOnClickListener
            }

            if (etEndTime.text.toString().trim().isEmpty()) {
                etEndTime.error = "Select end time"
                etEndTime.requestFocus()
                return@setOnClickListener
            }

            if (etCaloriesBurned.text.toString().trim().isEmpty()) {
                etCaloriesBurned.error = "Enter calories burned"
                etCaloriesBurned.requestFocus()
                return@setOnClickListener
            }

            if (etAvgHeartRate.text.toString().trim().isEmpty()) {
                etAvgHeartRate.error = "Enter average heart rate"
                etAvgHeartRate.requestFocus()
                return@setOnClickListener
            }

            if (etMaxHeartRate.text.toString().trim().isEmpty()) {
                etMaxHeartRate.error = "Enter max heart rate"
                etMaxHeartRate.requestFocus()
                return@setOnClickListener
            }

            if (actIntensity.text.toString().trim().isEmpty()) {
                actIntensity.error = "Select intensity"
                actIntensity.requestFocus()
                return@setOnClickListener
            }


            println("NAME = " + etActivityName.text.toString())
            println("TYPE = " + actActivityType.text.toString())
            println("DURATION = " + etDurationMinutes.text.toString())

            val url = "http://10.0.2.2/fitlife/save_activity.php"

            val request = object : StringRequest(
                Request.Method.POST,
                url,

                Response.Listener { response ->

                    val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                        .setTitle("Success")
                        .setMessage("Activity saved successfully!")
                        .setPositiveButton("OK") { dialog, _ ->

                            dialog.dismiss()

                            val intent = Intent(this, ActivitiesListActivity::class.java)
                            startActivity(intent)

                            finish()

                        }
                        .create()

                    dialog.show()

                }
                ,

                Response.ErrorListener { error ->

                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()

                }
            ) {

                override fun getParams(): MutableMap<String, String> {

                    val params = HashMap<String, String>()

                    params["user_id"] = "1"
                    params["name"] = etActivityName.text.toString()
                    params["type"] = actActivityType.text.toString()
                    params["duration"] = etDurationMinutes.text.toString()
                    params["distance"] = etDistanceKm.text.toString()
                    params["calories"] = etCaloriesBurned.text.toString()
                    params["date"] = etActivityDate.text.toString()
                    params["start"] = etStartTime.text.toString()
                    params["end"] = etEndTime.text.toString()
                    params["location"] = etLocation.text.toString()
                    params["notes"] = etNotes.text.toString()
                    params["intensity"] = actIntensity.text.toString()
                    params["avg_heart"] = etAvgHeartRate.text.toString()
                    params["max_heart"] = etMaxHeartRate.text.toString()
                    params["sets"] = etSets.text.toString()
                    params["reps"] = etRepsPerSet.text.toString()
                    params["weight"] = etWeightUsedKg.text.toString()
                    params["mood_before"] = actMoodBefore.text.toString()
                    params["mood_after"] = actMoodAfter.text.toString()

                    return params
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(request)

        }
    }
}