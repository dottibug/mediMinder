package com.example.mediminder.data.local

import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.MedicationLogDao
import com.example.mediminder.data.local.dao.ScheduleDao
import java.time.LocalDate
import java.time.LocalTime

// This class is for "seeding" the database with initial test data.
//  It is only for development/testing purposes.

class DatabaseSeeder(
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val scheduleDao: ScheduleDao,
    private val medicationLogDao: MedicationLogDao
) {
    suspend fun seedDatabase() {
        val med1 = Medication(name="Aspirin", prescribingDoctor = "Dr. Smith", notes="Take with food")
        val med2 = Medication(name="Ibuprofen", prescribingDoctor = "Dr. Johnson", notes="Take as needed")

        val med1Id = medicationDao.insert(med1)
        val med2Id = medicationDao.insert(med2)

        val dosage1 = Dosage(medicationId = med1Id, amount = 500.0, units = "mg")
        val dosage2 = Dosage(medicationId = med2Id, amount = 200.0, units = "mg")

        dosageDao.insert(dosage1)
        dosageDao.insert(dosage2)

        val schedule1 = Schedules(
            medicationId = med1Id,
            frequencyType = "daily",
            frequencyAmount = 1,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusDays(30),
            daysOfWeek = "0,1,2,3,4,5,6",
            timeOfDay = LocalTime.of(8, 0),
            daysOfMonth = null
        )

        val schedule2 = Schedules(
            medicationId = med2Id,
            frequencyType = "as_needed",
            frequencyAmount = null,
            startDate = LocalDate.now(),
            endDate = null,
            daysOfWeek = null,
            timeOfDay = null,
            daysOfMonth = null
        )

        scheduleDao.insert(schedule1)
        scheduleDao.insert(schedule2)

    }

    suspend fun clearDatabase() {
        medicationLogDao.deleteAll()
        scheduleDao.deleteAll()
        dosageDao.deleteAll()
        medicationDao.deleteAll()
    }
}