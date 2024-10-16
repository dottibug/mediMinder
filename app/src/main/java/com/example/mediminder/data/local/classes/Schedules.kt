package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

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
    @ColumnInfo(name = "frequency_type") val frequencyType: String,
    @ColumnInfo(name = "frequency_amount") val frequencyAmount: Int?,
    @ColumnInfo(name = "start_date") val startDate: LocalDate, // Stored as epoch day (days since epoch)
    @ColumnInfo(name = "end_date") val endDate: LocalDate?, // Stored as epoch day (days since epoch)
    @ColumnInfo(name = "days_of_week") val daysOfWeek: String?, // Use comma-separated string of numbers (0-6) to represent days of the week
    @ColumnInfo(name = "time_of_day") val timeOfDay: LocalTime?,
    @ColumnInfo(name = "days_of_month") val daysOfMonth: String? // Use comma-separated string of numbers (1-31) to represent days of the month
)