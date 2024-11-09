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
import com.example.mediminder.models.MedicationItem
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate


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


    // Coroutine off the main thread to avoid blocking the UI
    fun fetchMedicationsForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                _medications.value = repository.getMedicationLogsForDate(date)
            } catch (e: Exception) {
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