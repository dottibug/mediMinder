package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediminder.data.local.classes.Dosage

// Abstraction layer for the dosages table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface DosageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dosage: Dosage): Long

    // Get dosage by medication ID
    @Query("SELECT * FROM dosages WHERE medication_id = :medicationId")
    suspend fun getDosageByMedicationId(medicationId: Long): Dosage?

    @Query("DELETE FROM dosages")
    suspend fun deleteAll()

    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'dosages'")
    suspend fun resetSequence()
}