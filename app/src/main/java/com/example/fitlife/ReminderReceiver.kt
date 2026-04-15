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
        val prefs = context.getSharedPreferences("fitlife_prefs", Context.MODE_PRIVATE)
        val type = intent.getStringExtra("TYPE") ?: "DAILY"

        if (type == "HYDRATION") {
            handleHydration(context, prefs)
        } else {
            handleDaily(context, prefs)
        }
    }

    private fun handleDaily(context: Context, prefs: android.content.SharedPreferences) {
        val enabled = prefs.getBoolean("reminder_enabled", false)
        if (!enabled) return

        val message = prefs.getString("reminder_msg", "Time for your workout! 💪") ?: ""
        showNotification(context, message, "fitlife_daily", "FitLife 🏋️", 1001, prefs)

        // Reschedule for tomorrow
        val time = prefs.getString("reminder_time", "08:00") ?: "08:00"
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        val pi = PendingIntent.getBroadcast(
            context, 1001,
            Intent(context, ReminderReceiver::class.java).apply { putExtra("TYPE", "DAILY") },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        schedule(context, calendar.timeInMillis, pi)
    }

    private fun handleHydration(context: Context, prefs: android.content.SharedPreferences) {
        val enabled = prefs.getBoolean("hydration_enabled", false)
        if (!enabled) return

        showNotification(context, "Time to drink some water! 💧", "fitlife_hydration", "Hydration 🥤", 1002, prefs)

        // Reschedule
        val interval = prefs.getInt("hydration_interval", 60)
        val triggerAt = System.currentTimeMillis() + (interval * 60 * 1000)

        val pi = PendingIntent.getBroadcast(
            context, 1002,
            Intent(context, ReminderReceiver::class.java).apply { putExtra("TYPE", "HYDRATION") },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        schedule(context, triggerAt, pi)
    }

    private fun schedule(context: Context, time: Long, pi: PendingIntent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, time, pi)
        }
    }

    private fun showNotification(context: Context, message: String, channelId: String, title: String, id: Int, prefs: android.content.SharedPreferences) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("USER_ID", prefs.getInt("user_id", 1))
        }
        val pi = PendingIntent.getActivity(context, id, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val icon = if (channelId.contains("water")) R.drawable.ic_water else R.drawable.ic_workouts
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        manager.notify(id, notification)
    }
}
