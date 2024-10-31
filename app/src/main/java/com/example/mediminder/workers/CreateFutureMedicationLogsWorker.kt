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
        val database = AppDatabase.getDatabase(applicationContext)
        val medicationDao = database.medicationDao()
        val scheduleDao = database.scheduleDao()
        val reminderDao = database.remindersDao()
        val medicationLogDao = database.medicationLogDao()

        return try {
            val medications = medicationDao.getAllWithRemindersEnabled()
            val today = LocalDate.now()

            Log.d("LogsWorker testcat", "Found ${medications.size} medications with reminders enabled")


            medications.forEach { medication ->
                Log.d("LogsWorker testcat", "Processing medication: ${medication.name}")
                processMedication(medication.id, today, scheduleDao, reminderDao, medicationLogDao)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("LogsWorker testcat", "Error creating future logs: ${e.message}", e)
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

        if (schedule != null && reminder != null) {
            val futureLogs = medicationLogDao.getFutureLogsCount(medicationId, today)
            Log.d("LogsWorker testcat", "Medication $medicationId has $futureLogs future logs")

            if (futureLogs < MIN_FUTURE_DAYS) {
                Log.d("LogsWorker testcat", "Creating future logs for medication $medicationId")

                createLogsForMedication(
                    medicationId = medicationId,
                    scheduleId = schedule.id,
                    schedule = schedule,
                    reminder = reminder,
                    startDate = today.plusDays(futureLogs.toLong()),
                    medicationLogDao = medicationLogDao
                )
            } else {
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

        Log.d("LogsWorker testcat", "Creating logs for med $medicationId from $startDate to $endDate")


        while (!currentDate.isAfter(endDate)) {
            if (MedScheduledForDateUtil.isScheduledForDate(schedule, currentDate)) {
                val reminderTimes = getReminderTimes(reminder)
                Log.d("LogsWorker testcat", "Date $currentDate has ${reminderTimes.size} reminder times: $reminderTimes")
                insertLogsForDate(medicationId, scheduleId, currentDate, reminderTimes, medicationLogDao)
            }
            currentDate = currentDate.plusDays(1)
        }
    }

    private fun calculateEndDate(schedule: Schedules, startDate: LocalDate): LocalDate {
        return when (schedule.durationType) {
            "numDays" -> {
                val daysLeft = schedule.numDays?.minus(
                    ChronoUnit.DAYS.between(schedule.startDate, startDate).toInt()
                ) ?: 0
                if (daysLeft <= 0) return startDate
                startDate.plusDays(minOf(daysLeft.toLong(), DAYS_TO_CREATE.toLong()))
            }
            else -> startDate.plusDays(DAYS_TO_CREATE.toLong())
        }
    }

    private fun getReminderTimes(reminder: MedReminders): List<LocalTime> {
        return when (reminder.reminderFrequency) {
            "daily" -> reminder.dailyReminderTimes
            "hourly" -> ReminderTimesUtil.getHourlyReminderTimes(
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
        val logsToInsert = reminderTimes.map { time ->
            MedicationLogs(
                medicationId = medicationId,
                scheduleId = scheduleId,
                plannedDatetime = LocalDateTime.of(date, time),
                takenDatetime = null,
                status = MedicationStatus.PENDING
            )
        }

        val beforeCount = medicationLogDao.getTotalLogsCount()
        Log.d("testcat", "Before inserting logs for date $date: $beforeCount total logs")

        logsToInsert.forEach { log ->
            try {
                val id = medicationLogDao.insert(log)
                Log.d("testcat", "Inserted log with ID: $id for time: ${log.plannedDatetime}")
            } catch (e: Exception) {
                Log.e("testcat", "Failed to insert log: ${e.message}")
                throw e
            }
        }

        val afterCount = medicationLogDao.getTotalLogsCount()
        Log.d("testcat", "After inserting logs for date $date: $afterCount total logs")
    }

//    private suspend fun insertLogsForDate(
//        medicationId: Long,
//        scheduleId: Long,
//        date: LocalDate,
//        reminderTimes: List<LocalTime>,
//        medicationLogDao: MedicationLogDao
//    ) {
//        reminderTimes.forEach { time ->
//            try {
//                val log = MedicationLogs(
//                    medicationId = medicationId,
//                    scheduleId = scheduleId,
//                    plannedDatetime = LocalDateTime.of(date, time),
//                    takenDatetime = null,
//                    status = MedicationStatus.PENDING
//                )
//                Log.d("testcat", "Attempting to insert log: $log")
//                medicationLogDao.insert(log)
//                Log.d("testcat", "Successfully inserted log")
//            } catch (e: Exception) {
//                Log.e("testcat", "Failed to insert log: ${e.message}", e)
//                e.printStackTrace()            }
//        }
//    }



    companion object {
        private const val TAG = "CreateFutureMedicationLogsWorker"
        private const val MIN_FUTURE_DAYS = 7
        private const val DAYS_TO_CREATE = 7
    }
}