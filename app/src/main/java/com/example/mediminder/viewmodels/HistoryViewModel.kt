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
import com.example.mediminder.models.MedicationHistory
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import com.example.mediminder.utils.Constants.ERR_FETCHING_MEDS
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_HISTORY
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_HISTORY_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth

// View model for the HistoryActivity
// Tracks selected medication and month, and provides methods to fetch medication history
class HistoryViewModel(private val repository: MedicationRepository): ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _selectedMedicationId = MutableStateFlow<Long?>(null)
    val selectedMedicationId: StateFlow<Long?> = _selectedMedicationId.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Error message functions
    fun setErrorMessage(msg: String) { _errorMessage.value = msg }
    fun clearError() { _errorMessage.value = null }

    // Selection functions
    fun setSelectedMedication(medicationId: Long?) { _selectedMedicationId.value = medicationId }
    fun setSelectedMonth(yearMonth: YearMonth) { _selectedMonth.value = yearMonth }

    // Move to the previous or next month
    fun moveMonth(forward: Boolean) {
        _selectedMonth.value = when {
            forward -> _selectedMonth.value.plusMonths(1)
            else -> _selectedMonth.value.minusMonths(1)
        }
    }

    // Fetch medications
    suspend fun fetchMedications(): List<Medication> {
        try {
            _isLoading.value = true
            val medications = repository.getAllMedicationsSimple()
            _medications.value = medications
            return medications
        } catch (e: Exception) {
            Log.e(TAG, ERR_FETCHING_MEDS, e)
            setErrorMessage(ERR_FETCHING_MEDS)
            return emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    // Fetch medication history for a specific medication
    suspend fun fetchMedicationHistory(medicationId: Long?): List<DayLogs>? {
       try {
           _isLoading.value = true
           val selectedMonth = selectedMonth.value
           val today = LocalDate.now()
           val currentMonth = YearMonth.from(today)
           if (selectedMonth.isAfter(currentMonth)) return null
           val history = repository.getMedicationHistory(medicationId, selectedMonth)
           if (history.logs.isEmpty() && selectedMonth.isBefore(currentMonth)) { return null }
           else { return getDayLogs(currentMonth, selectedMonth, today, history) }
        } catch (e: Exception) {
           Log.e(TAG, ERR_FETCHING_MED_HISTORY, e)
           setErrorMessage(ERR_FETCHING_MED_HISTORY_USER)
           return null
        } finally {
            _isLoading.value = false
        }
    }

    // Get the logs for each day in the selected month
    private fun getDayLogs(
        currentMonth: YearMonth,
        selectedMonth: YearMonth,
        today: LocalDate,
        history: MedicationHistory
    ): List<DayLogs> {
        val lastDayToShow = getLastDayToShow(currentMonth, selectedMonth, today)
        val daysInMonth = getDaysInMonth(lastDayToShow, selectedMonth)
        return groupByDay(daysInMonth, history)
    }

    // Determine last day of month to show: today if current month, else the end of the selected month
    private fun getLastDayToShow(currentMonth: YearMonth, selectedMonth: YearMonth, today: LocalDate): LocalDate {
        return if (selectedMonth == currentMonth) { today }
        else { selectedMonth.atEndOfMonth() }
    }

    // Get days in the selected month
    private fun getDaysInMonth(lastDayToShow: LocalDate, selectedMonth: YearMonth): List<LocalDate> {
        return (1..lastDayToShow.dayOfMonth).map { day -> selectedMonth.atDay(day) }
    }

    // Group medication logs by day
    private fun groupByDay(daysInMonth: List<LocalDate>, history: MedicationHistory): List<DayLogs> {
        return daysInMonth.map { day ->
            val logsForDay = history.logs.filter { it.log.plannedDatetime.toLocalDate() == day }
            DayLogs(date = day, logs = logsForDay)
        }
    }

    companion object {
        private const val TAG = "HistoryViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                HistoryViewModel(repository)
            }
        }
    }
}