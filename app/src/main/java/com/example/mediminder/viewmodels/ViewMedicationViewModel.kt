package com.example.mediminder.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.utils.AppUtils.createMedicationRepository
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
                val repository = createMedicationRepository(database)
                ViewMedicationViewModel(repository)
            }
        }
    }
}