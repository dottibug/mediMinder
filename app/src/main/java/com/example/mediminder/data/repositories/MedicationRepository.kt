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

    suspend fun getAllMedicationsWithDosages(): List<Pair<Medication, Dosage?>> {
        val medications = medicationDao.getAll()
        Log.d("testcat MedicationRepository", "Fetched ${medications.size} medications")


        return medications.map { medication ->
            val dosage = dosageDao.getDosageByMedicationId(medication.id)
            Log.d("testcat MedicationRepository", "Medication: ${medication.name}, Dosage: $dosage")

            Pair(medication, dosage)
        }
    }

}

