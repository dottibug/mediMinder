package com.example.mediminder.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.repositories.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate


// This view model holds state for the main activity screen.
// Handles data flow between the medication repository and the main activity UI
class MainViewModel(private val repository: MedicationRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now()) // initialize to today's date
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _dateSelectorDates = MutableStateFlow<List<LocalDate>>(createDateList())
    val dateSelectorDates: StateFlow<List<LocalDate>> = _dateSelectorDates

    private val _medications = MutableStateFlow<List<Pair<Medication, Dosage?>>>(emptyList())
    val medications: StateFlow<List<Pair<Medication, Dosage?>>> = _medications

    // Create a list of dates for the date selector (-3 to +3 days from today)
    private fun createDateList(): List<LocalDate> {
        val today = LocalDate.now()
        return (-3..3).map { today.plusDays(it.toLong()) }
    }

    fun fetchMedicationsForDate(date: LocalDate) {
        viewModelScope.launch {
            _medications.value = repository.getMedicationsForDate(date)
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        fetchMedicationsForDate(date)
    }

    // Initialize medications for the current date
    init {
        fetchMedicationsForDate(LocalDate.now())
    }


    // NOTE: This is used. Do not delete.
    fun addMedication(medicationData: MedicationData, dosageData: DosageData, reminderData: ReminderData, scheduleData: ScheduleData) {
        viewModelScope.launch {
            repository.addMedication(medicationData, dosageData, reminderData, scheduleData)
        }
    }

    // View model factory: Creates the main view model with medication repository as a dependency injection
    // https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = AppDatabase.getDatabase(application)
                val medicationRepository = MedicationRepository(database.medicationDao(), database.dosageDao(), database.remindersDao(), database.scheduleDao())
                MainViewModel(medicationRepository)
            }
        }
    }

}