package com.example.mediminder.utils

import com.example.mediminder.data.local.classes.MedicationIcon
import com.example.mediminder.viewmodels.DosageData
import com.example.mediminder.viewmodels.MedicationData
import com.example.mediminder.viewmodels.ReminderData
import com.example.mediminder.viewmodels.ScheduleData
import java.time.LocalDate

object ValidationUtils {
    fun validateMedicationData(medicationData: MedicationData): MedicationData {
        return medicationData.copy(
            name = medicationData.name.trim().takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("Medication name is required"),
            doctor = medicationData.doctor.trim(),
            notes = medicationData.notes.trim(),
            icon = medicationData.icon ?: MedicationIcon.TABLET
        )
    }

    // Validate dosage data before saving: dosage amount and units are not empty
    fun validateDosageData(dosageData: DosageData): DosageData {
        return dosageData.copy(
            dosageAmount = dosageData.dosageAmount.trim().takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("Dosage amount is required"),

            // default units to "units" if not specified
            dosageUnits = dosageData.dosageUnits.trim().takeIf { it.isNotEmpty() }
                ?: "units",
        )
    }

    // Validate reminder data before saving
    fun validateReminderData(reminderData: ReminderData): ReminderData {
        return reminderData.copy(
            reminderEnabled = reminderData.reminderEnabled,
            // Reminder frequency is required when reminders are enabled
            reminderFrequency = if (reminderData.reminderEnabled) {
                reminderData.reminderFrequency.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("Reminder frequency is required when reminders are enabled")
            } else "",

            // Interval is required for hourly reminders
            hourlyReminderInterval = if (reminderData.reminderFrequency == "every x hours") {
                reminderData.hourlyReminderInterval?.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("Hourly reminder interval is required for hourly reminders")
            } else null,

            // Start time is required for hourly reminders
            hourlyReminderStartTime = if (reminderData.reminderFrequency == "every x hours") {
                reminderData.hourlyReminderStartTime
                    ?: throw IllegalArgumentException("Start time is required for hourly reminders")
            } else null,

            // End time is required for hourly reminders
            hourlyReminderEndTime = if (reminderData.reminderFrequency == "every x hours") {
                reminderData.hourlyReminderEndTime
                    ?: throw IllegalArgumentException("End time is required for hourly reminders")
            } else null,

            // At least one reminder time is required for daily reminders
            dailyReminderTimes = if (reminderData.reminderFrequency == "daily") {
                reminderData.dailyReminderTimes.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("At least one reminder time is required for daily reminders")
            } else emptyList(),
        )
    }

    // Validate schedule data before saving
    fun validateScheduleData(scheduleData: ScheduleData): ScheduleData {
        return scheduleData.copy(
            // Start date defaults to today if not specified
            startDate = scheduleData.startDate ?: LocalDate.now(),

            // Duration defaults to continuous unless specified otherwise
            durationType = if (scheduleData.durationType.isEmpty()) {
                "continuous"
            } else scheduleData.durationType,

            // Number of days is required for numDays duration type
            numDays = if (scheduleData.durationType == "numDays") {
                scheduleData.numDays?.takeIf { it > 0 }
                    ?: throw IllegalArgumentException("Number of days must be greater than 0")
            } else null,

            // Schedule type defaults to daily if not specified
            scheduleType = if (scheduleData.scheduleType.isEmpty()) {
                "daily"
            } else scheduleData.scheduleType,

            // Selected days are required for specificDays schedule
            selectedDays = if (scheduleData.scheduleType == "specificDays") {
                scheduleData.selectedDays.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("Specific days are required for this schedule")
            } else "",

            // Days interval is required for interval schedule
            daysInterval = if (scheduleData.scheduleType == "interval") {
                scheduleData.daysInterval?.takeIf { it > 0 }
                    ?: throw IllegalArgumentException("Days interval must be greater than 0")
            } else 0
        )
    }
}