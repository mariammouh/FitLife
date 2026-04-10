package com.example.fitlife

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddActivityFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_form)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarForm)
        toolbar.setNavigationOnClickListener { finish() }

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
        val btnShowWorkout = findViewById<Button>(R.id.btnShowWorkout)
        val actIntensity = findViewById<AutoCompleteTextView>(R.id.actIntensity)
        val actMoodBefore = findViewById<AutoCompleteTextView>(R.id.actMoodBefore)
        val actMoodAfter = findViewById<AutoCompleteTextView>(R.id.actMoodAfter)

        val btnSave = findViewById<Button>(R.id.btnSaveActivity)

        val userId = intent.getIntExtra("USER_ID", 1)

        etActivityDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etActivityDate.setText("%04d-%02d-%02d".format(year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
                datePicker.maxDate = System.currentTimeMillis()
                show()
            }
        }

        etStartTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                etStartTime.setText("%02d:%02d:00".format(hour, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        etEndTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                etEndTime.setText("%02d:%02d:00".format(hour, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        val types = arrayOf("Cardio", "Strength", "Flexibility", "Balance", "Sports", "HIIT", "Yoga", "Other")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
        actActivityType.setAdapter(typeAdapter)

        actActivityType.setOnItemClickListener { _, _, _, _ ->
            btnShowWorkout.visibility = View.VISIBLE
        }

        btnShowWorkout.setOnClickListener {
            val selectedType = actActivityType.text.toString()
            if (selectedType.isNotEmpty()) {
                val intent = android.content.Intent(this, WorkoutActivity::class.java)
                intent.putExtra("WORKOUT_NAME", selectedType)
                startActivity(intent)
            }
        }

        val intensityList = arrayOf("low", "moderate", "high")
        actIntensity.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, intensityList))

        val moods = arrayOf("neutral", "good", "great")
        val moodAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, moods)
        actMoodBefore.setAdapter(moodAdapter)
        actMoodAfter.setAdapter(moodAdapter)

        btnSave.setOnClickListener {
            if (etActivityName.text.isNullOrEmpty()) { etActivityName.error = "Required"; return@setOnClickListener }
            if (actActivityType.text.isNullOrEmpty()) { actActivityType.error = "Required"; return@setOnClickListener }

            RetrofitClient.instance.saveActivity(
                userId,
                etActivityName.text.toString(),
                actActivityType.text.toString(),
                etDurationMinutes.text.toString(),
                etDistanceKm.text.toString(),
                etCaloriesBurned.text.toString(),
                etActivityDate.text.toString(),
                etStartTime.text.toString(),
                etEndTime.text.toString(),
                etLocation.text.toString(),
                etNotes.text.toString(),
                actIntensity.text.toString(),
                etAvgHeartRate.text.toString(),
                etMaxHeartRate.text.toString(),
                etSets.text.toString(),
                etRepsPerSet.text.toString(),
                etWeightUsedKg.text.toString(),
                actMoodBefore.text.toString(),
                actMoodAfter.text.toString()
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddActivityFormActivity, "Activity Saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddActivityFormActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@AddActivityFormActivity, "Connection Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
