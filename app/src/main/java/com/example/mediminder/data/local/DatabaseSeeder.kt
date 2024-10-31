package com.example.mediminder.data.local

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.receivers.MedicationSchedulerReceiver
import com.example.mediminder.workers.CreateFutureMedicationLogsWorker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// This class is for "seeding" the database with initial test data.
//  It is only for development/testing purposes.

class DatabaseSeeder(
    private val applicationContext: Context,
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val medRemindersDao: MedRemindersDao,
    private val scheduleDao: ScheduleDao,
    private val medicationLogDao: MedicationLogDao
) {
    suspend fun seedDatabase() {

        try {
            Log.d("DatabaseSeeder testcat", "Starting database seeding")

            // 1. Insert all medications first
            val testMedAlarm = medicationDao.insert(
                Medication(
                    name = "Test Alarm Med",
                    prescribingDoctor = "Dr. Test",
                    notes = "This is a test medication for alarm testing",
                    reminderEnabled = true,
                )
            )

            val testMed1 = medicationDao.insert(
                Medication(
                    name = "Hearto",
                    prescribingDoctor = "Dr. Batman",
                    notes = "Take with food",
                    reminderEnabled = true,
                )
            )

            val testMed2 = medicationDao.insert(
                Medication(
                    name = "Ibuprofen",
                    prescribingDoctor = "Dr. Joker",
                    notes = "Take as needed",
                    reminderEnabled = false,
                )
            )

            val testMed3 = medicationDao.insert(
                Medication(
                    name = "Pressure thingy",
                    prescribingDoctor = "Dr. Batman",
                    notes = "",
                    reminderEnabled = true,
                )
            )

            Log.d("DatabaseSeeder testcat", "Medications inserted")

            // 2. Insert all dosages
            dosageDao.insert(
                Dosage(
                    medicationId = testMedAlarm,
                    amount = "1",
                    units = "mg"
                )
            )

            dosageDao.insert(
                Dosage(
                    medicationId = testMed1,
                    amount = "5",
                    units = "mg"
                )
            )

            dosageDao.insert(
                Dosage(
                    medicationId = testMed2,
                    amount = "200",
                    units = "mg"
                )
            )

            dosageDao.insert(
                Dosage(
                    medicationId = testMed3,
                    amount = "10",
                    units = "mcg"
                )
            )

            Log.d("DatabaseSeeder testcat", "Dosages inserted")

            // 3. Insert all schedules
            val testMedAlarmScheduled = scheduleDao.insert(
                Schedules(
                    medicationId = testMedAlarm,
                    startDate = LocalDate.now(),
                    durationType = "continuous",
                    numDays = 0,
                    scheduleType = "daily",
                    selectedDays = "",
                    daysInterval = 0
                )
            )

            val schedule1 = scheduleDao.insert(
                Schedules(
                    medicationId = testMed1,
                    startDate = LocalDate.now(),
                    durationType = "continuous",
                    numDays = 0,
                    scheduleType = "daily",
                    selectedDays = "",
                    daysInterval = 0
                )
            )

            val schedule2 = scheduleDao.insert(
                Schedules(
                    medicationId = testMed2,
                    startDate = LocalDate.now(),
                    durationType = "numDays",
                    numDays = 7,
                    scheduleType = "daily",
                    selectedDays = "",
                    daysInterval = 0
                )
            )

            val schedule3 = scheduleDao.insert(
                Schedules(
                    medicationId = testMed3,
                    startDate = LocalDate.now(),
                    durationType = "continuous",
                    numDays = 0,
                    scheduleType = "specificDays",
                    selectedDays = "1,3,5", // Monday, Wednesday, Friday
                    daysInterval = 0
                )
            )

            Log.d("DatabaseSeeder testcat", "Schedules inserted")


            // 4. Insert all reminders
            val now = LocalTime.now()
            val testTime = now.plusMinutes(2)

            medRemindersDao.insert(
                MedReminders(
                    medicationId = testMedAlarm,
                    reminderFrequency = "daily",
                    hourlyReminderInterval = "",
                    hourlyReminderStartTime = null,
                    hourlyReminderEndTime = null,
                    dailyReminderTimes = listOf(
                        LocalTime.of(testTime.hour, testTime.minute)
                    )
                )
            )

            medRemindersDao.insert(
                MedReminders(
                    medicationId = testMed1,
                    reminderFrequency = "daily",
                    hourlyReminderInterval = "",
                    hourlyReminderStartTime = null,
                    hourlyReminderEndTime = null,
                    dailyReminderTimes = listOf(LocalTime.of(12, 0), LocalTime.of(18, 0)),
                )
            )

            medRemindersDao.insert(
                MedReminders(
                    medicationId = testMed3,
                    reminderFrequency = "hourly",
                    hourlyReminderInterval = "6",
                    hourlyReminderStartTime = LocalTime.of(8, 30),
                    hourlyReminderEndTime = LocalTime.of(21, 0),
                    dailyReminderTimes = emptyList(),
                )
            )

            Log.d("DatabaseSeeder testcat", "Reminders inserted")

            // 5. Finally, insert medication logs
            val today = LocalDate.now()

            medicationLogDao.insert(
                MedicationLogs(
                    medicationId = testMedAlarm,
                    scheduleId = testMedAlarmScheduled,
                    plannedDatetime = LocalDateTime.of(
                        LocalDate.now(), LocalTime.of(testTime.hour, testTime.minute)
                    ),
                    takenDatetime = null,
                    status = MedicationStatus.PENDING
                )
            )

            listOf(LocalTime.of(12, 0), LocalTime.of(18, 0)).forEach { time ->
                medicationLogDao.insert(
                    MedicationLogs(
                        medicationId = testMed1,
                        scheduleId = schedule1,
                        plannedDatetime = LocalDateTime.of(today, time),
                        takenDatetime = null,
                        status = MedicationStatus.PENDING
                    )
                )
            }

            val pastDateTime = LocalDateTime.now().minusHours(3)
            medicationLogDao.insert(
                MedicationLogs(
                    medicationId = testMed3,
                    scheduleId = schedule3,
                    plannedDatetime = pastDateTime,
                    takenDatetime = null,
                    status = MedicationStatus.PENDING
                )
            )

            Log.d("DatabaseSeeder testcat", "Medication logs inserted")

            // Trigger the worker to create future logs for all medications
            val workManager = WorkManager.getInstance(applicationContext)
            val createFutureLogsRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>().build()
            workManager.enqueue(createFutureLogsRequest)
            // After triggering CreateFutureMedicationLogsWorker
            val schedulerIntent = Intent(applicationContext, MedicationSchedulerReceiver::class.java).apply {
                action = "com.example.mediminder.SCHEDULE_NEW_MEDICATION"
            }
            applicationContext.sendBroadcast(schedulerIntent)

            Log.d("DatabaseSeeder testcat", "Triggered CreateFutureMedicationLogsWorker")
        } catch (e: Exception) {
            Log.e("DatabaseSeeder testcat", "Error seeding database: ${e.message}", e)
            throw e
        }
    }

    suspend fun clearDatabase() {
        try {
            medicationDao.deleteAll()
            dosageDao.deleteAll()
            medRemindersDao.deleteAll()
            scheduleDao.deleteAll()
            medicationLogDao.deleteAll()
        } catch (e: Exception) {
            Log.e("DatabaseSeeder testcat", "Error clearing database: ${e.message}", e)
            throw e
        }
    }
}