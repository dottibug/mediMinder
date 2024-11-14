package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.mediminder.models.MedicationStatus
import java.time.LocalDateTime


// Represents a medication log row in the medication_logs table (associated with a medication and schedule)
@Entity(tableName = "medication_logs",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Schedules::class,
            parentColumns = ["id"],
            childColumns = ["schedule_id"],
            onDelete = ForeignKey.CASCADE
        )
    ])
data class MedicationLogs (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Long,
    @ColumnInfo(name = "schedule_id") val scheduleId: Long?,  // Nullable for as-needed medications
    @ColumnInfo(name = "planned_datetime") val plannedDatetime: LocalDateTime,
    @ColumnInfo(name = "taken_datetime") val takenDatetime: LocalDateTime?,
    @ColumnInfo(name = "status") val status: MedicationStatus,
    @ColumnInfo(name = "as_needed_dosage_amount") val asNeededDosageAmount: String?, // Nullable for scheduled medications
    @ColumnInfo(name = "as_needed_dosage_unit") val asNeededDosageUnit: String? // Nullable for scheduled medications
)