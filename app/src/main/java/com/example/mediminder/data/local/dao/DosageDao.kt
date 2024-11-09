package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.Dosage

// Abstraction layer for the dosages table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface DosageDao {
    // Insert dosage into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dosage: Dosage): Long

    // Update a dosage entity
    @Update
    suspend fun update(dosage: Dosage)

    // Get dosage by medication ID
    @Query("SELECT * FROM dosages WHERE medication_id = :medicationId")
    suspend fun getDosageByMedicationId(medicationId: Long): Dosage?

    // Delete all dosages
    @Query("DELETE FROM dosages")
    suspend fun deleteAll()

    // Reset the sequence for the dosages table
    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'dosages'")
    suspend fun resetSequence()
}