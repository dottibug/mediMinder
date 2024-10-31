package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.Medication

// Abstraction layer for the medication table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface MedicationDao {

    // Add after getAllWithRemindersEnabled()
    @Query("SELECT * FROM medications WHERE id = :medicationId")
    suspend fun getMedicationById(medicationId: Long): Medication

    @Query("SELECT * FROM medications WHERE reminder_enabled = 1")
    suspend fun getAllWithRemindersEnabled(): List<Medication>

    @Query("SELECT * FROM medications")
    suspend fun getAll(): List<Medication>

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