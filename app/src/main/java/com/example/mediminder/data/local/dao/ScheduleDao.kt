package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.Schedules

/**
 * Data Access Object (DAO) for schedule-related database operations.
 */
@Dao
interface ScheduleDao {
    // Insert a schedule entity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedules): Long

    // Update a schedule entity
    @Update
    suspend fun update(schedule: Schedules)

    // Get schedules by medication ID
    @Query("SELECT * FROM schedules WHERE medication_id = :medicationId")
    suspend fun getScheduleByMedicationId(medicationId: Long): Schedules?

    // Delete all schedules
    @Query("DELETE FROM schedules")
    suspend fun deleteAll()

    // Reset the sequence for the schedules table
    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'schedules'")
    suspend fun resetSequence()
}