package com.example.mediminder.data.local

import android.util.Log
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// This class is for "seeding" the database with initial test data.
//  It is only for development/testing purposes.

class DatabaseSeeder(
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val medRemindersDao: MedRemindersDao,
    private val scheduleDao: ScheduleDao,
    private val medicationLogDao: MedicationLogDao
) {
    suspend fun seedDatabase() {
        Log.d("testcat DatabaseSeeder", "Starting database seeding")

        //// TEST MED 1
        val testMed1 = Medication(
            name = "Hearto",
            prescribingDoctor = "Dr. Batman",
            notes = "Take with food",
            reminderEnabled = true,
        )

        val testMedId1 = medicationDao.insert(testMed1)

        val testDosage1 = Dosage(
            medicationId = testMedId1,
            amount = "5",
            units = "mg"
        )

        dosageDao.insert(testDosage1)

        val reminder1 = MedReminders(
            medicationId = testMedId1,
            reminderFrequency = "daily",
            hourlyReminderInterval = "",
            hourlyReminderStartTime = null,
            hourlyReminderEndTime = null,
            dailyReminderTimes = listOf(LocalTime.of(12, 0), LocalTime.of(18, 0)),
        )

        medRemindersDao.insert(reminder1)

        val schedule1 = Schedules(
            medicationId = testMedId1,
            startDate = LocalDate.now(),
            durationType = "continuous",
            numDays = 0,
            scheduleType = "daily",
            selectedDays = "",
            daysInterval = 0
        )

        val scheduleId1 = scheduleDao.insert(schedule1)


        ///// TEST MED 2
        val testMed2 = Medication(
            name = "Ibuprofen",
            prescribingDoctor = "Dr. Joker",
            notes = "Take as needed",
            reminderEnabled = false,
        )

        val testMedId2 = medicationDao.insert(testMed2)

        val testDosage2 = Dosage(
            medicationId = testMedId2,
            amount = "200",
            units = "mg"
        )

        dosageDao.insert(testDosage2)

        val schedule2 = Schedules(
            medicationId = testMedId2,
            startDate = LocalDate.now(),
            durationType = "numDays",
            numDays = 7,
            scheduleType = "daily",
            selectedDays = "",
            daysInterval = 0
        )
        scheduleDao.insert(schedule2)


        ///// TEST MED 3
        val testMed3 = Medication(
            name = "Pressure thingy",
            prescribingDoctor = "Dr. Batman",
            notes = "",
            reminderEnabled = true,
        )

        val testMedId3 = medicationDao.insert(testMed3)

        val testDosage3 = Dosage(
            medicationId = testMedId3,
            amount = "10",
            units = "mcg"
        )

        dosageDao.insert(testDosage3)

        val reminder3 = MedReminders(
            medicationId = testMedId3,
            reminderFrequency = "hourly",
            hourlyReminderInterval = "6",
            hourlyReminderStartTime = LocalTime.of(8, 30),
            hourlyReminderEndTime = LocalTime.of(21, 0),
            dailyReminderTimes = emptyList(),
        )
        medRemindersDao.insert(reminder3)

        val schedule3 = Schedules(
            medicationId = testMedId3,
            startDate = LocalDate.now(),
            durationType = "continuous",
            numDays = 0,
            scheduleType = "specificDays",
            selectedDays = "2,4,6", // Monday, Wednesday, Friday
            daysInterval = 0
        )
        scheduleDao.insert(schedule3)

        Log.d("testcat DatabaseSeeder", "Database seeding completed")

        val pastDateTime = LocalDateTime.now().minusHours(3)

        medicationLogDao.insert(
            MedicationLogs(
                medicationId = testMedId1,
                scheduleId = scheduleId1,
                plannedDatetime = pastDateTime, // 3 hours ago
                takenDatetime = null,
                status = MedicationStatus.PENDING // should get flagged by worker as missed
            )
        )
    }

    suspend fun clearDatabase() {
        medicationDao.deleteAll()
        dosageDao.deleteAll()
        medRemindersDao.deleteAll()
        scheduleDao.deleteAll()
        medicationLogDao.deleteAll()
    }
}