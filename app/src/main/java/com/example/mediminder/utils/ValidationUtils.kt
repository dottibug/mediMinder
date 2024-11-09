package com.example.mediminder.utils

import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.models.ValidatedData
import java.time.LocalDate

object ValidationUtils {
    private const val MED_NAME_REQ = "Medication name is required"
    private const val DOSE_AMT_REQ = "Dosage amount is required"
    private const val DOSE_UNIT_DEFAULT = "units"
    private const val REM_FREQ_REQ = "Reminder frequency is required when reminders are enabled"
    private const val EMPTY_STRING = ""
    private const val EVERY_X_HOURS = "every x hours"
    private const val HOURLY_INTERVAL_REQ = "An interval is required for hourly reminders"
    private const val START_TIME_REQ = "A start time is required for hourly reminders"
    private const val END_TIME_REQ = "An end time is required for hourly reminders"
    private const val DAILY = "daily"
    private const val REM_TIME_REQ = "At least one reminder time is required for daily reminders"
    private const val DURATION_DEFAULT = "continuous"
    private const val NUM_DAYS = "numDays"
    private const val NUM_DAYS_REQ = "Number of days must be greater than 0"
    private const val SPECIFIC_DAYS = "specificDays"
    private const val SPECIFIC_DAYS_REQ = "Specific days of the week must be selected"
    private const val INTERVAL = "interval"
    private const val DAYS_INTERVAL_REQ = "Days interval must be greater than 0"

    fun getValidatedData(
        medicationData: MedicationData,
        dosageData: DosageData,
        reminderData: ReminderData,
        scheduleData: ScheduleData
    ): ValidatedData {
        val validatedMedicationData = validateMedicationData(medicationData)
        val validatedDosageData = validateDosageData(dosageData)
        val validatedReminderData = validateReminderData(reminderData)
        val validatedScheduleData = validateScheduleData(scheduleData)

        return ValidatedData(
            validatedMedicationData,
            validatedDosageData,
            validatedReminderData,
            validatedScheduleData
        )
    }

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

    // Validate dosage data before saving: dosage amount and units are not empty
    private fun validateDosageData(dosageData: DosageData): DosageData {
        return dosageData.copy(
            dosageAmount = dosageData.dosageAmount
                .trim().takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(DOSE_AMT_REQ),

            dosageUnits = dosageData.dosageUnits
                .trim().takeIf { it.isNotEmpty() }
                ?: DOSE_UNIT_DEFAULT,
        )
    }

    // Validate reminder data before saving
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

    private fun getReminderFrequency(reminderData: ReminderData): String {
        if (reminderData.reminderEnabled) {
            return reminderData.reminderFrequency.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(REM_FREQ_REQ)
        }
        else { return EMPTY_STRING }
    }

    private fun getHourlyReminderInterval(reminderData: ReminderData): String? {
        if (reminderData.reminderFrequency == EVERY_X_HOURS) {
            return reminderData.hourlyReminderInterval?.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(HOURLY_INTERVAL_REQ)
        }
        else { return null }
    }

    private fun getReminderStartTime (reminderData: ReminderData): Pair<Int, Int>? {
        if (reminderData.reminderFrequency == EVERY_X_HOURS) {
            return reminderData.hourlyReminderStartTime
                ?: throw IllegalArgumentException(START_TIME_REQ)
        }
        else { return null }
    }

    private fun getReminderEndTime (reminderData: ReminderData): Pair<Int, Int>? {
        if (reminderData.reminderFrequency == EVERY_X_HOURS) {
            return reminderData.hourlyReminderEndTime
                ?: throw IllegalArgumentException(END_TIME_REQ)
        }
        else { return null }
    }

    private fun getDailyReminderTimes(reminderData: ReminderData): List<Pair<Int, Int>> {
        if (reminderData.reminderFrequency == DAILY) {
            return reminderData.dailyReminderTimes.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(REM_TIME_REQ)
        } else { return emptyList() }
    }

   // Validate schedule data before saving
    private fun validateScheduleData(scheduleData: ScheduleData): ScheduleData {
        return scheduleData.copy(
            startDate = scheduleData.startDate ?: LocalDate.now(),
            durationType = getDurationType(scheduleData),
            numDays = getNumDays(scheduleData),
            scheduleType = getScheduleType(scheduleData),
            selectedDays = getSelectDays(scheduleData),
            daysInterval = getDaysInterval(scheduleData)
        )
    }

    private fun getDurationType(scheduleData: ScheduleData): String {
        if (scheduleData.durationType.isEmpty()) { return DURATION_DEFAULT }
        else { return scheduleData.durationType }
    }

    private fun getNumDays(scheduleData: ScheduleData): Int? {
        if (scheduleData.durationType == NUM_DAYS) {
            return scheduleData.numDays?.takeIf { it > 0 }
                ?: throw IllegalArgumentException(NUM_DAYS_REQ)
        }
        else { return null }
    }

    private fun getScheduleType(scheduleData: ScheduleData): String {
        if (scheduleData.scheduleType.isEmpty()) { return DAILY }
        else { return scheduleData.scheduleType }
    }

    private fun getSelectDays(scheduleData: ScheduleData): String {
        if (scheduleData.scheduleType == SPECIFIC_DAYS) {
            return scheduleData.selectedDays.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException(SPECIFIC_DAYS_REQ)
        }
        else { return EMPTY_STRING }
    }

    private fun getDaysInterval(scheduleData: ScheduleData): Int {
        if (scheduleData.scheduleType == INTERVAL) {
            return scheduleData.daysInterval?.takeIf { it > 0 }
                ?: throw IllegalArgumentException(DAYS_INTERVAL_REQ)
        }
        else { return 0 }
    }
}