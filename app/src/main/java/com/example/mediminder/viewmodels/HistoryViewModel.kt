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
import com.example.mediminder.models.DayLogs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth

class HistoryViewModel(private val repository: MedicationRepository): ViewModel() {

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _selectedMedicationId = MutableStateFlow<Long?>(null)
    val selectedMedicationId: StateFlow<Long?> = _selectedMedicationId.asStateFlow()

    private val _selectedDate = MutableStateFlow(YearMonth.now())
    val selectedDate: StateFlow<YearMonth> = _selectedDate.asStateFlow()

    fun setSelectedMedication(medicationId: Long?) {
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

    // return: med name, med dosage, med planned date, med status
    suspend fun fetchMedicationHistory(medicationId: Long?): List<DayLogs>? {
        Log.d("HistoryViewModel testcat", "Fetching medication history for medication ID: $medicationId")
        return try {
            val selectedMonth = selectedDate.value
            val today = LocalDate.now()
            val currentMonth = YearMonth.from(today)

            // If selected month is in the future, return null
            if (selectedMonth.isAfter(currentMonth)) {
                Log.d("HistoryViewModel testcat", "Selected month is in future, returning null")
                return null
            }

            val history = repository.getMedicationHistory(medicationId, selectedMonth)
            Log.d("HistoryViewModel testcat", "Got history with ${history.logs.size} logs")


            // If no logs for this month at all, return null
            if (history.logs.isEmpty() && selectedMonth.isBefore(currentMonth)) {
                Log.d("HistoryViewModel testcat", "No logs for past month, returning null")
                return null
            }

            // Determine last day of month to show
            // If current month, only show logs up to today (do not show future scheduled logs)
            val lastDayOfMonth = if (selectedMonth == currentMonth) { today }
            else { selectedMonth.atEndOfMonth() }

            // Get days in the selected month
            val daysInMonth = (1..lastDayOfMonth.dayOfMonth).map { day ->
                selectedMonth.atDay(day)
            }

            Log.d("HistoryViewModel testcat", "Showing days from ${daysInMonth.first()} to ${daysInMonth.last()}")

            // Group medication logs by day
            val result = daysInMonth.map { day ->
                val logsForDay = history.logs.filter {
                    it.log.plannedDatetime.toLocalDate() == day
                }
                DayLogs(date = day, logs = logsForDay)
            }

            Log.d("HistoryViewModel testcat", "Returning ${result.size} day groups")
            result.forEach { dayGroup ->
                Log.d("HistoryViewModel testcat", "Day ${dayGroup.date}: ${dayGroup.logs.size} logs")
            }

            return result

        } catch (e: Exception) {
            Log.e("HistoryViewModel testcat", "Error loading medication history", e)
            return null
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