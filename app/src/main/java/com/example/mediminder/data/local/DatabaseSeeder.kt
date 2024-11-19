package com.example.mediminder.data.local

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.receivers.MedicationSchedulerReceiver
import com.example.mediminder.schedulers.MidnightMedicationScheduler
import com.example.mediminder.utils.AppUtils.getHourlyReminderTimes
import com.example.mediminder.workers.CheckMissedMedicationsWorker
import com.example.mediminder.workers.CreateFutureMedicationLogsWorker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

// This class is for "seeding" the database with initial test data.
//  It is for development/testing and demo purposes.

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

            // Blood Pressure Med: Started 3 months ago, continuous daily schedule with
            // morning and evening reminders
            val bloodPressureMed = medicationDao.insert(
                Medication(
                    name = "Lisinopril",
                    prescribingDoctor = "Dr. Smith",
                    notes = "Take with food. Monitor blood pressure.",
                    icon = MedicationIcon.TABLET,
                    reminderEnabled = true,
                    asNeeded = false,
                )
            )

            // Antibiotic: Short-term daily medication (10-day course) taken 3 times daily. Started 5 days ago.
            val antibioticMed = medicationDao.insert(
                Medication(
                    name = "Amoxicillin",
                    prescribingDoctor = "Dr. Smith",
                    notes = "Complete full course. Take with water.",
                    icon = MedicationIcon.CAPSULE,
                    reminderEnabled = true,
                    asNeeded = false,
                )
            )

            // Vitamin B12: Started 2 months ago. Continuous on specific days (every Sunday).
            val vitaminB12 = medicationDao.insert(
                Medication(
                    name = "Vitamin B12",
                    prescribingDoctor = "Dr. Johnson",
                    notes = "Weekly supplement",
                    icon = MedicationIcon.CAPSULE,
                    reminderEnabled = true,
                    asNeeded = false
                )
            )

            // Anti-inflammatory: Started 1 month ago. Continuous, taken every 2 days (interval-based schedule).
            val antiInflammatoryMed = medicationDao.insert(
                Medication(
                    name = "Naproxen",
                    prescribingDoctor = "Dr. Chan",
                    notes = "Take with food to prevent stomach upset",
                    icon = MedicationIcon.TABLET,
                    reminderEnabled = true,
                    asNeeded = false
                )
            )

            // Allergy Med: Started 1.5 months ago. Continuous on specific days (Monday, Wednesday, Friday).
            // Reminders every 6 hours between 8 am and 8 pm.
            val allergyMed = medicationDao.insert(
                Medication(
                    name = "Allegra",
                    prescribingDoctor = "Dr. Chan",
                    notes = "For seasonal allergies",
                    icon = MedicationIcon.TABLET,
                    reminderEnabled = true,
                    asNeeded = false
                )
            )

            // Insert dosages
            dosageDao.insert(Dosage(medicationId = bloodPressureMed, amount = "10", units = "mg"))
            dosageDao.insert(Dosage(medicationId = antibioticMed, amount = "500", units = "mg"))
            dosageDao.insert(Dosage(medicationId = vitaminB12, amount = "1000", units = "mcg"))
            dosageDao.insert(Dosage(medicationId = antiInflammatoryMed, amount = "250", units = "mg"))
            dosageDao.insert(Dosage(medicationId = allergyMed, amount = "180", units = "mg"))

            // Insert schedules
            val threeMonthsAgo = LocalDate.now().minusMonths(3)
            val fiveDaysAgo = LocalDate.now().minusDays(5)
            val twoMonthsAgo = LocalDate.now().minusMonths(2)
            val oneMonthAgo = LocalDate.now().minusMonths(1)
            val sixWeeksAgo = LocalDate.now().minusWeeks(6)

            val bloodPressureSchedule = scheduleDao.insert(
                Schedules(
                    medicationId = bloodPressureMed,
                    startDate = threeMonthsAgo,
                    durationType = "continuous",
                    numDays = 0,
                    scheduleType = "daily",
                    selectedDays = "",
                    daysInterval = 0
                )
            )

            val antibioticSchedule = scheduleDao.insert(
                Schedules(
                    medicationId = antibioticMed,
                    startDate = fiveDaysAgo,
                    durationType = "numDays",
                    numDays = 10,
                    scheduleType = "daily",
                    selectedDays = "",
                    daysInterval = 0
                )
            )

            val vitaminBSchedule = scheduleDao.insert(
                Schedules(
                    medicationId = vitaminB12,
                    startDate = twoMonthsAgo,
                    durationType = "continuous",
                    numDays = 0,
                    scheduleType = "specificDays",
                    selectedDays = "7",  // Sun
                    daysInterval = 0
                )
            )

            val antiInflammatorySchedule = scheduleDao.insert(
                Schedules(
                    medicationId = antiInflammatoryMed,
                    startDate = oneMonthAgo,
                    durationType = "continuous",
                    numDays = 0,
                    scheduleType = "interval",
                    selectedDays = "",
                    daysInterval = 2
                )
            )

            val allergySchedule = scheduleDao.insert(
                Schedules(
                    medicationId = allergyMed,
                    startDate = sixWeeksAgo,
                    durationType = "continuous",
                    numDays = 0,
                    scheduleType = "specificDays",
                    selectedDays = "1,3,5",     // Mon, Wed, Fri
                    daysInterval = 0
                    )
            )

            // Insert reminders
            medRemindersDao.insert(
                MedReminders(
                    medicationId = bloodPressureMed,
                    reminderFrequency = "daily",
                    hourlyReminderInterval = "",
                    hourlyReminderStartTime = null,
                    hourlyReminderEndTime = null,
                    dailyReminderTimes = listOf(
                        LocalTime.of(8, 0),     // 8 am
                        LocalTime.of(20, 0)     // 8 pm
                    )
                )
            )

            medRemindersDao.insert(
                MedReminders(
                    medicationId = antibioticMed,
                    reminderFrequency = "daily",
                    hourlyReminderInterval = "",
                    hourlyReminderStartTime = null,
                    hourlyReminderEndTime = null,
                    dailyReminderTimes = listOf(
                        LocalTime.of(8, 0),     // 8 am
                        LocalTime.of(14, 0),    // 2 pm
                        LocalTime.of(20, 0)     // 8 pm
                    )
                )
            )

            medRemindersDao.insert(
                MedReminders(
                    medicationId = vitaminB12,
                    reminderFrequency = "daily",
                    hourlyReminderInterval = "",
                    hourlyReminderStartTime = null,
                    hourlyReminderEndTime = null,
                    dailyReminderTimes = listOf(LocalTime.of(9, 0))
                )
            )

            medRemindersDao.insert(
                MedReminders(
                    medicationId = antiInflammatoryMed,
                    reminderFrequency = "daily",
                    hourlyReminderInterval = "",
                    hourlyReminderStartTime = null,
                    hourlyReminderEndTime = null,
                    dailyReminderTimes = listOf(LocalTime.of(12, 0))
                )
            )

            medRemindersDao.insert(
                MedReminders(
                    medicationId = allergyMed,
                    reminderFrequency = "every x hours",
                    hourlyReminderInterval = "6",
                    hourlyReminderStartTime = LocalTime.of(8, 0),
                    hourlyReminderEndTime = LocalTime.of(20, 0),
                    dailyReminderTimes = emptyList()
                )
            )

            Log.d("DatabaseSeeder testcat", "Database seeded")

            // Create past logs
            createPastLogs(
                listOf(
                    Triple(bloodPressureMed, bloodPressureSchedule, threeMonthsAgo),
                    Triple(antibioticMed, antibioticSchedule, fiveDaysAgo),
                    Triple(vitaminB12, vitaminBSchedule, twoMonthsAgo),
                    Triple(antiInflammatoryMed, antiInflammatorySchedule, oneMonthAgo),
                    Triple(allergyMed, allergySchedule, sixWeeksAgo)
                ),
                medicationLogDao,
                scheduleDao,
                medRemindersDao
            )

            // Trigger the worker to create future logs for all medications
            val workManager = WorkManager.getInstance(applicationContext)
            val createFutureLogsRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>().build()
            val checkMissedRequest = OneTimeWorkRequestBuilder<CheckMissedMedicationsWorker>().build()

            workManager.beginUniqueWork(
                "initial_setup",
                ExistingWorkPolicy.REPLACE,
                createFutureLogsRequest
            ).then(checkMissedRequest).enqueue()

            // Observe work completion and schedule notifications after
            workManager.getWorkInfoByIdLiveData(createFutureLogsRequest.id)
                .observeForever { workInfo ->
                    if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                        val schedulerIntent = Intent(applicationContext, MedicationSchedulerReceiver::class.java).apply {
                            action = "com.example.mediminder.SCHEDULE_NEW_MEDICATION"
                        }
                        applicationContext.sendBroadcast(schedulerIntent)
                        MidnightMedicationScheduler(applicationContext).scheduleMidnightAlarm()
                    }
                }

            Log.d("DatabaseSeeder testcat", "Triggered CreateFutureMedicationLogsWorker")
        } catch (e: Exception) {
            Log.e("DatabaseSeeder testcat", "Error seeding database: ${e.message}", e)
            throw e
        }
    }

    private suspend fun createPastLogs(
        medications: List<Triple<Long, Long, LocalDate>>,
        medicationLogDao: MedicationLogDao,
        scheduleDao: ScheduleDao,
        medRemindersDao: MedRemindersDao
    ) {
        val today = LocalDate.now()

        medications.forEach { (medicationId, scheduleId, startDate) ->
            val schedule = scheduleDao.getScheduleByMedicationId(medicationId)
            val reminder = medRemindersDao.getReminderByMedicationId(medicationId)

            if (schedule != null && reminder != null) {
                var currentDate = startDate

                while (currentDate.isBefore(today)) {
                    if (isMedScheduledForDate(schedule, currentDate)) {
                        val reminderTimes = getReminderTimes(reminder)
                        createPastLogsForDate(
                            medicationId,
                            scheduleId,
                            currentDate,
                            reminderTimes,
                            medicationLogDao
                        )
                    }
                    currentDate = currentDate.plusDays(1)
                }
            }
        }
    }

    private fun isMedScheduledForDate(schedule: Schedules, date: LocalDate): Boolean {
        return when (schedule.scheduleType) {
            "daily" -> true
            "specificDays" -> {
                val dayOfWeek = date.dayOfWeek.value
                schedule.selectedDays.split(",").map { it.toInt() }.contains(dayOfWeek)
            }
            "interval" -> {
                val daysBetween = ChronoUnit.DAYS.between(schedule.startDate, date).toInt()
                daysBetween % (schedule.daysInterval ?: 1) == 0
            }
            else -> false
        }
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

    private suspend fun createPastLogsForDate(
        medicationId: Long,
        scheduleId: Long,
        date: LocalDate,
        reminderTimes: List<LocalTime>,
        medicationLogDao: MedicationLogDao
    ) {
        reminderTimes.forEach { time ->
            val plannedDateTime = LocalDateTime.of(date, time)
            val (status, takenDateTime) = generateMedStatus(plannedDateTime)

            medicationLogDao.insert(
                MedicationLogs(
                    medicationId = medicationId,
                    scheduleId = scheduleId,
                    plannedDatetime = plannedDateTime,
                    takenDatetime = takenDateTime,
                    status = status,
                    asNeededDosageAmount = null,
                    asNeededDosageUnit = null
                )
            )
        }
    }

    // Generate a medication status (80% taken, 15% missed, 5% skipped)
    private fun generateMedStatus(plannedDateTime: LocalDateTime): Pair<MedicationStatus, LocalDateTime?> {
        val random = Random.nextFloat()

        return when {
            random < 0.8 -> {
                val randomMins = Random.nextLong(-30, 30)
                val takenTime = plannedDateTime.minusMinutes(randomMins)
                Pair(MedicationStatus.TAKEN, takenTime)
            }
            random < 0.95 -> Pair(MedicationStatus.MISSED, null)
            else -> Pair(MedicationStatus.SKIPPED, null)
        }
    }

    suspend fun clearDatabase() {
        try {
            medicationDao.deleteAll()
            dosageDao.deleteAll()
            medRemindersDao.deleteAll()
            scheduleDao.deleteAll()
            medicationLogDao.deleteAll()

            medicationDao.resetSequence()
            dosageDao.resetSequence()
            medRemindersDao.resetSequence()
            scheduleDao.resetSequence()
            medicationLogDao.resetSequence()

            return
        } catch (e: Exception) {
            Log.e("DatabaseSeeder testcat", "Error clearing database: ${e.message}", e)
            throw e
        }
    }
}