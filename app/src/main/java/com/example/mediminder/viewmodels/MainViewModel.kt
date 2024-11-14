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

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val _selectedAsNeededMedId = MutableStateFlow<Long?>(null)
    val selectedAsNeededMedId: StateFlow<Long?> = _selectedAsNeededMedId.asStateFlow()

    private val _asNeededMedications = MutableStateFlow<List<Medication>>(emptyList())
    val asNeededMedications: StateFlow<List<Medication>> = _asNeededMedications.asStateFlow()

    private val _timeTaken = MutableStateFlow<Pair<Int, Int>?>(null)
    val timeTaken: StateFlow<Pair<Int, Int>?> = _timeTaken.asStateFlow()

    private val _dateTaken = MutableStateFlow<LocalDate?>(LocalDate.now())
    val dateTaken: StateFlow<LocalDate?> = _dateTaken.asStateFlow()

    fun fetchMedicationsForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                _medications.value = repository.getLogsForDate(date)
            } catch (e: Exception) {
                Log.e("MainViewModel testcat", "Error fetching medications", e)
                _errorState.value = "Failed to fetch medications: ${e.message}"
            }
        }
    }

    // Coroutine off the main thread to avoid blocking the UI
    fun updateMedicationLogStatus(logId: Long, newStatus: MedicationStatus) {
        viewModelScope.launch {
            try {
                repository.updateMedicationLogStatus(logId, newStatus)
                fetchMedicationsForDate(_selectedDate.value)
            } catch (e: Exception) {
                Log.e("MainViewModel testcat", "Error updating status", e)
                _errorState.value = "Failed to update medication status: ${e.message}"
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        fetchMedicationsForDate(date)
    }

    private fun createDateList(): List<LocalDate> {
        val today = LocalDate.now()
        return (-3..3).map { today.plusDays(it.toLong()) }
    }

    fun setSelectedAsNeededMedication(asNeededMedId: Long?) { _selectedAsNeededMedId.value = asNeededMedId }

    fun fetchAsNeededMedications() {
        viewModelScope.launch {
            try {
                val testResult = repository.getAsNeededMedications()
                Log.d("MainViewModel testcat", "testResult: $testResult")
                _asNeededMedications.value = testResult

            } catch (e: Exception) {
                Log.e("MainViewModel testcat", "Error fetching as needed medications", e)
                _errorState.value = "Failed to fetch as needed medications: ${e.message}"
            }
        }
    }

    fun setTimeTaken(hour: Int, minute: Int) { _timeTaken.value = Pair(hour, minute) }

    fun setDateTaken(date: Long) { _dateTaken.value = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate() }

    fun addAsNeededLog(validatedData: ValidatedAsNeededData) {
        viewModelScope.launch {
            try {
                repository.addAsNeededLog(validatedData)
                // Refresh medication list for the current date
                fetchMedicationsForDate(_selectedDate.value)
            } catch (e: Exception) {
                Log.e("MainViewModel testcat", "Error adding as needed log", e)
                _errorState.value = "Failed to add as needed log: ${e.message}"
            }
        }
    }

    fun deleteAsNeededMedication(logId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteAsNeededMedication(logId)
                fetchMedicationsForDate(_selectedDate.value)
            } catch (e: Exception) {
                Log.e("MainViewModel testcat", "Error deleting as needed medication", e)
                _errorState.value = "Failed to delete as needed medication: ${e.message}"
            }
        }
    }

    companion object {
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