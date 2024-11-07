package com.example.mediminder.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.MedicationWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewMedicationViewModel(private val repository: MedicationRepository) : ViewModel() {

    private val _medication = MutableStateFlow<MedicationWithDetails?>(null)
    val medication: StateFlow<MedicationWithDetails?> = _medication.asStateFlow()

    fun fetchMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                _medication.value = repository.getMedicationDetailsById(medicationId)
                Log.d("ViewMedicationViewModel testcat", "Fetched medication with details: ${medication.value}")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
                val database = AppDatabase.getDatabase(application)
                val repository = MedicationRepository(
                    database.medicationDao(),
                    database.dosageDao(),
                    database.remindersDao(),
                    database.scheduleDao(),
                    database.medicationLogDao(),
                    application.applicationContext
                )
                ViewMedicationViewModel(repository)
            }
        }
    }
}