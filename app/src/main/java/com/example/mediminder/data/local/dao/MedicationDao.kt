package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.Medication

// Abstraction layer for the medication table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface MedicationDao {
    // Insert medication into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    // Update a medication entity
    @Update
    suspend fun update(medication: Medication)

    // Get medication count
    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getCount(): Int

    // Get all medications
    @Query("SELECT * FROM medications")
    suspend fun getAll(): List<Medication>

    // Get all scheduled medications
    @Query("SELECT * FROM medications WHERE as_needed = 0")
    suspend fun getAllScheduledMedications(): List<Medication>

    // Get medication by medication id
    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationById(medicationId: Long): Medication

    // Get all medications with reminders enabled
//    @Query("SELECT * FROM medications WHERE reminder_enabled = 1")
//    suspend fun getAllWithRemindersEnabled(): List<Medication>

    // Get all as-needed medications
    @Query("SELECT * FROM medications WHERE as_needed = 1")
    suspend fun getAsNeededMedications(): List<Medication>

    // Delete all medications
    @Query("DELETE FROM medications")
    suspend fun deleteAll()

    // Delete medication by id
    @Query("DELETE FROM medications WHERE id = :medicationId")
    suspend fun deleteById(medicationId: Long)

    // Reset the sequence for the medications table
    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'medications'")
    suspend fun resetSequence()
}