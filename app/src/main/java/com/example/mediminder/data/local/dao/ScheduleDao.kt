package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediminder.data.local.classes.Schedules

// Abstraction layer for the schedules table in the database
// https://developer.android.com/training/data-storage/room/accessing-data
@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedules): Long

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}