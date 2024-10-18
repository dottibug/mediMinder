package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.Medication
import java.time.LocalDate

// Abstraction layer for the medication table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface MedicationDao {

    // Get the scheduled medications for a given date (excludes as-needed medications)
    @Query("SELECT DISTINCT m.* FROM medications m " +
            "JOIN schedules s ON m.id = s.medication_id " +
            "WHERE s.frequency_type != 'as_needed' AND s.start_date <= :date AND (s.end_date IS NULL OR s.end_date >= :date)")
    suspend fun getScheduledMedicationsForDate(date: LocalDate): List<Medication>

    // Get as-needed medications
    @Query("SELECT DISTINCT m.* FROM medications m " +
            "JOIN schedules s ON m.id = s.medication_id " +
            "WHERE s.frequency_type = 'as_needed'")
    suspend fun getAsNeededMedications(): List<Medication>





    /////////////////////////
    @Query("SELECT * FROM medications")
    suspend fun getAll(): List<Medication>

    @Query("SELECT DISTINCT m.* FROM medications m " +
            "JOIN schedules s ON m.id = s.medication_id " +
            "WHERE s.start_date <= :date AND (s.end_date IS NULL OR s.end_date >= :date)")
    suspend fun getMedicationsForDate(date: LocalDate): List<Medication>

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getMedicationCount(): Int

    @Query("DELETE FROM medications")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)
}