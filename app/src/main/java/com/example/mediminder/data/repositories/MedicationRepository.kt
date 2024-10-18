package com.example.mediminder.data.repositories
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.local.dao.DosageDao
import com.example.mediminder.data.local.dao.MedicationDao
import com.example.mediminder.data.local.dao.ScheduleDao
import java.time.LocalDate

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val dosageDao: DosageDao,
    private val scheduleDao: ScheduleDao
) {

    // TODO: this is used to check if the table is empty (but we maybe only need to count scheduled meds?)
    suspend fun getMedicationCount(): Int {
        return medicationDao.getMedicationCount()
    }

    // Get the scheduled medications for a given date
    suspend fun getScheduledMedicationsForDate(date: LocalDate): List<MedicationWithDosage> {
        return medicationDao.getScheduledMedicationsForDate(date).map { medication ->
            val dosage = dosageDao.getDosageForMedication(medication.id)
            MedicationWithDosage(medication, dosage)
        }
    }

    suspend fun getAsNeededMedications(): List<MedicationWithDosage> {
        return medicationDao.getAsNeededMedications().map { medication ->
            val dosage = dosageDao.getDosageForMedication(medication.id)
            MedicationWithDosage(medication, dosage)
        }
    }

    suspend fun insertMedication(medication: Medication): Long {
        return medicationDao.insert(medication)
    }

    suspend fun insertDosage(dosage: Dosage): Long {
        return dosageDao.insert(dosage)
    }

    suspend fun insertSchedule(schedule: Schedules): Long {
        return scheduleDao.insert(schedule)
    }

////////////////////////////
    suspend fun getMedicationsForDate(date: LocalDate): List<Medication> {
        return medicationDao.getMedicationsForDate(date)
    }

}

// Data class to represent a medication with its dosage
data class MedicationWithDosage(
    val medication: Medication,
    val dosage: Dosage?
)