package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import java.time.LocalDate

// Represents a schedule row in the schedules table (associated with a medication)
@Entity(tableName = "schedules",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medication_id"],
        onDelete = ForeignKey.CASCADE
    )])
// selectedDays format: comma-separated list of days (1-7, Monday-Sunday) to match Kotlin LocalDate.dayOfWeek
// Ex: "1,3,5" represents Monday, Wednesday, Friday
data class Schedules (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Long,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "duration_type", defaultValue = CONTINUOUS) val durationType: String,
    @ColumnInfo(name = "num_days") val numDays: Int? =  null,
    @ColumnInfo(name = "schedule_type", defaultValue = DAILY) val scheduleType: String,
    @ColumnInfo(name = "selected_days", defaultValue = "") val selectedDays: String,
    @ColumnInfo(name = "days_interval") val daysInterval: Int? = null
)
