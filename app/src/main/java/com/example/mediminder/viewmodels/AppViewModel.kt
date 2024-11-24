package com.example.mediminder.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.ReminderData
import com.example.mediminder.models.ScheduleData
import com.example.mediminder.models.ValidatedData
import com.example.mediminder.state.MedicationState
import com.example.mediminder.state.ReminderState
import com.example.mediminder.state.ScheduleState
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import com.example.mediminder.utils.Constants.ADD
import com.example.mediminder.utils.Constants.ERR_ADDING_MED
import com.example.mediminder.utils.Constants.ERR_ADDING_MED_USER
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_USER
import com.example.mediminder.utils.Constants.ERR_UPDATING_MED
import com.example.mediminder.utils.Constants.ERR_UPDATING_MED_USER
import com.example.mediminder.utils.Constants.ERR_VALIDATING_INPUT
import com.example.mediminder.utils.ValidationUtils.validateMedicationData
import com.example.mediminder.workers.WorkerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Handles adding or updating scheduled and as-needed medications, as well as creating the workers
 * that check for missed medications and create future medication logs.
 * Handles error checking
 */
open class AppViewModel(
    private val repository: MedicationRepository,
    private val applicationContext: Context
): ViewModel() {
    private val workerManager = WorkerManager(applicationContext)

    // -------- State Management --------
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    val medication = MedicationState()
    val schedule = ScheduleState()
    val reminder = ReminderState()

    // -------- Setters --------
    fun setErrorMessage(msg: String) { _errorMessage.value = msg }
    fun clearError() { _errorMessage.value = null }

    fun setAsScheduled (asScheduled: Boolean) {
        medication.asScheduled.value = asScheduled
        schedule.isScheduled.value = asScheduled
    }

    // -------- Database Functions --------
    fun fetchMedicationDetails(medId: Long) {
        viewModelScope.launch {
            try {
                val details = repository.getMedicationDetailsById(medId)
                medication.setMedicationDetails(details)
            } catch (e: Exception) {
                handleError(e, ERR_FETCHING_MED, ERR_FETCHING_MED_USER)
            }
        }
    }

    suspend fun saveMedication(
        action: String,
        medicationData: MedicationData,
        dosageData: DosageData?,
        reminderData: ReminderData?,
        scheduleData: ScheduleData?,
        medicationId: Long = -1L
    ): Boolean {
        val logError = if (action == ADD) ERR_ADDING_MED else ERR_UPDATING_MED
        val toastError = if (action == ADD) ERR_ADDING_MED_USER else ERR_UPDATING_MED_USER

        return try {
            val medData = when (action) {
                ADD -> medicationData.copy(asNeeded = !medication.asScheduled.value)
                else -> medicationData
            }

            val validData = validateMedicationData(medData, dosageData, reminderData, scheduleData)
            if (action == ADD) addMed(validData)
            else updateMed(medicationId, validData)

            true
        } catch (e: Exception) {
            handleError(e, logError, toastError)
            false
        }
    }

    private suspend fun addMed(validData: ValidatedData) {
        val medicationId = repository.addMedication(validData.medicationData, validData.dosageData,
            validData.reminderData, validData.scheduleData)
        workerManager.createWorkers(CREATE_LOGS, medicationId)
    }

    private suspend fun updateMed(medicationId: Long, validData: ValidatedData) {
        repository.deleteFutureLogs(medicationId)  // Delete future logs before updating medication
        repository.updateMedication(medicationId, validData.medicationData, validData.dosageData,
            validData.reminderData, validData.scheduleData)
        workerManager.createWorkers(UPDATE_LOGS, medicationId)
    }

    private fun handleError(e: Exception, logError: String, toastError: String) {
        Log.e(TAG, logError, e)
        when (e) {
            is IllegalArgumentException -> setErrorMessage(e.message ?: ERR_VALIDATING_INPUT)
            else -> setErrorMessage(toastError)
        }
    }

    companion object {
        private const val TAG = "AppViewModel"
        private const val CREATE_LOGS = "create_logs"
        private const val UPDATE_LOGS = "update_logs"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                AppViewModel(repository, application.applicationContext)
            }
        }
    }
}