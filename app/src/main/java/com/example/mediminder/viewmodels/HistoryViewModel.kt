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

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    fun setSelectedMedication(medicationId: Long?) { _selectedMedicationId.value = medicationId }
    fun setSelectedMonth(yearMonth: YearMonth) { _selectedMonth.value = yearMonth }

    fun moveMonth(forward: Boolean) {
        _selectedMonth.value = when {
            forward -> _selectedMonth.value.plusMonths(1)
            else -> _selectedMonth.value.minusMonths(1)
        }
    }

    suspend fun fetchMedications(): List<Medication> {
        try {
            val medications = repository.getAllMedicationsSimple()
            _medications.value = medications
            return medications
        } catch (e: Exception) {
            Log.e("HistoryViewModel testcat", "Error loading medications", e)
            return emptyList()
        }
    }

    suspend fun fetchMedicationHistory(medicationId: Long?): List<DayLogs>? {
       try {
           val selectedMonth = selectedMonth.value
           val today = LocalDate.now()
           val currentMonth = YearMonth.from(today)

           if (selectedMonth.isAfter(currentMonth)) return null

           val history = repository.getMedicationHistory(medicationId, selectedMonth)
           if (history.logs.isEmpty() && selectedMonth.isBefore(currentMonth)) { return null }
           else { return getDayLogs(currentMonth, selectedMonth, today, history) }

        } catch (e: Exception) {
            Log.e("HistoryViewModel testcat", "Error loading medication history", e)
            return null
        }
    }

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