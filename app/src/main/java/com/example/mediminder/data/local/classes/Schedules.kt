package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

// Represents a schedule row in the schedules table (associated with a medication)
@Entity(tableName = "schedules",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medication_id"],
        onDelete = ForeignKey.CASCADE
    )])
data class Schedules (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Long,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "duration_type") val durationType: String,
    @ColumnInfo(name = "num_days") val numDays: Int?,
    @ColumnInfo(name = "schedule_type") val scheduleType: String,
    @ColumnInfo(name = "selected_days") val selectedDays: String,
    @ColumnInfo(name = "days_interval") val daysInterval: Int?
)
