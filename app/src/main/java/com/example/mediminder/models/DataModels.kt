package com.example.mediminder.models

import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.Schedules
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// DataModels contains data classes and enum classes used throughout the app

// Medication action enum
enum class MedicationAction {
    ADD,
    EDIT
}

enum class MedicationStatus {
    PENDING,        // Initial state when created
    TAKEN,          // User marked as taken
    SKIPPED,        // User skipped medication
    MISSED,         // Time passed without action
    UNSCHEDULED     // Unscheduled (as needed) medications
}

// Icons used for medication cards
enum class MedicationIcon {
    CAPSULE,
    DROP,
    INHALER,
    INJECTION,
    LIQUID,
    TABLET,
}

// Medication data
data class MedicationData(
    val name: String,
    val doctor: String,
    val notes: String,
    val icon: MedicationIcon?,
    val status: MedicationStatus,
    val asNeeded: Boolean = false
)

// Dosage data
data class DosageData(
    val dosageAmount: String?,
    val dosageUnits: String?
)

// Reminder data
data class ReminderData(
    val reminderEnabled: Boolean,
    val reminderFrequency: String,
    val hourlyReminderInterval: String?,
    val hourlyReminderStartTime: Pair<Int, Int>?,
    val hourlyReminderEndTime: Pair<Int, Int>?,
    val dailyReminderTimes: List<Pair<Int, Int>>,
)

// Schedule data
data class ScheduleData(
    val isScheduled: Boolean,
    val startDate: LocalDate?,
    val durationType: String,
    val numDays: Int?,
    val scheduleType: String,
    val selectedDays: String,
    val daysInterval: Int?
)

// Medication item: Medication, dosage, time, status, log id, and canUpdateStatus
data class MedicationItem(
    val medication: Medication,
    val dosage: Dosage?,
    val time: LocalTime,
    val status: MedicationStatus,
    val logId: Long,
    val canUpdateStatus: Boolean = true
)

// Medication with details: Medication, dosage, reminders, and schedule
data class MedicationWithDetails(
    val medication: Medication,
    val dosage: Dosage?,
    val reminders: MedReminders?,
    val schedule: Schedules?
)

// Medication history: List of medication logs
data class MedicationHistory(
    val logs: List<MedicationLogWithDetails>
)

// Medication log with details: Medication name, dosage, and log
data class MedicationLogWithDetails(
    val id: Long,
    val name: String,
    val dosageAmount: String?,
    val dosageUnits: String?,
    val log: MedicationLogs
)

// Day logs: Date and list of medication logs
data class DayLogs(
    val date: LocalDate,
    val logs: List<MedicationLogWithDetails>
)

// Validated medication, dosage, reminder, and schedule data
data class ValidatedData(
    val medicationData: MedicationData,
    val dosageData: DosageData?,
    val reminderData: ReminderData?,
    val scheduleData: ScheduleData?
)

// Validated as-needed medication data
data class ValidatedAsNeededData(
    val medicationId: Long,
    val scheduleId: Long?,
    val plannedDatetime: LocalDateTime,
    val takenDatetime: LocalDateTime,
    val status: MedicationStatus,
    val asNeededDosageAmount: String?,
    val asNeededDosageUnit: String?
)