package com.example.mediminder.workers

import android.content.Context
import android.util.Log
import androidx.room.Transaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.utils.MedScheduledForDateUtil
import com.example.mediminder.utils.ReminderTimesUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class CreateFutureMedicationLogsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("testcat", "CreateFutureMedicationLogsWorker started")

        val database = AppDatabase.getDatabase(applicationContext)
        val medicationDao = database.medicationDao()
        val scheduleDao = database.scheduleDao()
        val reminderDao = database.remindersDao()
        val medicationLogDao = database.medicationLogDao()

        return try {
            val medications = medicationDao.getAllWithRemindersEnabled()
            Log.d("testcat", "Found ${medications.size} medications with reminders")

            val today = LocalDate.now()

            medications.forEach { medication ->
                Log.d("testcat", "Processing medication: ${medication.name}")

                processMedication(medication.id, today, scheduleDao, reminderDao, medicationLogDao)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("testcat", "Error in CreateFutureMedicationLogsWorker: ${e.message}")

            Result.retry()
        }
    }

    private suspend fun processMedication(
        medicationId: Long,
        today: LocalDate,
        scheduleDao: ScheduleDao,
        reminderDao: MedRemindersDao,
        medicationLogDao: MedicationLogDao
    ){
        val schedule = scheduleDao.getScheduleByMedicationId(medicationId)
        val reminder = reminderDao.getReminderByMedicationId(medicationId)

        Log.d("testcat", "Processing medication ID: $medicationId")
        Log.d("testcat", "Schedule: $schedule")
        Log.d("testcat", "Reminder: $reminder")

        if (schedule != null && reminder != null) {
            val futureLogs = medicationLogDao.getFutureLogsCount(medicationId, today)
            Log.d("testcat", "Future logs count: $futureLogs")


            if (futureLogs < MIN_FUTURE_DAYS) {
                Log.d("testcat", "Creating logs from ${today.plusDays(futureLogs.toLong())}")

                createLogsForMedication(
                    medicationId = medicationId,
                    scheduleId = schedule.id,
                    schedule = schedule,
                    reminder = reminder,
                    startDate = today.plusDays(futureLogs.toLong()),
                    medicationLogDao = medicationLogDao
                )
            }
            else {
                Log.d("LogsWorker testcat", "Skipping medication $medicationId - has enough future logs")
            }
        }
    }

    private suspend fun createLogsForMedication(
        medicationId: Long,
        scheduleId: Long,
        schedule: Schedules,
        reminder: MedReminders,
        startDate: LocalDate,
        medicationLogDao: MedicationLogDao
    ) {
        val endDate = calculateEndDate(schedule, startDate)
        var currentDate = startDate

        Log.d("testcat", "Creating logs from $startDate to $endDate")


        while (!currentDate.isAfter(endDate)) {
            if (MedScheduledForDateUtil.isScheduledForDate(schedule, currentDate)) {
                val reminderTimes = getReminderTimes(reminder)
                Log.d("testcat", "Reminder times for $currentDate: $reminderTimes")

                insertLogsForDate(medicationId, scheduleId, currentDate, reminderTimes, medicationLogDao)
            }
            else {
                Log.d("testcat", "Medication not scheduled for $currentDate")
            }
            currentDate = currentDate.plusDays(1)
        }
    }

    private fun calculateEndDate(schedule: Schedules, startDate: LocalDate): LocalDate {
        return when (schedule.durationType) {
            "numDays" -> {
                val daysLeft = schedule.numDays?.minus(
                    ChronoUnit.DAYS.between(schedule.startDate, startDate).toInt()
                ) ?: DAYS_TO_CREATE // Default to DAYS_TO_CREATE if numDays is null
                if (daysLeft <= 0) return startDate.plusDays(DAYS_TO_CREATE.toLong())
                startDate.plusDays(daysLeft.toLong())
            }
            else -> startDate.plusDays(DAYS_TO_CREATE.toLong())
        }
    }

    private fun getReminderTimes(reminder: MedReminders): List<LocalTime> {
        return when (reminder.reminderFrequency) {
            "daily" -> reminder.dailyReminderTimes
            "every x hours" -> ReminderTimesUtil.getHourlyReminderTimes(
                reminder.hourlyReminderInterval,
                reminder.hourlyReminderStartTime?.let { Pair(it.hour, it.minute) },
                reminder.hourlyReminderEndTime?.let { Pair(it.hour, it.minute) }
            )
            else -> emptyList()
        }
    }

    @Transaction
    private suspend fun insertLogsForDate(
        medicationId: Long,
        scheduleId: Long,
        date: LocalDate,
        reminderTimes: List<LocalTime>,
        medicationLogDao: MedicationLogDao
    ) {
        reminderTimes.forEach { time ->
            val plannedDateTime = LocalDateTime.of(date, time)
            // Check if log already exists for this medication at this time
            val existingLog = medicationLogDao.getLogByMedicationIdAndPlannedTime(medicationId, plannedDateTime)

            if (existingLog == null) {
                try {
                    val log = MedicationLogs(
                        medicationId = medicationId,
                        scheduleId = scheduleId,
                        plannedDatetime = plannedDateTime,
                        takenDatetime = null,
                        status = MedicationStatus.PENDING
                    )
                    val id = medicationLogDao.insert(log)
                } catch (e: Exception) {
                    Log.e("testcat", "Failed to insert log: ${e.message}")
                    throw e
                }
            }
//            else {
//                Log.d("testcat", "Skipped inserting duplicate log for time: $plannedDateTime")
//            }
        }
    }

    companion object {
        private const val TAG = "CreateFutureMedicationLogsWorker"
        private const val MIN_FUTURE_DAYS = 7
        private const val DAYS_TO_CREATE = 7
    }
}