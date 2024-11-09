package com.example.mediminder.workers

import android.content.Context
import android.util.Log
import androidx.room.Transaction
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.utils.AppUtils.getHourlyReminderTimes
import com.example.mediminder.utils.AppUtils.isScheduledForDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class CreateFutureMedicationLogsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val medicationDao = database.medicationDao()
        val scheduleDao = database.scheduleDao()
        val reminderDao = database.remindersDao()
        val medicationLogDao = database.medicationLogDao()

        return try {
            val medications = medicationDao.getAllWithRemindersEnabled()
            val today = LocalDate.now()

            medications.forEach { medication ->
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

        // Create logs for medication if it doesn't have enough future logs (otherwise, skip)
        if (schedule != null && reminder != null) {
            val futureLogs = medicationLogDao.getFutureLogsCount(medicationId, today)

            if (futureLogs < MIN_FUTURE_DAYS) {
                createLogsForMedication(
                    medicationId = medicationId,
                    scheduleId = schedule.id,
                    schedule = schedule,
                    reminder = reminder,
                    startDate = today.plusDays(futureLogs.toLong()),
                    medicationLogDao = medicationLogDao
                )
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

        while (!currentDate.isAfter(endDate)) {
            if (isScheduledForDate(schedule, currentDate)) {
                val reminderTimes = getReminderTimes(reminder)
                insertLogsForDate(medicationId, scheduleId, currentDate, reminderTimes, medicationLogDao)
            }
            currentDate = currentDate.plusDays(1)
        }
    }

    private fun calculateEndDate(schedule: Schedules, startDate: LocalDate): LocalDate {
        return when (schedule.durationType) {
            "numDays" -> getEndDateForSetNumDays(schedule, startDate)
            else -> startDate.plusDays(DAYS_TO_CREATE.toLong())
        }
    }

    private fun getEndDateForSetNumDays(schedule: Schedules, startDate: LocalDate): LocalDate {
        val daysLeft = calculateDaysLeft(schedule, startDate)
        if (daysLeft <= 0) { return startDate.plusDays(DAYS_TO_CREATE.toLong()) }
        else { return startDate.plusDays(daysLeft.toLong()) }
    }

    private fun calculateDaysLeft(schedule: Schedules, startDate: LocalDate): Int {
        val daysLeft = schedule.numDays
            ?.minus(ChronoUnit.DAYS.between(schedule.startDate, startDate).toInt())
            ?: DAYS_TO_CREATE // Default to DAYS_TO_CREATE if numDays is null
        return daysLeft
    }

    private fun getReminderTimes(reminder: MedReminders): List<LocalTime> {
        return when (reminder.reminderFrequency) {
            "daily" -> reminder.dailyReminderTimes
            "every x hours" -> getHourlyReminderTimes(
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
            val existingLog = medicationLogDao.getLogByMedIdAndTime(medicationId, plannedDateTime)

            if (existingLog == null) {
                try {
                    medicationLogDao.insert(
                        MedicationLogs(
                            medicationId = medicationId,
                            scheduleId = scheduleId,
                            plannedDatetime = plannedDateTime,
                            takenDatetime = null,
                            status = MedicationStatus.PENDING
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