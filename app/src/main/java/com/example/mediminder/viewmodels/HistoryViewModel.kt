package com.example.mediminder.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.repositories.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth

class HistoryViewModel(private val repository: MedicationRepository): ViewModel() {

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _selectedMedicationId = MutableStateFlow<Long?>(null)
    val selectedMedicationId: StateFlow<Long?> = _selectedMedicationId.asStateFlow()

    private val _selectedDate = MutableStateFlow(YearMonth.now())
    val selectedDate: StateFlow<YearMonth> = _selectedDate.asStateFlow()

    fun setSelectedMedication(medicationId: Long) {
        _selectedMedicationId.value = medicationId
    }

    fun moveMonth(forward: Boolean) {
        _selectedDate.value = if (forward) {
            _selectedDate.value.plusMonths(1)
        } else {
            _selectedDate.value.minusMonths(1)
        }
    }

    fun setSelectedMonth(yearMonth: YearMonth) {
        _selectedDate.value = yearMonth
    }

    suspend fun fetchMedications(): List<Medication> {
        return try {
            val medications = repository.getAllMedicationsSimple()
            _medications.value = medications

            medications.forEach { medication ->
                Log.d("HistoryViewModel testcat", "Medication: $medication")
            }

            medications
        } catch (e: Exception) {
            Log.e("HistoryViewModel testcat", "Error loading medications", e)
            emptyList()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val database = AppDatabase.getDatabase(application)
                val repository = MedicationRepository(
                    database.medicationDao(),
                    database.dosageDao(),
                    database.remindersDao(),
                    database.scheduleDao(),
                    database.medicationLogDao(),
                    application
                )
                HistoryViewModel(repository)
            }
        }
    }
}