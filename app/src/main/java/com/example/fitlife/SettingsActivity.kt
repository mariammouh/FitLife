package com.example.fitlife

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class SettingsActivity : AppCompatActivity() {

    private val PREFS = "fitlife_prefs"
    private var userId = 1

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        userId           = intent.getIntExtra("USER_ID", 1)
        val username     = intent.getStringExtra("USERNAME") ?: "User"
        val nbrTries     = intent.getIntExtra("NBR_TRIES", 0)
        val isPaying     = intent.getBooleanExtra("IS_PAYING", false)
        val goalWeight   = intent.getFloatExtra("GOAL_WEIGHT", 0f)
        val fitnessGoal  = intent.getStringExtra("FITNESS_GOAL") ?: "lose_weight"

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putInt("user_id", userId).apply()

        // ── Views ──
        val btnBack           = findViewById<ImageButton>(R.id.btnBack)
        val rowChangePassword = findViewById<RelativeLayout>(R.id.rowChangePassword)
        val rowUsername       = findViewById<RelativeLayout>(R.id.rowUsername)
        val rowWeightGoal     = findViewById<RelativeLayout>(R.id.rowWeightGoal)
        val rowFitnessGoal    = findViewById<RelativeLayout>(R.id.rowFitnessGoal)
        val rowLanguage       = findViewById<RelativeLayout>(R.id.rowLanguage)
        val tvUsername        = findViewById<TextView>(R.id.tvUsername)
        val tvWeightGoal      = findViewById<TextView>(R.id.tvWeightGoal)
        val tvFitnessGoal     = findViewById<TextView>(R.id.tvFitnessGoal)
        val tvNbrTries        = findViewById<TextView>(R.id.tvNbrTries)
        val switchReminder    = findViewById<SwitchCompat>(R.id.switchReminder)
        val tvReminderTime    = findViewById<TextView>(R.id.tvReminderTime)
        val rowTime           = findViewById<RelativeLayout>(R.id.rowReminderTime)
        val rowMessage        = findViewById<RelativeLayout>(R.id.rowReminderMessage)
        val rowTestNotif      = findViewById<RelativeLayout>(R.id.rowTestNotif)
        val cardUpgrade       = findViewById<CardView>(R.id.cardUpgrade)
        val rowLogout         = findViewById<RelativeLayout>(R.id.rowLogout)

        // ── Populate ──
        tvUsername?.text   = username
        tvWeightGoal?.text = if (goalWeight > 0) "${goalWeight.toInt()} kg" else "— kg"
        tvFitnessGoal?.text = goalLabel(fitnessGoal)
        tvNbrTries?.text   = "$nbrTries / 10 "

        if (isPaying) cardUpgrade?.visibility = View.GONE

        val savedTime    = prefs.getString("reminder_time", "08:00") ?: "08:00"
        val savedEnabled = prefs.getBoolean("reminder_enabled", false)
        tvReminderTime?.text      = savedTime
        switchReminder?.isChecked = savedEnabled

        btnBack?.setOnClickListener { finish() }

        // Change Password
        rowChangePassword?.setOnClickListener {
            showChangePasswordDialog()
        }

        // Edit Username
        rowUsername?.setOnClickListener {
            val input = EditText(this).apply {
                hint = "New username"
                setText(tvUsername?.text)
                setPadding(48, 24, 48, 24)
            }
            AlertDialog.Builder(this)
                .setTitle("Edit Username")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newVal = input.text.toString().trim()
                    if (newVal.isNotEmpty()) {
                        updateUserField("username", newVal) {
                            tvUsername?.text = newVal
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Edit Target Weight
        rowWeightGoal?.setOnClickListener {
            val input = EditText(this).apply {
                hint = "Target weight (kg)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(tvWeightGoal?.text?.toString()?.replace(" kg", ""))
                setPadding(48, 24, 48, 24)
            }
            AlertDialog.Builder(this)
                .setTitle("Target Weight")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newVal = input.text.toString().trim()
                    if (newVal.isNotEmpty()) {
                        updateUserField("goal_weight_kg", newVal) {
                            tvWeightGoal?.text = "$newVal kg"
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Edit Fitness Goal
        rowFitnessGoal?.setOnClickListener {
            val options = arrayOf("Lose Weight", "Build Muscle", "Maintain", "Improve Endurance")
            val keys    = arrayOf("lose_weight", "build_muscle", "maintain", "improve_endurance")
            val current = keys.indexOf(fitnessGoal).takeIf { it >= 0 } ?: 0
            AlertDialog.Builder(this)
                .setTitle("Fitness Goal")
                .setSingleChoiceItems(options, current) { dialog, which ->
                    updateUserField("goal", keys[which]) {
                        tvFitnessGoal?.text = options[which]
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Language
        rowLanguage?.setOnClickListener {
            val langs = arrayOf("English", "Français", "العربية", "Español")
            AlertDialog.Builder(this)
                .setTitle("Language")
                .setItems(langs) { _, which ->
                    Toast.makeText(this, "${langs[which]} — coming soon!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        cardUpgrade?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Upgrade to Premium 🚀")
                .setMessage("Unlock FitLife Premium for €9.99/month:\n\n• Unlimited sessions\n• Advanced statistics\n• Priority access to new features")
                .setPositiveButton("Continue") { _, _ ->
                    Toast.makeText(this, "Payment coming soon!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Later", null)
                .show()
        }

        // ════════════════════════════════════════
        // REMINDER — Toggle
        // ════════════════════════════════════════

        switchReminder?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (checkNotificationPermission()) {
                    prefs.edit().putBoolean("reminder_enabled", true).apply()
                    requestExactAlarmPermissionIfNeeded()
                    scheduleReminder(tvReminderTime?.text?.toString() ?: "08:00")
                    Toast.makeText(this, "Reminder set for ${tvReminderTime?.text} ✅", Toast.LENGTH_SHORT).show()
                } else {
                    switchReminder.isChecked = false
                    requestNotificationPermission()
                }
            } else {
                prefs.edit().putBoolean("reminder_enabled", false).apply()
                cancelReminder()
                Toast.makeText(this, "Reminder disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Time picker
        rowTime?.setOnClickListener {
            val parts  = (tvReminderTime?.text?.toString() ?: "08:00").split(":")
            val hour   = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            TimePickerDialog(this, { _, h, m ->
                val formatted = String.format("%02d:%02d", h, m)
                tvReminderTime?.text = formatted
                prefs.edit().putString("reminder_time", formatted).apply()
                if (switchReminder?.isChecked == true) {
                    scheduleReminder(formatted)
                    Toast.makeText(this, "Reminder updated: $formatted ⏰", Toast.LENGTH_SHORT).show()
                }
            }, hour, minute, true).show()
        }

        // Edit reminder message
        rowMessage?.setOnClickListener {
            val current = prefs.getString("reminder_msg", "Time for your workout! 💪") ?: ""
            val input = EditText(this).apply {
                setText(current)
                setPadding(48, 24, 48, 24)
            }
            AlertDialog.Builder(this)
                .setTitle("Reminder Message")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val msg = input.text.toString().trim().ifEmpty { "Time for your workout! 💪" }
                    prefs.edit().putString("reminder_msg", msg).apply()
                    Toast.makeText(this, "Message saved ✅", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Test notification — fires immediately
        rowTestNotif?.setOnClickListener {
            if (checkNotificationPermission()) {
                sendTestNotification(prefs)
            } else {
                requestNotificationPermission()
            }
        }

        // ════════════════════════════════════════
        // LOGOUT
        // ════════════════════════════════════════

        rowLogout?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    prefs.edit().clear().apply()
                    val i = Intent(this, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showChangePasswordDialog() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        val etOld = EditText(this).apply {
            hint = "Current Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val etNew = EditText(this).apply {
            hint = "New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(etOld)
        layout.addView(etNew)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val newPass = etNew.text.toString()
                if (newPass.length < 6) {
                    Toast.makeText(this, "New password too short", Toast.LENGTH_SHORT).show()
                } else {
                    RetrofitClient.instance.changePassword(userId, newPass).enqueue(object : Callback<UpdateUserResponse> {
                        override fun onResponse(call: Call<UpdateUserResponse>, response: Response<UpdateUserResponse>) {
                            Toast.makeText(this@SettingsActivity, "Password updated ✅", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                            Toast.makeText(this@SettingsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserField(field: String, value: String, onSuccess: () -> Unit) {
        val body = mapOf("user_id" to userId.toString(), field to value)
        RetrofitClient.instance.updateUser(body).enqueue(object : Callback<UpdateUserResponse> {
            override fun onResponse(call: Call<UpdateUserResponse>, response: Response<UpdateUserResponse>) {
                if (response.body()?.success == true) {
                    onSuccess()
                    Toast.makeText(this@SettingsActivity, "Saved ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "Save failed — check connection", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                Toast.makeText(this@SettingsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun scheduleReminder(time: String) {
        val parts  = time.split(":")
        val hour   = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildReminderPendingIntent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        }
    }

    private fun cancelReminder() {
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildReminderPendingIntent())
    }

    private fun buildReminderPendingIntent(): PendingIntent {
        val intent = Intent(this, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            this, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun sendTestNotification(prefs: android.content.SharedPreferences) {
        val message   = prefs.getString("reminder_msg", "Time for your workout! 💪") ?: ""
        val channelId = "fitlife_reminder"
        val manager   = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "FitLife Reminder", NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("USER_ID", prefs.getInt("user_id", 1))
        }
        val pendingTap = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_workouts)
            .setContentTitle("FitLife 🏋️")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingTap)
            .setAutoCancel(true)
            .build()

        manager.notify(1002, notification)
        Toast.makeText(this, "Test notification sent! 🔔", Toast.LENGTH_SHORT).show()
    }

    private fun goalLabel(key: String) = when (key) {
        "lose_weight"        -> "Lose Weight"
        "build_muscle"       -> "Build Muscle"
        "maintain"           -> "Maintain"
        "improve_endurance"  -> "Improve Endurance"
        else                 -> key
    }
}
