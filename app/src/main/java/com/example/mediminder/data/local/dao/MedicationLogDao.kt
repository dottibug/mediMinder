package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.MedicationStatus
import java.time.LocalDateTime

// Abstraction layer for the medication_logs table in the database
// https://developer.android.com/training/data-storage/room/accessing-data

@Dao
interface MedicationLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicationLog: MedicationLogs): Long


    @Query("SELECT * FROM medication_logs WHERE status = 'PENDING' AND planned_datetime <= :cutOffTime")
    suspend fun getPendingMedicationLogs(cutOffTime: LocalDateTime): List<MedicationLogs>


    @Query("SELECT * FROM medication_logs WHERE medication_id = :medicationId AND planned_datetime = :plannedDateTime")
    suspend fun getMedicationLogForTime(medicationId: Long, plannedDateTime: LocalDateTime): MedicationLogs?


    @Query("DELETE FROM medication_logs")
    suspend fun deleteAll()

    @Query("UPDATE medication_logs SET status = :newStatus WHERE id = :logId")
    suspend fun updateStatus(logId: Long, newStatus: MedicationStatus)
}