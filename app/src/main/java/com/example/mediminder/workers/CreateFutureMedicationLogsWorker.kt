package com.example.mediminder.workers

import android.content.Context
import android.util.Log
import androidx.room.Transaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.utils.AppUtils.getHourlyReminderTimes
import com.example.mediminder.utils.AppUtils.isScheduledForDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

// This background worker creates medication logs for the next 7 days, allowing users to see their
// upcoming scheduled medications. The worker runs once per day, and also when a medication is
// added or updated. Duplicate logs are not created.
// The worker runs in the background even if the app is destroyed, which is important for an app that
// that is scheduling notifications.
class CreateFutureMedicationLogsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)

        return try {
            // Check if updating a single medication
            val medicationId = inputData.getLong("medicationId", -1)
            if (medicationId != -1L) { handleSingleMedication(medicationId, database) }
            else { handleAllMedications(database) }
            Result.success()
        } catch (e: Exception) {
            Log.e("testcat", "Error in CreateFutureMedicationLogsWorker: ${e.message}")
            Result.retry()
        }
    }

    // Create logs for a single medication (used when updating a medication)
    private suspend fun handleSingleMedication(medicationId: Long, database: AppDatabase) {
        val medication = database.medicationDao().getMedicationById(medicationId)

        if (!medication.asNeeded) {
            processMedication(
                medicationId = medicationId,
                today = LocalDate.now(),
                scheduleDao = database.scheduleDao(),
                reminderDao = database.remindersDao(),
                medicationLogDao = database.medicationLogDao()
            )
        }

    }

    // Create logs for all scheduled medications (used daily)
    private suspend fun handleAllMedications(database: AppDatabase) {
        val medications = database.medicationDao().getAllScheduledMedications()
        val today = LocalDate.now()

        medications.forEach { medication ->
            processMedication(
                medicationId = medication.id,
                today = today,
                scheduleDao = database.scheduleDao(),
                reminderDao = database.remindersDao(),
                medicationLogDao = database.medicationLogDao()
            )
        }
    }

    // Create logs for the given medication if it has less than 7 days of future logs
    private suspend fun processMedication(
        medicationId: Long,
        today: LocalDate,
        scheduleDao: ScheduleDao,
        reminderDao: MedRemindersDao,
        medicationLogDao: MedicationLogDao
    ){
        val schedule = scheduleDao.getScheduleByMedicationId(medicationId)
        val reminder = reminderDao.getReminderByMedicationId(medicationId)

        if (schedule != null && reminder != null) {
            val futureLogs = medicationLogDao.getFutureLogsCount(medicationId, today)

            if (futureLogs < MIN_FUTURE_DAYS) {
                val startDate = today.plusDays(futureLogs.toLong())
                val endDate = calculateEndDate(schedule, startDate)
                var currentDate = startDate

                while (!currentDate.isAfter(endDate)) {
                    if (isScheduledForDate(schedule, currentDate)) {
                        val reminderTimes = getReminderTimes(reminder)
                        insertLogsForDate(medicationId, schedule.id, currentDate, reminderTimes, medicationLogDao)
                    }
                    currentDate = currentDate.plusDays(1)
                }
            }
        }
    }

    // Calculate the end date based on the schedule's duration type
    private fun calculateEndDate(schedule: Schedules, startDate: LocalDate): LocalDate {
        return when (schedule.durationType) {
            "numDays" -> getEndDateForSetNumDays(schedule, startDate)
            else -> startDate.plusDays(DAYS_TO_CREATE.toLong())
        }
    }

    // Calculate the end date based on the number of days in the schedule
    private fun getEndDateForSetNumDays(schedule: Schedules, startDate: LocalDate): LocalDate {
        val daysLeft = calculateDaysLeft(schedule, startDate)
        if (daysLeft <= 0) { return startDate.plusDays(DAYS_TO_CREATE.toLong()) }
        else { return startDate.plusDays(daysLeft.toLong()) }
    }


    // Calculate the number of days left in the schedule
    private fun calculateDaysLeft(schedule: Schedules, startDate: LocalDate): Int {
        val daysLeft = schedule.numDays
            ?.minus(ChronoUnit.DAYS.between(schedule.startDate, startDate).toInt())
            ?: DAYS_TO_CREATE // Default to DAYS_TO_CREATE if numDays is null
        return daysLeft
    }

    // Get the reminder times based on the reminder frequency
    private fun getReminderTimes(reminder: MedReminders): List<LocalTime> {
        return when (reminder.reminderFrequency) {
            "daily", "" -> reminder.dailyReminderTimes
            "every x hours" -> getHourlyReminderTimes(
                reminder.hourlyReminderInterval,
                reminder.hourlyReminderStartTime?.let { Pair(it.hour, it.minute) },
                reminder.hourlyReminderEndTime?.let { Pair(it.hour, it.minute) }
            )
            else -> emptyList()
        }
    }

    // Insert logs for the given date and reminder times
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
            val existingLog = medicationLogDao.getLogByMedIdAndTime(medicationId, plannedDateTime)

            if (existingLog == null) {
                try {
                    medicationLogDao.insert(
                        MedicationLogs(
                            medicationId = medicationId,
                            scheduleId = scheduleId,
                            plannedDatetime = plannedDateTime,
                            takenDatetime = null,
                            status = MedicationStatus.PENDING,
                            asNeededDosageAmount = null,
                            asNeededDosageUnit = null
                        )
                    )
                } catch (e: Exception) {
                    Log.e("testcat", "Failed to insert log: ${e.message}")
                    throw e
                }
            }
        }
    }

    companion object {
        private const val MIN_FUTURE_DAYS = 7
        private const val DAYS_TO_CREATE = 7
    }
}