package com.example.mediminder.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ViewModel for the DeleteMedicationActivity
class DeleteMedicationViewModel(
    private val repository: MedicationRepository,
) : ViewModel() {

    private val _currentMedication = MutableStateFlow<Medication?>(null)
    private val currentMedication: StateFlow<Medication?> = _currentMedication.asStateFlow()

    private val _medicationName = MutableStateFlow("")
    val medicationName: StateFlow<String> = _medicationName.asStateFlow()

    suspend fun fetchMedication(medicationId: Long) {
        val medication = repository.getMedicationById(medicationId)
        _currentMedication.value = medication
        _medicationName.value = medication.name
    }

    suspend fun deleteMedication() {
        currentMedication.value?.let { medication ->
            repository.deleteMedication(medication.id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                DeleteMedicationViewModel(repository)
            }
        }
    }
}