package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.models.MedicationStatus
import java.time.LocalDate
import java.time.LocalDateTime

// Abstraction layer for the medication_logs table in the database
// https://developer.android.com/training/data-storage/room/accessing-data

@Dao
interface MedicationLogDao {
    // Insert a medication log entity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicationLog: MedicationLogs): Long

    // Update the status of a medication log
    @Query("UPDATE medication_logs SET status = :newStatus WHERE id = :logId")
    suspend fun updateStatus(logId: Long, newStatus: MedicationStatus)

    // Get total count of future logs for a medication
    @Query("""SELECT COUNT(*) FROM medication_logs WHERE medication_id = :medicationId 
        AND date(planned_datetime) >= date(:fromDate)""")
    suspend fun getFutureLogsCount(medicationId: Long, fromDate: LocalDate): Int

    // Get log by medication ID and scheduled time
    @Query("""SELECT * FROM medication_logs WHERE medication_id = :medicationId 
            AND planned_datetime = :plannedDateTime LIMIT 1""")
    suspend fun getLogByMedIdAndTime(medicationId: Long, plannedDateTime: LocalDateTime): MedicationLogs

    // Get pending medication logs
    @Query("SELECT * FROM medication_logs WHERE status = 'PENDING' AND planned_datetime <= :cutOffTime")
    suspend fun getPendingMedicationLogs(cutOffTime: LocalDateTime): List<MedicationLogs>

    // Get logs by schedule date
    @Query("""SELECT * FROM medication_logs WHERE planned_datetime >= :startOfDay 
        AND planned_datetime < :endOfDay""")
    suspend fun getLogsForDate(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<MedicationLogs>

    // Get logs by medication ID within a date range
    @Query("""SELECT * FROM medication_logs WHERE medication_id = :medicationId 
        AND planned_datetime >= :startDate AND planned_datetime < :endDate""")
    suspend fun getLogsForMedicationInRange(
        medicationId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime): List<MedicationLogs>

    // Get logs within a date range, sorted by date
    @Query("""SELECT * FROM medication_logs WHERE planned_datetime BETWEEN :startDate AND :endDate 
        ORDER BY planned_datetime ASC""")
    suspend fun getLogsInRange(startDate: LocalDateTime, endDate: LocalDateTime): List<MedicationLogs>

    // Delete log by log id
    @Query("DELETE FROM medication_logs WHERE id = :logId")
    suspend fun deleteById(logId: Long)

    // Delete all logs for a specific medication
    @Query("DELETE FROM medication_logs WHERE medication_id = :medicationId")
    suspend fun deleteAllLogsForMedication(medicationId: Long)

    // Delete all medication logs
    @Query("DELETE FROM medication_logs")
    suspend fun deleteAll()

    // Reset the sequence for the medication_logs table
    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'medication_logs'")
    suspend fun resetSequence()
}