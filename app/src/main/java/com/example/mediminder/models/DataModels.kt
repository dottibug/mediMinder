package com.example.mediminder.models

import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationLogs
import com.example.mediminder.data.local.classes.Schedules
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class MedicationStatus {
    PENDING,        // Initial state when created
    TAKEN,          // User marked as taken
    SKIPPED,        // User skipped medication
    MISSED,         // Time passed without action
    UNSCHEDULED     // Unscheduled (as needed) medications
}

enum class MedicationIcon {
    CAPSULE,
    DROP,
    INHALER,
    INJECTION,
    LIQUID,
    TABLET,
}

data class MedicationData(
    val name: String,
    val doctor: String,
    val notes: String,
    val icon: MedicationIcon?,
    val status: MedicationStatus,
    val asNeeded: Boolean = false
)

data class DosageData(
    val dosageAmount: String?,
    val dosageUnits: String?
)

data class ReminderData(
    val reminderEnabled: Boolean,
    val reminderFrequency: String,
    val hourlyReminderInterval: String?,
    val hourlyReminderStartTime: Pair<Int, Int>?,
    val hourlyReminderEndTime: Pair<Int, Int>?,
    val dailyReminderTimes: List<Pair<Int, Int>>,
)

data class ScheduleData(
    val isScheduled: Boolean,
    val startDate: LocalDate?,
    val durationType: String,
    val numDays: Int?,
    val scheduleType: String,
    val selectedDays: String,
    val daysInterval: Int?
)

data class MedicationItem(
    val medication: Medication,
    val dosage: Dosage?,
    val time: LocalTime,
    val status: MedicationStatus,
    val logId: Long
)

data class MedicationWithDetails(
    val medication: Medication,
    val dosage: Dosage?,
    val reminders: MedReminders?,
    val schedule: Schedules?
)

data class MedicationHistory(
    val logs: List<MedicationLogWithDetails>
)

data class MedicationLogWithDetails(
    val id: Long,
    val name: String,
    val dosageAmount: String?,
    val dosageUnits: String?,
    val log: MedicationLogs
)

data class DayLogs(
    val date: LocalDate,
    val logs: List<MedicationLogWithDetails>
)

data class ReminderState(
    val reminderEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val reminderFrequency: MutableStateFlow<String> = MutableStateFlow(""),
    val hourlyReminderInterval: MutableStateFlow<String?> = MutableStateFlow(null),
    val hourlyReminderStartTime: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null),
    val hourlyReminderEndTime: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null),
    val dailyReminderTimes: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(emptyList())
)

data class ScheduleState(
    val isScheduledMedication: MutableStateFlow<Boolean> = MutableStateFlow(true),
    val startDate: MutableStateFlow<LocalDate?> = MutableStateFlow(null),
    val durationType: MutableStateFlow<String> = MutableStateFlow("continuous"),
    val numDays: MutableStateFlow<Int?> = MutableStateFlow(0),
    val scheduleType: MutableStateFlow<String> = MutableStateFlow("daily"),
    val selectedDays: MutableStateFlow<String> = MutableStateFlow(""),
    val daysInterval: MutableStateFlow<Int?> = MutableStateFlow(0)
)

data class ValidatedData(
    val medicationData: MedicationData,
    val dosageData: DosageData?,
    val reminderData: ReminderData?,
    val scheduleData: ScheduleData?
)

data class ValidatedAsNeededData(
    val medicationId: Long,
    val scheduleId: Long?,
    val plannedDatetime: LocalDateTime,
    val takenDatetime: LocalDateTime,
    val status: MedicationStatus,
    val asNeededDosageAmount: String?,
    val asNeededDosageUnit: String?
)