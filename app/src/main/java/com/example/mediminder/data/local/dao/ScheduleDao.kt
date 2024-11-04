package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mediminder.data.local.classes.Schedules

// Abstraction layer for the schedules table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedules): Long

    @Query("SELECT * FROM schedules WHERE medication_id = :medicationId")
    suspend fun getScheduleByMedicationId(medicationId: Long): Schedules?

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()

    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'schedules'")
    suspend fun resetSequence()

    @Update
    suspend fun update(schedule: Schedules)
}