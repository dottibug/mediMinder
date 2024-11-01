package com.example.mediminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.MedicationStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Abstraction layer for the medication_logs table in the database
// https://developer.android.com/training/data-storage/room/accessing-data

@Dao
interface MedicationLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicationLog: MedicationLogs): Long

    @Query("SELECT COUNT(*) FROM medication_logs")
    suspend fun getTotalLogsCount(): Int

    @Query("SELECT * FROM medication_logs WHERE status = 'PENDING' AND planned_datetime <= :cutOffTime")
    suspend fun getPendingMedicationLogs(cutOffTime: LocalDateTime): List<MedicationLogs>

    @Query("SELECT * FROM medication_logs WHERE medication_id = :medicationId AND planned_datetime = :plannedDateTime")
    suspend fun getMedicationLogForTime(medicationId: Long, plannedDateTime: LocalDateTime): MedicationLogs?

    @Query("DELETE FROM medication_logs")
    suspend fun deleteAll()

    @Query("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'medication_logs'")
    suspend fun resetSequence()

    @Query("UPDATE medication_logs SET status = :newStatus WHERE id = :logId")
    suspend fun updateStatus(logId: Long, newStatus: MedicationStatus)

    @Query("""
        SELECT COUNT(*) FROM medication_logs 
        WHERE medication_id = :medicationId 
        AND date(planned_datetime) >= date(:fromDate)
    """)
    suspend fun getFutureLogsCount(medicationId: Long, fromDate: LocalDate): Int

    @Query("""
    SELECT status FROM medication_logs 
    WHERE medication_id = :medicationId 
    AND date(planned_datetime) = date(:date)
    AND time(planned_datetime) = time(:time)
    LIMIT 1
    """)
    suspend fun getStatusForMedicationAndDateTime(
        medicationId: Long,
        date: LocalDate,
        time: LocalTime
    ): MedicationStatus?

    @Query("""
SELECT * FROM medication_logs 
WHERE planned_datetime >= :startOfDay 
AND planned_datetime < :endOfDay
""")
    suspend fun getLogsForDate(startOfDay: LocalDateTime, endOfDay: LocalDateTime): List<MedicationLogs>


    @Query("SELECT * FROM medication_logs WHERE medication_id = :medicationId AND planned_datetime = :plannedDateTime LIMIT 1")
    suspend fun getLogByMedicationIdAndPlannedTime(medicationId: Long, plannedDateTime: LocalDateTime): MedicationLogs?

    @Query("""
    SELECT * FROM medication_logs 
    WHERE medication_id = :medicationId 
    AND date(planned_datetime) BETWEEN date(:startDate) AND date(:endDate)
    """)
    suspend fun getLogsForMedicationInRange(
        medicationId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MedicationLogs>
}