package com.example.mediminder.models

import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EMPTY_STRING
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// DataModels contains data classes and enum classes used throughout the app

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

// Medication item: Medication, dosage, time, status, and log id
data class MedicationItem(
    val medication: Medication,
    val dosage: Dosage?,
    val time: LocalTime,
    val status: MedicationStatus,
    val logId: Long
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

// Reminder state holds values for the data flow of the BaseReminderFragment
data class ReminderState(
    val reminderEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val reminderFrequency: MutableStateFlow<String> = MutableStateFlow(EMPTY_STRING),
    val hourlyReminderInterval: MutableStateFlow<String?> = MutableStateFlow(null),
    val hourlyReminderStartTime: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null),
    val hourlyReminderEndTime: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null),
    val dailyReminderTimes: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(emptyList())
)

// Schedule state holds values for the data flow of the BaseScheduleFragment
data class ScheduleState(
    val isScheduledMedication: MutableStateFlow<Boolean> = MutableStateFlow(true),
    val startDate: MutableStateFlow<LocalDate?> = MutableStateFlow(null),
    val durationType: MutableStateFlow<String> = MutableStateFlow(CONTINUOUS),
    val numDays: MutableStateFlow<Int?> = MutableStateFlow(0),
    val scheduleType: MutableStateFlow<String> = MutableStateFlow(DAILY),
    val selectedDays: MutableStateFlow<String> = MutableStateFlow(EMPTY_STRING),
    val daysInterval: MutableStateFlow<Int?> = MutableStateFlow(0)
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