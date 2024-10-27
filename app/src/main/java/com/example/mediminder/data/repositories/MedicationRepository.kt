package com.example.mediminder.data.repositories
import android.util.Log
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedRemindersDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.ScheduleDao
import com.example.mediminder.viewmodels.DosageData
import com.example.mediminder.viewmodels.MedicationData
import com.example.mediminder.viewmodels.ReminderData
import com.example.mediminder.viewmodels.ScheduleData
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val remindersDao: MedRemindersDao,
    private val scheduleDao: ScheduleDao
) {

    suspend fun addMedication(
        medicationData: MedicationData,
        dosageData: DosageData,
        reminderData: ReminderData,
        scheduleData: ScheduleData
    ) {

        try {
            val medicationId = medicationDao.insert(
                Medication(
                    name = medicationData.name,
                    prescribingDoctor = medicationData.doctor,
                    notes = medicationData.notes,
                    reminderEnabled = reminderData.reminderEnabled
                )
            )

            dosageDao.insert(
                Dosage(
                    medicationId = medicationId,
                    amount = dosageData.dosageAmount,
                    units = dosageData.dosageUnits
                )
            )

            scheduleDao.insert(
                Schedules(
                    medicationId = medicationId,
                    startDate = scheduleData.startDate ?: LocalDate.now(),
                    durationType = scheduleData.durationType,
                    numDays = scheduleData.numDays,
                    scheduleType = scheduleData.scheduleType,
                    selectedDays = scheduleData.selectedDays,
                    daysInterval = scheduleData.daysInterval
                )
            )

            if (reminderData.reminderEnabled) {
                remindersDao.insert(
                    MedReminders(
                        medicationId = medicationId,
                        reminderFrequency = reminderData.reminderFrequency,
                        hourlyReminderInterval = reminderData.hourlyReminderInterval,
                        hourlyReminderStartTime = reminderData.hourlyReminderStartTime?.let { LocalTime.of(it.first, it.second) },
                        dailyReminderTimes = reminderData.dailyReminderTimes.map { LocalTime.of(it.first, it.second) },
                        reminderType = reminderData.reminderType
                    )
                )
            }

            Log.d("testcat MedicationRepository", "Medication added: $medicationData, Dosage: $dosageData, Reminder: $reminderData, Schedule: $scheduleData")

        } catch (e: Exception) {
            Log.e("testcat MedicationRepository", "Error adding medication: ${e.message}")
            // todo: throw e    to have calling functions handle errors
        }
    }

    suspend fun getMedicationsForDate(date: LocalDate): List<Pair<Medication, Dosage?>> {
        val allMeds = medicationDao.getAllWithRemindersEnabled()

        Log.d("testcat MedicationRepository", "Fetched ${allMeds.size} medications")

        return allMeds.filter { medication ->
            val schedule = scheduleDao.getScheduleByMedicationId(medication.id)
            isScheduledForDate(schedule, date)
        }.map { medication ->
            val dosage = dosageDao.getDosageByMedicationId(medication.id)
            Log.d("testcat MedicationRepository", "Medication: ${medication.name}, Dosage: $dosage")
            Pair(medication, dosage)
        }
    }

    private fun isScheduledForDate(schedule: Schedules?, date: LocalDate): Boolean {
        if (schedule == null) return false
        if (!dateWithinMedicationDuration(date, schedule)) return false

        return when (schedule.scheduleType) {
            "daily" -> true

            // Note: Kotlin dayOfWeek is 1 to 7 (Monday to Sunday). Our database uses 0 to 6
            //  (Sunday to Saturday), so we need to add 1 to the day of the week.
            "specificDays" -> {
                val dayOfWeek = (date.dayOfWeek.value % 7) + 1
                schedule.selectedDays.split(",").contains(dayOfWeek.toString())
            }

            // Checks if the number of days between the start date and current date is evenly
            // divisible by the interval; if it is, the medication is scheduled for the current day
            // Ex. Interval = 3
            // Day 0 (start date): 0 % 3 == 0, medication is scheduled
            // Day 1: 1 % 3 == 1, medication is not scheduled
            // Day 2: 2 % 3 == 2, medication is not scheduled
            // Day 3: 3 % 3 == 0, medication is scheduled
            // Day 4: 4 % 3 == 1, medication is not scheduled, etc
            "interval" -> {
                val daysSinceStart = ChronoUnit.DAYS.between(schedule.startDate, date).toInt()
                daysSinceStart % (schedule.daysInterval ?: 1) == 0
            }

            else -> false
        }

    }

    // Checks if the medication is within the scheduled number of days from the start date
    private fun dateWithinMedicationDuration(date: LocalDate, schedule: Schedules): Boolean {
        if (date < schedule.startDate) return false

        return when (schedule.durationType) {
            "continuous" -> true
            "numDays" -> date <= schedule.startDate.plusDays(schedule.numDays?.toLong() ?: 0)
            else -> false
        }
    }
}

