package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediminder.data.local.classes.MedicationLogs

// Abstraction layer for the medication_logs table in the database
// https://developer.android.com/training/data-storage/room/accessing-data

@Dao
interface MedicationLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicationLog: MedicationLogs): Long

    @Query("DELETE FROM medication_logs")
    suspend fun deleteAll()
}