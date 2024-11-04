package com.example.mediminder.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.MedicationIcon
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.data.repositories.MedicationWithDetails
import com.example.mediminder.receivers.MedicationSchedulerReceiver
import com.example.mediminder.utils.ValidationUtils
import com.example.mediminder.workers.CheckMissedMedicationsWorker
import com.example.mediminder.workers.CreateFutureMedicationLogsWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class BaseMedicationViewModel(
    protected val repository: MedicationRepository,
    protected val applicationContext: Context
) : ViewModel() {
    // Reminder State
    private val reminderEnabled = MutableStateFlow(false)
    private val reminderFrequency = MutableStateFlow("")
    private val hourlyReminderInterval = MutableStateFlow<String?>(null)
    private val hourlyReminderStartTime = MutableStateFlow<Pair<Int, Int>?>(null)
    private val hourlyReminderEndTime = MutableStateFlow<Pair<Int, Int>?>(null)
    private val dailyReminderTimes = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())

    // Schedule State
    private val startDate = MutableStateFlow<LocalDate?>(null)
    private val durationType = MutableStateFlow("continuous")
    private val numDays = MutableStateFlow<Int?>(0)
    private val scheduleType = MutableStateFlow("daily")
    private val selectedDays = MutableStateFlow("")
    private val daysInterval = MutableStateFlow<Int?>(0)

    // Current medication (for editing medications)
    private val _currentMedication = MutableStateFlow<MedicationWithDetails?>(null)
    val currentMedication: StateFlow<MedicationWithDetails?> = _currentMedication.asStateFlow()

    // Update Functions
    fun updateIsReminderEnabled(enabled: Boolean) { reminderEnabled.value = enabled }
    fun updateReminderFrequency(frequency: String?) { reminderFrequency.value = frequency ?: "" }
    fun updateHourlyReminderInterval(interval: String?) { hourlyReminderInterval.value = interval }
    fun updateHourlyReminderStartTime(startTime: Pair<Int, Int>?) { hourlyReminderStartTime.value = startTime }
    fun updateHourlyReminderEndTime(endTime: Pair<Int, Int>?) { hourlyReminderEndTime.value = endTime }
    fun updateDailyReminderTimes(times: List<Pair<Int, Int>>) { dailyReminderTimes.value = times }

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

    // Getter Functions
    fun getReminderData(): ReminderData {
        return ReminderData(
            reminderEnabled = reminderEnabled.value,
            reminderFrequency = reminderFrequency.value,
            hourlyReminderInterval = hourlyReminderInterval.value,
            hourlyReminderStartTime = hourlyReminderStartTime.value,
            hourlyReminderEndTime = hourlyReminderEndTime.value,
            dailyReminderTimes = dailyReminderTimes.value,
        )
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

    // Coroutine off the main thread to avoid blocking the UI
    // Save medication data to database. Validates data before saving.
    fun addMedication(
        medicationData: MedicationData,
        dosageData: DosageData,
        reminderData: ReminderData,
        scheduleData: ScheduleData
    ) {
        val validatedMedicationData = ValidationUtils.validateMedicationData(medicationData)
        val validatedDosageData = ValidationUtils.validateDosageData(dosageData)
        val validatedReminderData = ValidationUtils.validateReminderData(reminderData)
        val validatedScheduleData = ValidationUtils.validateScheduleData(scheduleData)

        viewModelScope.launch {
            try {
                // Save to database
                val medicationId = repository.addMedication(
                    validatedMedicationData,
                    validatedDosageData,
                    validatedReminderData,
                    validatedScheduleData
                )

                // Create future logs
                val workManager = WorkManager.getInstance(applicationContext)
                val createFutureLogsRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>()
                    .build()
                val checkMissedRequest = OneTimeWorkRequestBuilder<CheckMissedMedicationsWorker>()
                    .build()

                workManager.beginUniqueWork(
                    "create_logs_${medicationId}_${System.currentTimeMillis()}",
                    ExistingWorkPolicy.REPLACE,
                    createFutureLogsRequest
                )
                    .then(checkMissedRequest)
                    .enqueue()


                // Send broadcast to update UI
                // Observe work completion before sending broadcasts
                workManager.getWorkInfoByIdLiveData(createFutureLogsRequest.id)
                    .observeForever { workInfo ->
                        if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                            // Send broadcast to update UI
                            val updateIntent = Intent("com.example.mediminder.MEDICATION_STATUS_CHANGED").apply {
                                setPackage(applicationContext.packageName)
                            }
                            applicationContext.sendBroadcast(updateIntent)

                            // Schedule notifications
                            val schedulerIntent = Intent(applicationContext, MedicationSchedulerReceiver::class.java).apply {
                                action = "com.example.mediminder.SCHEDULE_NEW_MEDICATION"
                            }
                            applicationContext.sendBroadcast(schedulerIntent)
                        }
                    }
            } catch (e: Exception) {
                Log.e("testcat", "Error saving medication: ${e.message}")
                throw e
            }
        }
    }

    fun updateMedication(
        medicationId: Long,
        medicationData: MedicationData,
        dosageData: DosageData,
        reminderData: ReminderData,
        scheduleData: ScheduleData
    ) {
        val validatedMedicationData = ValidationUtils.validateMedicationData(medicationData)
        val validatedDosageData = ValidationUtils.validateDosageData(dosageData)
        val validatedReminderData = ValidationUtils.validateReminderData(reminderData)
        val validatedScheduleData = ValidationUtils.validateScheduleData(scheduleData)

        viewModelScope.launch {
            try {
                Log.d("testcat", "Starting medication update for id: $medicationId")
                Log.d("testcat", "Validated dosage data: $validatedDosageData")

                // Update in database
                repository.updateMedication(
                    medicationId,
                    validatedMedicationData,
                    validatedDosageData,
                    validatedReminderData,
                    validatedScheduleData
                )

                Log.d("testcat", "Successfully updated medication")

                // Create future logs
                val workManager = WorkManager.getInstance(applicationContext)
                val createFutureLogsRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>()
                    .build()
                val checkMissedRequest = OneTimeWorkRequestBuilder<CheckMissedMedicationsWorker>()
                    .build()

                workManager.beginUniqueWork(
                    "update_logs_${medicationId}_${System.currentTimeMillis()}",
                    ExistingWorkPolicy.REPLACE,
                    createFutureLogsRequest
                )
                    .then(checkMissedRequest)
                    .enqueue()

                // Observe work completion before sending broadcasts
                workManager.getWorkInfoByIdLiveData(createFutureLogsRequest.id)
                    .observeForever { workInfo ->
                        if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                            // Send broadcast to update UI
                            val updateIntent = Intent("com.example.mediminder.MEDICATION_STATUS_CHANGED").apply {
                                setPackage(applicationContext.packageName)
                            }
                            applicationContext.sendBroadcast(updateIntent)

                            // Reschedule notifications
                            val schedulerIntent = Intent(applicationContext, MedicationSchedulerReceiver::class.java).apply {
                                action = "com.example.mediminder.SCHEDULE_NEW_MEDICATION"
                            }
                            applicationContext.sendBroadcast(schedulerIntent)
                        }
                    }
            } catch (e: Exception) {
                Log.e("testcat", "Error updating medication: ${e.message}")
                throw e
            }
        }
    }

    fun fetchMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                _currentMedication.value = repository.getMedicationDetailsById(medicationId)
            } catch (e: Exception) {
                Log.e("BaseMedicationViewModel", "Error fetching medication", e)
                throw e
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
                    database.scheduleDao(),
                    database.medicationLogDao(),
                    application.applicationContext)
                BaseMedicationViewModel(medicationRepository, application.applicationContext)
            }
        }
    }
}

data class MedicationData(
    val name: String,
    val doctor: String,
    val notes: String,
    val icon: MedicationIcon?,
    val status: MedicationStatus
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
    val hourlyReminderEndTime: Pair<Int, Int>?,
    val dailyReminderTimes: List<Pair<Int, Int>>,
)

data class ScheduleData(
    val startDate: LocalDate?,
    val durationType: String,
    val numDays: Int?,
    val scheduleType: String,
    val selectedDays: String,
    val daysInterval: Int?
)