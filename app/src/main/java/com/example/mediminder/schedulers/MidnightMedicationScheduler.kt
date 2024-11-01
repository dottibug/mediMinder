package com.example.mediminder.schedulers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mediminder.receivers.MedicationSchedulerReceiver
import java.util.Calendar

class MidnightMedicationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // At midnight, create alarms for the following day
    fun scheduleMidnightAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val intent = Intent(context, MedicationSchedulerReceiver::class.java).apply {
            action = "com.example.mediminder.SCHEDULE_DAILY_MEDICATIONS"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "midnight_scheduler".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set for next midnight
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
//        testMidnightAlarm(pendingIntent)
    }

    // NOTE: For testing and development purposes only
    private fun testMidnightAlarm(pendingIntent: PendingIntent) {
        val calendar = Calendar.getInstance().apply { add(Calendar.MINUTE, 2) }
        Log.d("testcat", "Scheduling midnight alarm for: ${calendar.time}")

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            1000 * 60 * 5, // Repeat every 5 minutes instead of daily
            pendingIntent
        )
    }
}
