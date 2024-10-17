package com.example.mediminder.data.repositories
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.dao.MedicationDao
import java.time.LocalDate

class MedicationRepository(private val medicationDao: MedicationDao) {
    suspend fun getMedicationCount(): Int {
        return medicationDao.getMedicationCount()
    }

    suspend fun getMedicationsForDate(date: LocalDate): List<Medication> {
        return medicationDao.getMedicationsForDate(date)
    }

}