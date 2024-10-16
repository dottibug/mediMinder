package com.example.mediminder.viewmodels

import androidx.lifecycle.ViewModel
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
}