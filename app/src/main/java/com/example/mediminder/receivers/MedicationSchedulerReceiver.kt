package com.example.mediminder.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.utils.AppUtils.getHourlyReminderTimes
import com.example.mediminder.utils.AppUtils.isScheduledForDate
import com.example.mediminder.utils.Constants.ALARM_PERMISSION_DENIED
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.DOSAGE
import com.example.mediminder.utils.Constants.DOSAGE_DEFAULT_UNIT
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.ERR_SCHEDULING_NOTIFICATIONS
import com.example.mediminder.utils.Constants.EVERY_X_HOURS
import com.example.mediminder.utils.Constants.LOG_ID
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.Constants.MED_NAME
import com.example.mediminder.utils.Constants.SCHEDULE_DAILY_MEDICATIONS
import com.example.mediminder.utils.Constants.SCHEDULE_NEW_MEDICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Broadcast receiver for scheduling medication reminders
 */
class MedicationSchedulerReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SCHEDULE_DAILY_MEDICATIONS || intent.action == SCHEDULE_NEW_MEDICATION) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (!hasAlarmPermission(alarmManager)) { return@launch }
                    processMedications(context, alarmManager)
                } catch (e: Exception) {
                    Log.e(TAG, ERR_SCHEDULING_NOTIFICATIONS, e)
                }
            }
        }
    }

    // Check if alarm permission is granted
    private fun hasAlarmPermission(alarmManager: AlarmManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !alarmManager.canScheduleExactAlarms()) { return false }
        else { return true }
    }

    // Process medications to schedule reminders
    private suspend fun processMedications(context: Context, alarmManager: AlarmManager) {
        val today = LocalDate.now()
        val database = AppDatabase.getDatabase(context)
        val medications = database.medicationDao().getAllScheduledMedications()

        medications.forEach { medication ->
            val schedule = database.scheduleDao().getScheduleByMedicationId(medication.id)
            val isScheduledToday = isScheduledForDate(schedule, today)
            if (isScheduledToday) { createReminders(medication, database, today, context, alarmManager) }
        }
    }

    // Create reminders for a medication
    private suspend fun createReminders(
        medication: Medication,
        database: AppDatabase,
        today: LocalDate,
        context: Context,
        alarmManager: AlarmManager
    ) {
        val reminder = database.remindersDao().getReminderByMedicationId(medication.id)
        val dosage = database.dosageDao().getDosageByMedicationId(medication.id)
        val dosageString = getDosageString(dosage)
        val reminderTimes = getReminderTimes(reminder)

        reminderTimes.forEach { time ->
            val medTime = LocalDateTime.of(today, time)
            val log = database.medicationLogDao().getLogByMedIdAndTime(medication.id, medTime)

            if (log != null) {
                scheduleReminder(context, medication.id, medication.name, dosageString, log.id, time, alarmManager)
            }
        }
    }

    // Get dosage string
    private fun getDosageString(dosage: Dosage?): String {
        if (dosage == null) { return EMPTY_STRING }
        val amount = dosage.amount
        val units = if (dosage.units.isNullOrEmpty()) { DOSAGE_DEFAULT_UNIT } else { dosage.units }
        return "$amount $units"
    }

    // Get reminder times
    private fun getReminderTimes(reminder: MedReminders?): List<LocalTime> {
        if (reminder != null) {
            return when (reminder.reminderFrequency) {
                DAILY -> reminder.dailyReminderTimes
                EVERY_X_HOURS -> getHourlyReminderTimes(
                    reminder.hourlyReminderInterval,
                    reminder.hourlyReminderStartTime?.let { Pair(it.hour, it.minute) },
                    reminder.hourlyReminderEndTime?.let { Pair(it.hour, it.minute) }
                )
                else -> emptyList()
            }
        } else { return emptyList() }
    }

    // Schedule a reminder
    private fun scheduleReminder(
        context: Context,
        medicationId: Long,
        medicationName: String,
        dosage: String,
        logId: Long,
        time: LocalTime,
        alarmManager: AlarmManager,
    ) {
        // Check permission first
        if (!hasAlarmPermission(alarmManager)) { throw SecurityException(ALARM_PERMISSION_DENIED) }

        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(MED_ID, medicationId)
            putExtra(MED_NAME, medicationName)
            putExtra(DOSAGE, dosage)
            putExtra(LOG_ID, logId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            "${medicationId}_${time.hour}_${time.minute}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm (return early if trigger time is null to skip scheduling past reminders)
        val triggerTime = getTriggerTime(time) ?: return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    // Get trigger time for the alarm
    private fun getTriggerTime(time: LocalTime): Long? {
        val now = LocalDateTime.now()
        if (now.hour > time.hour || (now.hour == time.hour && now.minute >= time.minute)) { return null}

        else return now.withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    companion object {
        private const val TAG = "MedicationSchedulerReceiver"
    }
}