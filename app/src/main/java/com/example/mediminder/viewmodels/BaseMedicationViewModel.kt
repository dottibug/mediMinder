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
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ReminderState
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.models.ScheduleState
import com.example.mediminder.receivers.MedicationSchedulerReceiver
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import com.example.mediminder.utils.ValidationUtils.getValidatedData
import com.example.mediminder.workers.CheckMissedMedicationsWorker
import com.example.mediminder.workers.CreateFutureMedicationLogsWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class BaseMedicationViewModel(
    private val repository: MedicationRepository,
    private val applicationContext: Context
) : ViewModel() {

    private val reminderState = ReminderState()
    private val scheduleState = ScheduleState()

    // Current medication (for editing medications)
    private val _currentMedication = MutableStateFlow<MedicationWithDetails?>(null)
    val currentMedication: StateFlow<MedicationWithDetails?> = _currentMedication.asStateFlow()

    // Update Functions
    fun updateIsReminderEnabled(enabled: Boolean) { reminderState.reminderEnabled.value = enabled }
    fun updateReminderFrequency(frequency: String?) { reminderState.reminderFrequency.value = frequency ?: "" }
    fun updateHourlyReminderInterval(interval: String?) { reminderState.hourlyReminderInterval.value = interval }
    fun updateHourlyReminderStartTime(startTime: Pair<Int, Int>?) { reminderState.hourlyReminderStartTime.value = startTime }
    fun updateHourlyReminderEndTime(endTime: Pair<Int, Int>?) { reminderState.hourlyReminderEndTime.value = endTime }
    fun updateDailyReminderTimes(times: List<Pair<Int, Int>>) { reminderState.dailyReminderTimes.value = times }

    fun updateStartDate(date: LocalDate?) { scheduleState.startDate.value = date }
    fun updateDurationType(type: String) { scheduleState.durationType.value = type }
    fun updateNumDays(days: Int?) { scheduleState.numDays.value = days }
    fun updateScheduleType(type: String) { scheduleState.scheduleType.value = type }

    fun updateSelectedDays(days: String) {
        scheduleState.selectedDays.value = days
        scheduleState.daysInterval.value = 0
    }

    fun updateDaysInterval(interval: Int?) {
        scheduleState.daysInterval.value = interval
        scheduleState.selectedDays.value = ""
    }

    // Getter Functions
    fun getReminderData(): ReminderData {
        return ReminderData(
            reminderEnabled = reminderState.reminderEnabled.value,
            reminderFrequency = reminderState.reminderFrequency.value,
            hourlyReminderInterval = reminderState.hourlyReminderInterval.value,
            hourlyReminderStartTime = reminderState.hourlyReminderStartTime.value,
            hourlyReminderEndTime = reminderState.hourlyReminderEndTime.value,
            dailyReminderTimes = reminderState.dailyReminderTimes.value,
        )
    }

    fun getScheduleData(): ScheduleData {
        return ScheduleData(
            startDate = scheduleState.startDate.value,
            durationType = scheduleState.durationType.value,
            numDays = scheduleState.numDays.value,
            scheduleType = scheduleState.scheduleType.value,
            selectedDays = scheduleState.selectedDays.value,
            daysInterval = scheduleState.daysInterval.value
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
        val validatedData = getValidatedData(medicationData, dosageData, reminderData, scheduleData)

        viewModelScope.launch {
            try {
                // Save to database
                val medicationId = repository.addMedication(
                    validatedData.medicationData,
                    validatedData.dosageData,
                    validatedData.reminderData,
                    validatedData.scheduleData
                )
                createWorkers("create_logs", medicationId)
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
        val validatedData = getValidatedData(medicationData, dosageData, reminderData, scheduleData)

        viewModelScope.launch {
            try {
                // Update in database
                repository.updateMedication(
                    medicationId,
                    validatedData.medicationData,
                    validatedData.dosageData,
                    validatedData.reminderData,
                    validatedData.scheduleData
                )
                createWorkers("update_logs", medicationId)
            } catch (e: Exception) {
                Log.e("testcat", "Error updating medication: ${e.message}")
                throw e
            }
        }
    }

    private fun createWorkers(workerPrefix: String, medicationId: Long) {
        val workManager = WorkManager.getInstance(applicationContext)
        val (createFutureLogsRequest, checkMissedRequest) = createWorkRequests()

        workManager.beginUniqueWork(
            "${workerPrefix}_${medicationId}_${System.currentTimeMillis()}",
            ExistingWorkPolicy.REPLACE,
            createFutureLogsRequest
        ).then(checkMissedRequest).enqueue()

        val futureLogsReqId = createFutureLogsRequest.id
        observeWorkCompletion(workManager, futureLogsReqId)
    }

    private fun createWorkRequests(): Pair<OneTimeWorkRequest, OneTimeWorkRequest> {
        val createFutureLogsRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>().build()
        val checkMissedRequest = OneTimeWorkRequestBuilder<CheckMissedMedicationsWorker>().build()
        return Pair(createFutureLogsRequest, checkMissedRequest)
    }

    private fun observeWorkCompletion(workManager: WorkManager, futureLogsReqId: UUID) {
        // Observe work completion before sending broadcasts
        workManager.getWorkInfoByIdLiveData(futureLogsReqId)
            .observeForever { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    // Send broadcast to update UI
                    val updateIntent = Intent(MED_STATUS_CHANGED).apply {
                        setPackage(applicationContext.packageName)
                    }
                    applicationContext.sendBroadcast(updateIntent)

                    // Reschedule notifications
                    val schedulerIntent = Intent(applicationContext, MedicationSchedulerReceiver::class.java).apply {
                        action = SCHEDULE_NEW_MEDICATION
                    }
                    applicationContext.sendBroadcast(schedulerIntent)
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
        private const val MED_STATUS_CHANGED = "com.example.mediminder.MEDICATION_STATUS_CHANGED"
        private const val SCHEDULE_NEW_MEDICATION = "com.example.mediminder.SCHEDULE_NEW_MEDICATION"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                BaseMedicationViewModel(repository, application.applicationContext)
            }
        }
    }
}