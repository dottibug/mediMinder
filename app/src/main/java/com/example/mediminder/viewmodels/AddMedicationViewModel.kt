package com.example.mediminder.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate


// TODO: Handle default values/error messages for no input, no date selected, etc
// TODO: Handle if x times daily has reminder tiimes that are the same; just use one if the time is exactly the same

class AddMedicationViewModel(private val repository: MedicationRepository): ViewModel() {

    // Reminder
//    private val reminderEnabled = MutableStateFlow(false)

    private val reminderEnabled = MutableStateFlow(false)
    init {
        viewModelScope.launch {
            reminderEnabled.collect { value ->
                Log.d("testcat", "reminderEnabled value changed to: $value")
            }
        }
    }


    private val reminderFrequency = MutableStateFlow("")
    private val hourlyReminderInterval = MutableStateFlow<String?>(null)
    private val hourlyReminderStartTime = MutableStateFlow<Pair<Int, Int>?>(null)
    private val dailyReminderTimes = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    private val reminderType = MutableStateFlow("")

    fun updateIsReminderEnabled(enabled: Boolean) { reminderEnabled.value = enabled }
    fun updateReminderFrequency(frequency: String?) { reminderFrequency.value = frequency ?: "" }
    fun updateHourlyReminderInterval(interval: String?) { hourlyReminderInterval.value = interval }
    fun updateHourlyReminderStartTime(startTime: Pair<Int, Int>?) { hourlyReminderStartTime.value = startTime }
    fun updateDailyReminderTimes(times: List<Pair<Int, Int>>) { dailyReminderTimes.value = times }
    fun updateReminderType(type: String) { reminderType.value = type }

    fun getReminderData(): ReminderData {
        return ReminderData(
            reminderEnabled = reminderEnabled.value,
            reminderFrequency = reminderFrequency.value,
            hourlyReminderInterval = hourlyReminderInterval.value,
            hourlyReminderStartTime = hourlyReminderStartTime.value,
            dailyReminderTimes = dailyReminderTimes.value,
            reminderType = reminderType.value
        )
    }

    // Schedule
    private val startDate = MutableStateFlow<LocalDate?>(null)
    private val durationType = MutableStateFlow("continuous")
    private val numDays = MutableStateFlow<Int?>(0)
    private val scheduleType = MutableStateFlow("daily")
    private val selectedDays = MutableStateFlow("")
    private val daysInterval = MutableStateFlow<Int?>(0)

    fun updateStartDate(date: LocalDate?) { startDate.value = date }
    fun updateDurationType(type: String) { durationType.value = type }
    fun updateNumDays(days: Int?) { numDays.value = days }
    fun updateScheduleType(type: String) { scheduleType.value = type }

    fun updateSelectedDays(days: String) {
        selectedDays.value = days
        daysInterval.value = 0
    }

    fun updateDaysInterval(interval: Int?) {
        daysInterval.value = interval
        selectedDays.value = ""
    }

    fun getScheduleData(): ScheduleData {
        return ScheduleData(
            startDate = startDate.value,
            durationType = durationType.value,
            numDays = numDays.value,
            scheduleType = scheduleType.value,
            selectedDays = selectedDays.value,
            daysInterval = daysInterval.value
        )
    }

    // Save medication data to database. Validates data before saving.
    fun saveMedication(medicationData: MedicationData, dosageData: DosageData, reminderData: ReminderData, scheduleData: ScheduleData) {
        val validatedMedicationData = validateMedicationData(medicationData)
        val validatedDosageData = validateDosageData(dosageData)
        val validatedReminderData = validateReminderData(reminderData)
        val validatedScheduleData = validateScheduleData(scheduleData)

        viewModelScope.launch {
            try {
                repository.addMedication(
                    validatedMedicationData,
                    validatedDosageData,
                    validatedReminderData,
                    validatedScheduleData)
            } catch (e: Exception) {
                // todo: Handle error (maybe toast the error message? if it's an IllegalArgumentException)
            }
        }
    }

    // Validate medication data before saving:
    // name is not empty
    // doctor and notes are trimmed
    private fun validateMedicationData(medicationData: MedicationData): MedicationData {
        return medicationData.copy(
            name = medicationData.name.trim().takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("Medication name is required"),
            doctor = medicationData.doctor.trim(),
            notes = medicationData.notes.trim()
        )
    }

    // Validate dosage data before saving: dosage amount and units are not empty
    private fun validateDosageData(dosageData: DosageData): DosageData {
        return dosageData.copy(
            dosageAmount = dosageData.dosageAmount.trim().takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("Dosage amount is required"),
            dosageUnits = dosageData.dosageUnits.trim().takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("Dosage units are required")
        )
    }

    // Validate reminder data before saving
    private fun validateReminderData(reminderData: ReminderData): ReminderData {
        return reminderData.copy(
            reminderEnabled = reminderData.reminderEnabled,
            // Reminder frequency is required when reminders are enabled
            reminderFrequency = if (reminderData.reminderEnabled) {
                reminderData.reminderFrequency.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("Reminder frequency is required when reminders are enabled")
            } else "",

            // Interval is required for hourly reminders
            hourlyReminderInterval = if (reminderData.reminderFrequency == "hourly") {
                reminderData.hourlyReminderInterval?.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("Hourly reminder interval is required for hourly reminders")
            } else null,

            // Start time is required for hourly reminders
            hourlyReminderStartTime = if (reminderData.reminderFrequency == "hourly") {
                reminderData.hourlyReminderStartTime
                    ?: throw IllegalArgumentException("Start time is required for hourly reminders")
            } else null,

            // At least one reminder time is required for daily reminders
            dailyReminderTimes = if (reminderData.reminderFrequency == "daily") {
                reminderData.dailyReminderTimes.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("At least one reminder time is required for daily reminders")
            } else emptyList(),

            // Reminder type is required when reminders are enabled
            reminderType = if (reminderData.reminderEnabled) {
                reminderData.reminderType.takeIf { it.isNotEmpty() }
                    ?: throw IllegalArgumentException("Reminder type is required when reminders are enabled")
            } else ""
        )
    }

    // Validate schedule data before saving
    private fun validateScheduleData(scheduleData: ScheduleData): ScheduleData {
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                val database = AppDatabase.getDatabase(application)
                val medicationRepository = MedicationRepository(
                    database.medicationDao(),
                    database.dosageDao(),
                    database.remindersDao(),
                    database.scheduleDao())
                AddMedicationViewModel(medicationRepository)
            }
        }
    }
}

data class MedicationData(
    val name: String,
    val doctor: String,
    val notes: String
)

data class DosageData(
    val dosageAmount: String,
    val dosageUnits: String
)

data class ReminderData(
    val reminderEnabled: Boolean,
    val reminderFrequency: String,
    val hourlyReminderInterval: String?,
    val hourlyReminderStartTime: Pair<Int, Int>?,
    val dailyReminderTimes: List<Pair<Int, Int>>,
    val reminderType: String
)

data class ScheduleData(
    val startDate: LocalDate?,
    val durationType: String,
    val numDays: Int?,
    val scheduleType: String,
    val selectedDays: String,
    val daysInterval: Int?
)