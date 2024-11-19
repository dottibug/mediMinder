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
import androidx.work.workDataOf
import com.example.mediminder.activities.BaseActivity.Companion.EVERY_X_HOURS
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ReminderState
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.models.ScheduleState
import com.example.mediminder.models.ValidatedData
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

// ViewModel for the BaseMedicationInfoFragment
// Handles adding or updating scheduled and as-needed medications, as well as creating the workers
// that check for missed medications and create future medication logs
class BaseMedicationViewModel(
    private val repository: MedicationRepository,
    private val applicationContext: Context
) : ViewModel() {

    private val _currentMedication = MutableStateFlow<MedicationWithDetails?>(null)
    val currentMedication: StateFlow<MedicationWithDetails?> = _currentMedication.asStateFlow()

//    private val _asNeeded = MutableStateFlow(false)
//    val asNeeded: StateFlow<Boolean> = _asNeeded.asStateFlow()

    private val _asScheduled = MutableStateFlow(true)
    val asScheduled: StateFlow<Boolean> = _asScheduled.asStateFlow()

    private val reminderState = ReminderState()
    private val scheduleState = ScheduleState()

//    fun setAsNeeded(enabled: Boolean) { _asNeeded.value = !enabled }
    fun setAsScheduled(enabled: Boolean) { _asScheduled.value = enabled }

    // Reminder state update functions
    fun updateReminderFrequency(frequency: String?) { reminderState.reminderFrequency.value = frequency ?: "" }
    fun updateHourlyReminderInterval(interval: String?) { reminderState.hourlyReminderInterval.value = interval }
    fun updateHourlyReminderStartTime(startTime: Pair<Int, Int>?) { reminderState.hourlyReminderStartTime.value = startTime }
    fun updateHourlyReminderEndTime(endTime: Pair<Int, Int>?) { reminderState.hourlyReminderEndTime.value = endTime }
    fun updateDailyReminderTimes(times: List<Pair<Int, Int>>) { reminderState.dailyReminderTimes.value = times }

    // Schedule state update functions
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

    // -----------------------------------------------------------------------------------------
    // GETTER FUNCTIONS
    // -----------------------------------------------------------------------------------------
    // Get the reminder state
    fun getReminderData(): ReminderData {
        return ReminderData(
            reminderEnabled = true, // Always true for schedule medications
            reminderFrequency = getReminderFrequency(),
            hourlyReminderInterval = reminderState.hourlyReminderInterval.value,
            hourlyReminderStartTime = reminderState.hourlyReminderStartTime.value,
            hourlyReminderEndTime = reminderState.hourlyReminderEndTime.value,
            dailyReminderTimes = reminderState.dailyReminderTimes.value,
        )
    }

    // Helper function to get the reminder frequency
    private fun getReminderFrequency(): String {
        when (reminderState.reminderFrequency.value) {
            EVERY_X_HOURS -> return EVERY_X_HOURS
            else -> return "daily"
        }
    }

    // Get the schedule state
    fun getScheduleData(): ScheduleData {
        return ScheduleData(
            isScheduled = scheduleState.isScheduledMedication.value,
            startDate = scheduleState.startDate.value,
            durationType = scheduleState.durationType.value,
            numDays = scheduleState.numDays.value,
            scheduleType = scheduleState.scheduleType.value,
            selectedDays = scheduleState.selectedDays.value,
            daysInterval = scheduleState.daysInterval.value
        )
    }

    // -----------------------------------------------------------------------------------------
    // CRUD OPERATIONS
    // -----------------------------------------------------------------------------------------
    // Fetch medication by id
    fun fetchMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                _currentMedication.value = repository.getMedicationDetailsById(medicationId)
                _asScheduled.value = _currentMedication.value?.medication?.asNeeded == false
            } catch (e: Exception) {
                Log.e("BaseMedicationViewModel", "Error fetching medication", e)
                throw e
            }
        }
    }

    // Save medication data to database. Validates data before saving.
    fun addMedication(
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?
    ) {
        // Update medicationData to include asNeeded flag
        val updatedMedicationData = medicationData.copy(asNeeded = !_asScheduled.value)

        val validatedData = if (dosageData != null) {
            getValidatedData(updatedMedicationData, dosageData, reminderData, scheduleData)
        } else {
            // As-needed medications only need medicationData validated
            ValidatedData(
                medicationData = updatedMedicationData,
                dosageData = null,
                reminderData = null,
                scheduleData = null
            )
        }

        // Save medication to database and trigger workers
        viewModelScope.launch {
            try {
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

    // Update medication data in database. Validates data before updating.
    fun updateMedication(
        medicationId: Long,
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?
    ) {
        val validatedData = if (dosageData != null) {
            getValidatedData(medicationData, dosageData, reminderData, scheduleData)
        } else {
            // As-needed medications only need medicationData validated
            ValidatedData(
                medicationData = medicationData.copy(
                    name = medicationData.name.trim().takeIf { it.isNotEmpty() }
                        ?: throw IllegalArgumentException("Medication name is required")
                ),
                dosageData = null,
                reminderData = null,
                scheduleData = null
            )
        }

        // Update medication in the database and trigger workers to update logs
        viewModelScope.launch {
            try {
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

    // -----------------------------------------------------------------------------------------
    // BACKGROUND WORKERS
    // -----------------------------------------------------------------------------------------
    // Create workers to check for missed medications and create future medication logs
    private fun createWorkers(workerPrefix: String, medicationId: Long) {
        val workManager = WorkManager.getInstance(applicationContext)
        val (createFutureLogsRequest, checkMissedRequest) = createWorkRequests(medicationId)

        // Enqueue the work requests (create future logs then check for missed medications)
        workManager.beginUniqueWork(
            "${workerPrefix}_${medicationId}_${System.currentTimeMillis()}",
            ExistingWorkPolicy.REPLACE,
            createFutureLogsRequest
        ).then(checkMissedRequest).enqueue()

        val futureLogsReqId = createFutureLogsRequest.id
        observeWorkCompletion(workManager, futureLogsReqId)
    }

    // Build work requests for creating future medication logs and checking for missed medications
    private fun createWorkRequests(medId: Long): Pair<OneTimeWorkRequest, OneTimeWorkRequest> {
        val createFutureLogsRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>()
            .setInputData(workDataOf("medicationId" to medId))
            .build()

        val checkMissedRequest = OneTimeWorkRequestBuilder<CheckMissedMedicationsWorker>().build()

        return Pair(createFutureLogsRequest, checkMissedRequest)
    }

    // Observe work completion of creating future medication logs
    // Send broadcasts to update UI and reschedule notifications upon worker completion
    private fun observeWorkCompletion(workManager: WorkManager, futureLogsReqId: UUID) {
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

    companion object {
        private const val MED_STATUS_CHANGED = "com.example.mediminder.MEDICATION_STATUS_CHANGED"
        private const val SCHEDULE_NEW_MEDICATION = "com.example.mediminder.SCHEDULE_NEW_MEDICATION"

        // Factory to create BaseMedicationViewModel (allows dependency injection)
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