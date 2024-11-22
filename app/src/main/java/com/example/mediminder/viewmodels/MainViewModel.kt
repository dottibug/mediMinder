package com.example.mediminder.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.MedicationItem
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.models.ValidatedAsNeededData
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import com.example.mediminder.utils.Constants.ERR_ADDING_MED
import com.example.mediminder.utils.Constants.ERR_ADDING_MED_USER
import com.example.mediminder.utils.Constants.ERR_DELETING_MED
import com.example.mediminder.utils.Constants.ERR_DELETING_MED_USER
import com.example.mediminder.utils.Constants.ERR_FETCHING_AS_NEEDED_MEDS
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_USER
import com.example.mediminder.utils.Constants.ERR_SETTING_STATUS
import com.example.mediminder.utils.Constants.ERR_UPDATING_STATUS
import com.example.mediminder.utils.Constants.ERR_UPDATING_STATUS_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


// This view model holds state for the main activity screen.
// Handles data flow between the medication repository and the main activity UI
class MainViewModel(private val repository: MedicationRepository) : BaseViewModel() {

    private val _medications = MutableStateFlow<List<MedicationItem>>(emptyList())
    val medications: StateFlow<List<MedicationItem>> = _medications.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _dateSelectorDates = MutableStateFlow(createDateList())
    val dateSelectorDates: StateFlow<List<LocalDate>> = _dateSelectorDates.asStateFlow()

    private val _selectedAsNeededMedId = MutableStateFlow<Long?>(null)
    val selectedAsNeededMedId: StateFlow<Long?> = _selectedAsNeededMedId.asStateFlow()

    private val _asNeededMedications = MutableStateFlow<List<Medication>>(emptyList())
    val asNeededMedications: StateFlow<List<Medication>> = _asNeededMedications.asStateFlow()

    private val _timeTaken = MutableStateFlow<Pair<Int, Int>?>(null)
    val timeTaken: StateFlow<Pair<Int, Int>?> = _timeTaken.asStateFlow()

    private val _dateTaken = MutableStateFlow<LocalDate?>(LocalDate.now())
    val dateTaken: StateFlow<LocalDate?> = _dateTaken.asStateFlow()

    private val _initialMedStatus = MutableStateFlow<MedicationStatus?>(null)
    val initialMedStatus: StateFlow<MedicationStatus?> = _initialMedStatus.asStateFlow()

    // Fetch medications for the selected date
    fun fetchMedicationsForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                startLoading()
                val today = LocalDate.now()
                val medications = repository.getLogsForDate(date)
                _medications.value = medications.map { med ->
                    med.copy(canUpdateStatus = !date.isAfter(today))
                }
            } catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_MED, e)
                setErrorMessage(ERR_FETCHING_MED_USER)
            } finally {
                stopLoading()
            }
        }
    }

    // Set the initial medication status for the given log ID
    fun setInitialMedStatus(logId: Long) {
        viewModelScope.launch {
            try {
                val status = repository.getMedicationLogStatus(logId)
                _initialMedStatus.value = status
            } catch (e: Exception) {
                Log.e(TAG, ERR_SETTING_STATUS, e)
                setErrorMessage(ERR_SETTING_STATUS)
            }
        }
    }

    // Update medication status for a given log id
    fun updateMedicationLogStatus(logId: Long, newStatus: MedicationStatus) {
        viewModelScope.launch {
            try {
                repository.updateMedicationLogStatus(logId, newStatus)
                fetchMedicationsForDate(_selectedDate.value)
            } catch (e: Exception) {
                Log.e(TAG, ERR_UPDATING_STATUS, e)
                setErrorMessage(ERR_UPDATING_STATUS_USER)
            }
        }
    }

    // Set the selected date
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        fetchMedicationsForDate(date)
    }

    // Create the list of quick dates a user can select (3 days before today to 7 days after today)
    private fun createDateList(): List<LocalDate> {
        val today = LocalDate.now()
        return (DAYS_AGO..DAYS_AHEAD).map { today.plusDays(it.toLong()) }
    }

    // Set as-needed medication selected by user
    fun setSelectedAsNeededMedication(asNeededMedId: Long?) { _selectedAsNeededMedId.value = asNeededMedId }

    // Fetch all as-needed medications from the database
    fun fetchAsNeededMedications() {
        viewModelScope.launch {
            try {
                val testResult = repository.getAsNeededMedications()
                _asNeededMedications.value = testResult
            } catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_AS_NEEDED_MEDS, e)
                setErrorMessage(ERR_FETCHING_AS_NEEDED_MEDS)
            }
        }
    }

    // Set time user took as-needed medication
    fun setTimeTaken(hour: Int, minute: Int) { _timeTaken.value = Pair(hour, minute) }

    // Set date user took as-needed medication
    fun setDateTaken(date: Long) { _dateTaken.value = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate() }

    // Add as-needed medication log to the database
    fun addAsNeededLog(validatedData: ValidatedAsNeededData) {
        viewModelScope.launch {
            try {
                repository.addAsNeededLog(validatedData)
                // Refresh medication list for the current date
                fetchMedicationsForDate(_selectedDate.value)
            } catch (e: Exception) {
                Log.e(TAG, ERR_ADDING_MED, e)
                setErrorMessage(ERR_ADDING_MED_USER)
            }
        }
    }

    // Delete as-needed medication log from the database
    fun deleteAsNeededMedication(logId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteAsNeededMedication(logId)
                fetchMedicationsForDate(_selectedDate.value)
            } catch (e: Exception) {
                Log.e(TAG, ERR_DELETING_MED, e)
                setErrorMessage(ERR_DELETING_MED_USER)
            }
        }
    }

    companion object {
        private const val TAG = "MainViewModel"
        private const val DAYS_AGO = -3
        private const val DAYS_AHEAD = 7

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                MainViewModel(repository)
            }
        }
    }
}