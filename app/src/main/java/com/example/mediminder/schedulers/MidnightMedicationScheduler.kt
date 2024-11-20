package com.example.mediminder.schedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.mediminder.receivers.MedicationSchedulerReceiver
import com.example.mediminder.utils.Constants.MIDNIGHT_SCHEDULER
import com.example.mediminder.utils.Constants.SCHEDULE_DAILY_MEDICATIONS
import java.util.Calendar

// https://developer.android.com/training/scheduling/alarms
// This service class schedules medication notifications for the next day. It runs every day at midnight.
class MidnightMedicationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleMidnightAlarm() {
        // Check if the app has permission to schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !alarmManager.canScheduleExactAlarms()) { return }

        // Create a pending intent to be run at the scheduled time
        val intent = Intent(context, MedicationSchedulerReceiver::class.java).apply {
            action = SCHEDULE_DAILY_MEDICATIONS
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            MIDNIGHT_SCHEDULER.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the intent to run every day at midnight
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        // NOTE: For development purposes only
        // testMidnightAlarm(pendingIntent)
    }

    // NOTE: For testing and development purposes only
    private fun testMidnightAlarm(pendingIntent: PendingIntent) {
        val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 2) }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            1000 * 60 * 5, // Repeat every 5 minutes instead of daily
            pendingIntent
        )
    }
}
