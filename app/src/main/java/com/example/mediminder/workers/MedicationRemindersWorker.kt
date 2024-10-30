package com.example.mediminder.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.receivers.MedicationReminderReceiver
import com.example.mediminder.utils.MedScheduledForDateUtil
import com.example.mediminder.utils.ReminderTimesUtil.getHourlyReminderTimes
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class MedicationRemindersWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("AlarmDebug", "Starting MedicationRemindersWorker - Thread: ${Thread.currentThread().name}")

        try {
            Log.d("AlarmDebug", "Initializing database")
            val database = AppDatabase.getDatabase(applicationContext)

            Log.d("AlarmDebug", "Getting AlarmManager service")
            val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Check if we have permission to schedule alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.e("AlarmDebug", "No permission to schedule exact alarms")
                    return Result.failure(
                        workDataOf("error" to "No permission to schedule exact alarms")
                    )
                }
            }

            val today = LocalDate.now()
            val medications = database.medicationDao().getAllWithRemindersEnabled()
            Log.d("AlarmDebug", "Found ${medications.size} medications with reminders enabled")

            // Medications for today
            medications.forEach { medication ->
                val schedule = database.scheduleDao().getScheduleByMedicationId(medication.id)
                if (MedScheduledForDateUtil.isScheduledForDate(schedule, today)) {

                    Log.d("AlarmDebug", "Processing medication: ${medication.name}")
                    val dosage = database.dosageDao().getDosageByMedicationId(medication.id)
                    val reminder = database.remindersDao().getReminderByMedicationId(medication.id)
                    Log.d("AlarmDebug", "Reminder for ${medication.name}: $reminder")

                    // Schedule a reminder for each reminder time
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
                        Log.d("AlarmDebug", "Reminder times for ${medication.name}: $reminderTimes")

                        reminderTimes.forEach { time ->
                            scheduleReminder(
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
            Log.d("AlarmDebug", "Worker completed successfully")
            return Result.success()
        } catch (e: SecurityException) {
            // If app does not have permission to schedule exact alarms, return failure
            Log.e("AlarmDebug", "Security exception: ${e.message}")
            return Result.failure(workDataOf("error" to e.message))
        } catch (e: Exception) {
            Log.e("AlarmDebug", "General exception: ${e.message}")
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun scheduleReminder(
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

        val intent = Intent(applicationContext, MedicationReminderReceiver::class.java).apply {
            putExtra("medicationId", medicationId)
            putExtra("medicationName", medicationName)
            putExtra("dosage", dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            "${medicationId}_${time.hour}_${time.minute}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = LocalDateTime.now()
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        Log.d("AlarmDebug", "Scheduling alarm for $medicationName at $time")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        Log.d("AlarmDebug", "Alarm scheduled successfully")
    }
}