package com.example.mediminder.utils

import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.models.ValidatedAsNeededData
import com.example.mediminder.models.ValidatedData
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.EVERY_X_HOURS
import com.example.mediminder.utils.Constants.INTERVAL
import com.example.mediminder.utils.Constants.NUM_DAYS
import com.example.mediminder.utils.Constants.SPECIFIC_DAYS
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Validation utilities help validate medication, dose, reminder, and schedule data before saving
// or updating medications in the database
object ValidationUtils {

    // Main validation function
    fun validateMedicationData(
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?
    ): ValidatedData {
        val validMedInfo = validateMedInfo(medicationData)
        val isScheduled = !medicationData.asNeeded

        // Validate dosage, schedule, and reminders if the medication is scheduled
        if (isScheduled) {
            return ValidatedData(
                medicationData = validMedInfo,
                dosageData = validateDosageData(dosageData),
                reminderData = validateReminderData(reminderData),
                scheduleData = validateScheduleData(scheduleData)
            )
        } else {
            return ValidatedData(
                medicationData = validMedInfo,
                dosageData = null,
                reminderData = null,
                scheduleData = null
            )
        }
    }

    // Validate medication info:
    // - Medication name is required
    // - Defaults to tablet icon if icon is null
    private fun validateMedInfo(data: MedicationData): MedicationData {
        if (data.name.trim().isEmpty()) {
            throw IllegalArgumentException(MED_NAME_REQUIRED)
        }

        return data.copy(
            name = data.name.trim(),
            doctor = data.doctor.trim(),
            notes = data.notes.trim(),
            icon = data.icon ?: MedicationIcon.TABLET
        )
    }

    // Validate dosage data:
    // - Dosage amount is required
    // - Defaults to "units" if dosage units are null
    private fun validateDosageData(data: DosageData?): DosageData {
        requireNotNull(data) { DOSAGE_AMOUNT_REQUIRED }
        if (data.dosageAmount.isNullOrBlank()) {
            throw IllegalArgumentException(DOSAGE_AMOUNT_REQUIRED)
        }

        return data.copy(
            dosageAmount = data.dosageAmount.trim(),
            dosageUnits = data.dosageUnits ?: DOSE_UNIT_DEFAULT
        )
    }

    // Validate reminder data: Reminder frequency can not be null
    private fun validateReminderData(data: ReminderData?): ReminderData {
        requireNotNull(data) { REMINDER_REQUIRED }

        return when (data.reminderFrequency) {
            DAILY -> validateDailyReminders(data)
            EVERY_X_HOURS -> validateHourlyReminders(data)
            else -> throw IllegalArgumentException(INVALID_REMINDER_FREQUENCY)
        }
    }

    // Validate schedule data
    private fun validateScheduleData(data: ScheduleData?): ScheduleData {
        requireNotNull(data) { SCHEDULE_REQUIRED }
        val validDurationData = validateDuration(data)
        val validData = validateScheduleType(validDurationData)
        return validData
    }

    // Helper function to validate schedule type
    private fun validateScheduleType(data: ScheduleData): ScheduleData {
        return when (data.scheduleType) {
            DAILY -> data.copy(selectedDays = EMPTY_STRING, daysInterval = 0)
            SPECIFIC_DAYS -> validateSpecificDays(data)
            INTERVAL -> validateDaysInterval(data)
            else -> throw IllegalArgumentException(INVALID_SCHEDULE_TYPE)
        }
    }

    // Helper function to validate schedule duration
    private fun validateDuration(data: ScheduleData): ScheduleData {
        return when (data.durationType) {
            CONTINUOUS -> data.copy(numDays = null)
            NUM_DAYS -> validateNumDays(data)
            else -> throw IllegalArgumentException(INVALID_DURATION_TYPE)
        }
    }

    // Helper function to validate number of days
    private fun validateNumDays(data: ScheduleData): ScheduleData {
        requireNotNull(data.numDays) { NUM_DAYS_REQUIRED }
        if (data.numDays <= 0) { throw IllegalArgumentException(NUM_DAYS_INVALID) }
        return data.copy()
    }

    // Helper function to validate specific days schedule
    private fun validateSpecificDays(data: ScheduleData): ScheduleData {
        if (data.selectedDays.isEmpty()) {
            throw IllegalArgumentException(SCHEDULE_DAYS_REQUIRED)
        }
        return data.copy(daysInterval = 0)
    }

    // Helper function to validate days interval schedule
    private fun validateDaysInterval(data: ScheduleData): ScheduleData {
        val interval = data.daysInterval ?: 0
        require(interval > 0) { DAY_INTERVAL_REQ }
        return data.copy(selectedDays = EMPTY_STRING)
    }

    // Helper function to validate daily reminders
    private fun validateDailyReminders(data: ReminderData): ReminderData {
        if (data.dailyReminderTimes.isEmpty()) {
            throw IllegalArgumentException(REMINDER_TIME_REQ)
        }
        return data
    }

    // Helper function to validate hourly reminders
    private fun validateHourlyReminders(data: ReminderData): ReminderData {
        requireNotNull(data.hourlyReminderInterval) { HOURLY_INTERVAL_REQ }
        requireNotNull(data.hourlyReminderStartTime) { START_TIME_REQ }
        requireNotNull(data.hourlyReminderEndTime) { END_TIME_REQ }
        return data
    }

    // Constants
    private const val MED_NAME_REQUIRED = "Medication name is required"
    private const val DOSAGE_AMOUNT_REQUIRED = "Dosage amount is required"
    private const val DOSE_UNIT_DEFAULT = "units"
    private const val REMINDER_REQUIRED = "Reminders are required for scheduled medications"
    private const val INVALID_REMINDER_FREQUENCY = "Invalid reminder frequency"
    private const val REMINDER_TIME_REQ = "At least one daily reminder time is required"
    private const val HOURLY_INTERVAL_REQ = "An hourly interval is required"
    private const val START_TIME_REQ = "A start time is required"
    private const val END_TIME_REQ = "An end time is required"
    private const val SCHEDULE_REQUIRED = "A schedule is required for scheduled medications"
    private const val INVALID_SCHEDULE_TYPE = "Invalid schedule type"
    private const val DAY_INTERVAL_REQ = "Day interval must be greater than 0"
    private const val SCHEDULE_DAYS_REQUIRED = "Please select which days to take the medication"
    private const val NUM_DAYS_REQUIRED = "Number of days is required"
    private const val NUM_DAYS_INVALID = "Number of days must be greater than 0"
    private const val INVALID_DURATION_TYPE = "Invalid duration type"

    //////////////////////////

    // Validate data for an as-needed medication
    fun getValidatedAsNeededData(
        selectedMedId: Long?,
        dosageAmount: String,
        dosageUnits: String,
        dateTaken: LocalDate?,
        timeTaken: Pair<Int, Int>?
    ): ValidatedAsNeededData {
        val validatedMedId =
            selectedMedId ?: throw IllegalArgumentException("Medication is required")
        val validatedDosageAmount =
            dosageAmount.trim().takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException(
                DOSAGE_AMOUNT_REQUIRED
            )
        val validatedDosageUnits =
            dosageUnits.trim().takeIf { it.isNotEmpty() } ?: DOSE_UNIT_DEFAULT
        val validatedDateTaken =
            dateTaken ?: throw IllegalArgumentException("Date taken is required")
        val validatedTimeTaken = timeTaken?.let { LocalTime.of(it.first, it.second) }

        return ValidatedAsNeededData(
            medicationId = validatedMedId,
            scheduleId = null,
            plannedDatetime = LocalDateTime.of(validatedDateTaken, validatedTimeTaken),
            takenDatetime = LocalDateTime.of(validatedDateTaken, validatedTimeTaken),
            status = com.example.mediminder.models.MedicationStatus.TAKEN,
            asNeededDosageAmount = validatedDosageAmount,
            asNeededDosageUnit = validatedDosageUnits
        )
    }

}