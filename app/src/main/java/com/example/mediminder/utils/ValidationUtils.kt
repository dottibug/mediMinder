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
    private const val MED_NAME_REQ = "Medication name is required"
    private const val DOSE_AMT_REQ = "Dosage amount is required"
    private const val DOSE_UNIT_DEFAULT = "units"
    private const val REM_FREQ_REQ = "Reminder frequency is required when reminders are enabled"
    private const val HOURLY_INTERVAL_REQ = "An interval is required for hourly reminders"
    private const val START_TIME_REQ = "A start time is required for hourly reminders"
    private const val END_TIME_REQ = "An end time is required for hourly reminders"
    private const val REM_TIME_REQ = "At least one reminder time is required for daily reminders"
    private const val NUM_DAYS_REQ = "Number of days must be greater than 0"
    private const val SPECIFIC_DAYS_REQ = "Specific days of the week must be selected"
    private const val DAYS_INTERVAL_REQ = "Days interval must be greater than 0"

    // Validate data for a scheduled medication
    fun getValidatedData(
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?
    ): ValidatedData {
        val isScheduled = !medicationData.asNeeded

        return ValidatedData(
            medicationData = validateMedicationData(medicationData),
            dosageData = if (isScheduled) validateDosageData(dosageData) else null,
            reminderData = if (isScheduled) reminderData?.let { validateReminderData(it) } else null,
            scheduleData = if (isScheduled) scheduleData?.let { validateScheduleData(it) } else null
        )
    }

    // Validate data for an as-needed medication
    fun getValidatedAsNeededData(
        selectedMedId: Long?,
        dosageAmount: String,
        dosageUnits: String,
        dateTaken: LocalDate?,
        timeTaken: Pair<Int, Int>?
    ): ValidatedAsNeededData {
        val validatedMedId = selectedMedId ?: throw IllegalArgumentException("Medication is required")
        val validatedDosageAmount = dosageAmount.trim().takeIf { it.isNotEmpty() } ?: throw IllegalArgumentException(DOSE_AMT_REQ)
        val validatedDosageUnits = dosageUnits.trim().takeIf { it.isNotEmpty() } ?: DOSE_UNIT_DEFAULT
        val validatedDateTaken = dateTaken ?: throw IllegalArgumentException("Date taken is required")
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

    // Validate medication data:
    // - Name cannot be empty
    // - Doctor and notes are trimmed
    // - Icon defaults to tablet if null
    private fun validateMedicationData(medicationData: MedicationData): MedicationData {
        return medicationData.copy(
            name = medicationData.name
                .trim().takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(MED_NAME_REQ),
            doctor = medicationData.doctor.trim(),
            notes = medicationData.notes.trim(),
            icon = medicationData.icon ?: MedicationIcon.TABLET
        )
    }

    // Validate dosage data:
    // - Amount cannot be empty
    // - Units defaults to "units" if empty
    private fun validateDosageData(dosageData: DosageData?): DosageData? {
        if (dosageData != null) {
            return dosageData.copy(
                dosageAmount = dosageData.dosageAmount?.trim()?.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException(DOSE_AMT_REQ),

                dosageUnits = dosageData.dosageUnits?.trim()?.takeIf { it.isNotEmpty() }
                    ?: DOSE_UNIT_DEFAULT
            )
        }
        else return null
    }

    // Validate reminder data:
    // - Reminder frequency is required when reminders are enabled
    // - Hourly reminder interval is required for hourly reminders
    // - Hourly reminder start and end time are required for hourly reminders
    // - Daily reminder times are required for daily reminders
    private fun validateReminderData(reminderData: ReminderData): ReminderData {
        return reminderData.copy(
            reminderEnabled = reminderData.reminderEnabled,
            reminderFrequency = getReminderFrequency(reminderData),
            hourlyReminderInterval = getHourlyReminderInterval(reminderData),
            hourlyReminderStartTime = getReminderStartTime(reminderData),
            hourlyReminderEndTime = getReminderEndTime(reminderData),
            dailyReminderTimes = getDailyReminderTimes(reminderData),
        )
    }

    // Helper function to validate reminder frequency
    private fun getReminderFrequency(reminderData: ReminderData): String {
        if (reminderData.reminderEnabled) {
            return reminderData.reminderFrequency.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(REM_FREQ_REQ)
        }
        else { return EMPTY_STRING }
    }

    // Helper function to validate hourly reminder interval
    private fun getHourlyReminderInterval(reminderData: ReminderData): String? {
        if (reminderData.reminderFrequency == EVERY_X_HOURS) {
            return reminderData.hourlyReminderInterval?.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(HOURLY_INTERVAL_REQ)
        }
        else { return null }
    }

    // Helper function to validate reminder start time
    private fun getReminderStartTime (reminderData: ReminderData): Pair<Int, Int>? {
        if (reminderData.reminderFrequency == EVERY_X_HOURS) {
            return reminderData.hourlyReminderStartTime
                ?: throw IllegalArgumentException(START_TIME_REQ)
        }
        else { return null }
    }

    // Helper function to validate reminder end time
    private fun getReminderEndTime (reminderData: ReminderData): Pair<Int, Int>? {
        if (reminderData.reminderFrequency == EVERY_X_HOURS) {
            return reminderData.hourlyReminderEndTime
                ?: throw IllegalArgumentException(END_TIME_REQ)
        }
        else { return null }
    }

    // Helper function to validate daily reminder times
    private fun getDailyReminderTimes(reminderData: ReminderData): List<Pair<Int, Int>> {
        if (reminderData.reminderFrequency == DAILY) {
            return reminderData.dailyReminderTimes.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(REM_TIME_REQ)
        } else { return emptyList() }
    }

   // Validate schedule data:
    // - Duration type defaults to "continuous" if empty
    // - Start date defaults to today if null
    // - Number of days is required for numDays type
    // - Schedule type defaults to "daily" if empty
    // - Selected days are required for specificDays type
    // - Days interval is required for interval type
    private fun validateScheduleData(scheduleData: ScheduleData): ScheduleData {
        return scheduleData.copy(
            isScheduled = scheduleData.isScheduled,
            startDate = scheduleData.startDate ?: LocalDate.now(),
            durationType = getDurationType(scheduleData),
            numDays = getNumDays(scheduleData),
            scheduleType = getScheduleType(scheduleData),
            selectedDays = getSelectDays(scheduleData),
            daysInterval = getDaysInterval(scheduleData)
        )
    }

    // Helper function to validate duration type
    private fun getDurationType(scheduleData: ScheduleData): String {
        if (scheduleData.durationType.isEmpty()) { return CONTINUOUS }
        else { return scheduleData.durationType }
    }

    // Helper function to validate number of days
    private fun getNumDays(scheduleData: ScheduleData): Int? {
        if (scheduleData.durationType == NUM_DAYS) {
            return scheduleData.numDays?.takeIf { it > 0 }
                ?: throw IllegalArgumentException(NUM_DAYS_REQ)
        }
        else { return null }
    }

    // Helper function to validate schedule type
    private fun getScheduleType(scheduleData: ScheduleData): String {
        if (scheduleData.scheduleType.isEmpty()) { return DAILY }
        else { return scheduleData.scheduleType }
    }

    // Helper function to validate selected days
    private fun getSelectDays(scheduleData: ScheduleData): String {
        if (scheduleData.scheduleType == SPECIFIC_DAYS) {
            return scheduleData.selectedDays.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(SPECIFIC_DAYS_REQ)
        }
        else { return EMPTY_STRING }
    }

    // Helper function to validate days interval
    private fun getDaysInterval(scheduleData: ScheduleData): Int {
        if (scheduleData.scheduleType == INTERVAL) {
            return scheduleData.daysInterval?.takeIf { it > 0 }
                ?: throw IllegalArgumentException(DAYS_INTERVAL_REQ)
        }
        else { return 0 }
    }
}