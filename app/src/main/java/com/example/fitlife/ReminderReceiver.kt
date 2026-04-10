package com.example.fitlife

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs   = context.getSharedPreferences("fitlife_prefs", Context.MODE_PRIVATE)
        val message = prefs.getString("reminder_msg", "Time for your workout! 💪") ?: ""
        val enabled = prefs.getBoolean("reminder_enabled", false)

        // Only fire if reminder is still enabled
        if (!enabled) return

        // ── Show notification ──
        showNotification(context, message, prefs)

        // ── Reschedule for same time TOMORROW ──
        // This is how daily repeating works reliably with setExactAndAllowWhileIdle
        val time   = prefs.getString("reminder_time", "08:00") ?: "08:00"
        val parts  = time.split(":")
        val hour   = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            add(java.util.Calendar.DAY_OF_YEAR, 1) // always tomorrow
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, 1001,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        }
    }

    private fun showNotification(context: Context, message: String, prefs: android.content.SharedPreferences) {
        val channelId = "fitlife_reminder"
        val manager   = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel (required Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FitLife Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description         = "Daily workout reminders"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }

        // Tap → open Dashboard
        val tapIntent = Intent(context, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("USER_ID", prefs.getInt("user_id", 1))
        }
        val pendingTap = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_workouts)
            .setContentTitle("FitLife 🏋️")
            .setContentText(message)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingTap)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}