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
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.MedicationItem
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.models.ValidatedAsNeededData
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


// This view model holds state for the main activity screen.
// Handles data flow between the medication repository and the main activity UI
class MainViewModel(private val repository: MedicationRepository) : ViewModel() {

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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setErrorMessage(msg: String) { _errorMessage.value = msg }

    fun clearError() { _errorMessage.value = null }

    // Fetch medications for the selected date
    fun fetchMedicationsForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                _medications.value = repository.getLogsForDate(date)
            } catch (e: Exception) {
                Log.e("MainViewModel testcat", "Error fetching medications", e)
                _errorMessage.value = "Failed to fetch medications: ${e.message}"
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
                Log.e("MainViewModel testcat", "Error getting status", e)
                _errorMessage.value = "Failed to get medication status: ${e.message}"
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
                Log.e("MainViewModel testcat", "Error updating status", e)
                _errorMessage.value = "Failed to update medication status: ${e.message}"
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
                Log.e("MainViewModel testcat", "Error fetching as needed medications", e)
                _errorMessage.value = "Failed to fetch as needed medications: ${e.message}"
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
                Log.e("MainViewModel testcat", "Error adding as needed log", e)
                _errorMessage.value = "Failed to add as needed log: ${e.message}"
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
                Log.e("MainViewModel testcat", "Error deleting as needed medication", e)
                _errorMessage.value = "Failed to delete as needed medication: ${e.message}"
            }
        }
    }

    companion object {
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