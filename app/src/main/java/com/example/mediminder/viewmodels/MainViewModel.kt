package com.example.mediminder.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.repositories.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate


// This view model holds state for the main activity screen. It uses the medication repository to
// get medication data
class MainViewModel(private val repository: MedicationRepository) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now()) // initialize to today's date
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow() // expose as immutable state flow

    private val _medications = MutableStateFlow<List<Medication>>(emptyList()) // initialize to empty list
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow() // expose as immutable state flow

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        loadMedicationsForDate(date)
    }

    private fun loadMedicationsForDate(date: LocalDate) {
        // todo: load medications for the given date from the repository
    }

    // View model factory: Creates the main view model with medication repository as a dependency injection
    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val medicationRepository = MedicationRepository()
                MainViewModel(medicationRepository)
            }
        }
    }

}