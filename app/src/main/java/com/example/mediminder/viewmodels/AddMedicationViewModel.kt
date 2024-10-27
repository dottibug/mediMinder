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
    private val reminderEnabled = MutableStateFlow(false)
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
    private val scheduleType = MutableStateFlow("") // todo: default to daily?
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

    fun saveMedication(medicationData: MedicationData, dosageData: DosageData, reminderData: ReminderData, scheduleData: ScheduleData) {
        // todo: validation logic and saving to database

        // Log for debugging
        Log.i("testcat", "Saving medication: $medicationData")
        Log.i("testcat", "Dosage data: $dosageData")
        Log.i("testcat", "Reminder data: $reminderData")
        Log.i("testcat", "Schedule data: $scheduleData")

        viewModelScope.launch {
            try {
                repository.addMedication(medicationData, dosageData, reminderData, scheduleData)
            } catch (e: Exception) {
                // todo: Handle error
            }
        }
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