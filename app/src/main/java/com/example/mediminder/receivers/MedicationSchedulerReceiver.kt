package com.example.mediminder.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.utils.MedScheduledForDateUtil
import com.example.mediminder.utils.ReminderTimesUtil.getHourlyReminderTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class MedicationSchedulerReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == "com.example.mediminder.SCHEDULE_DAILY_MEDICATIONS" ||
            intent.action == "com.example.mediminder.SCHEDULE_NEW_MEDICATION") {

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        && !alarmManager.canScheduleExactAlarms()) {
                        return@launch
                    }

                    val today = LocalDate.now()
                    val medications = database.medicationDao().getAllWithRemindersEnabled()

                    medications.forEach { medication ->
                        val schedule = database.scheduleDao().getScheduleByMedicationId(medication.id)
                        if (MedScheduledForDateUtil.isScheduledForDate(schedule, today)) {
                            val dosage = database.dosageDao().getDosageByMedicationId(medication.id)
                            val reminder = database.remindersDao().getReminderByMedicationId(medication.id)

                            reminder?.let { rem ->
                                val reminderTimes = when (rem.reminderFrequency) {
                                    "daily" -> rem.dailyReminderTimes
                                    "hourly" -> getHourlyReminderTimes(
                                        rem.hourlyReminderInterval,
                                        rem.hourlyReminderStartTime?.let { Pair(it.hour, it.minute) },
                                        rem.hourlyReminderEndTime?.let { Pair(it.hour, it.minute) }
                                    )
                                    else -> emptyList()
                                }

                                reminderTimes.forEach { time ->
                                    scheduleReminder(
                                        context,
                                        alarmManager,
                                        medication.id,
                                        medication.name,
                                        dosage?.let { "${it.amount} ${it.units}" } ?: "",
                                        time
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("testcat AppDebug", "Error in medication scheduling process: ${e.message}", e)
                }
            }
        }
    }

    private fun scheduleReminder(
        context: Context,
        alarmManager: AlarmManager,
        medicationId: Long,
        medicationName: String,
        dosage: String,
        time: LocalTime
    ) {
        // Check permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                throw SecurityException("Permission to schedule exact alarms not granted")
            }
        }

        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("medicationId", medicationId)
            putExtra("medicationName", medicationName)
            putExtra("dosage", dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${medicationId}_${time.hour}_${time.minute}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = LocalDateTime.now()
        val triggerTime = if (now.hour > time.hour
            || (now.hour == time.hour && now.minute >= time.minute)) {
            // If the time has passed for today, just ignore it and return
            return
        } else {
            // Schedule for today
            now.withHour(time.hour)
                .withMinute(time.minute)
                .withSecond(0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}