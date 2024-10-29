package com.example.mediminder.data.local.classes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = Medication::class,
        parentColumns = ["id"],
        childColumns = ["medication_id"],
        onDelete = ForeignKey.CASCADE
    )])
data class MedReminders (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "medication_id") val medicationId: Long,

    @ColumnInfo(name = "reminder_frequency") val reminderFrequency: String,
    @ColumnInfo(name = "hourly_reminder_interval") val hourlyReminderInterval: String?,
    @ColumnInfo(name = "hourly_reminder_start_time") val hourlyReminderStartTime: LocalTime?,
    @ColumnInfo(name = "hourly_reminder_end_time") val hourlyReminderEndTime: LocalTime?,
    @ColumnInfo(name = "daily_reminder_times") val dailyReminderTimes: List<LocalTime>,
)