package com.example.mediminder.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class BaseScheduleViewModel: ViewModel() {
//    private val _isScheduleEnabled = MutableStateFlow(true)
//    val isScheduleEnabled: StateFlow<Boolean> = _isScheduleEnabled.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private val _durationType = MutableStateFlow("continuous") // "continuous" or "numDays"
    val durationType: StateFlow<String> = _durationType.asStateFlow()

    private val _numDays = MutableStateFlow<Int?>(null)
    val numDays: StateFlow<Int?> = _numDays.asStateFlow()

    private val _scheduleType = MutableStateFlow("daily") // "daily", "specificDays", "interval"
    val scheduleType: StateFlow<String> = _scheduleType.asStateFlow()

    // "0,1,2" -> Sunday, Monday, Tuesday
    private val _selectedDays = MutableStateFlow("")
    val selectedDays: StateFlow<String> = _selectedDays.asStateFlow()

    private val _daysInterval = MutableStateFlow<Int?>(null)
    val daysInterval: StateFlow<Int?> = _daysInterval.asStateFlow()

//    fun setScheduleEnabled(enabled: Boolean) { _isScheduleEnabled.value = enabled }

    fun setStartDate(date: Long?) {
        if (date == null) { _startDate.value = null }
        else {
            _startDate.value = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
        }
    }

    fun setDurationType(durationType: String) { _durationType.value = durationType }

    fun setNumDays(numDays: Int?) { _numDays.value = numDays }

    fun setScheduleType(scheduleType: String) { _scheduleType.value = scheduleType }

    fun setSelectedDays(selectedDays: String) { _selectedDays.value = selectedDays }

    fun setDaysInterval(daysInterval: String) { _daysInterval.value = daysInterval.toIntOrNull() }

    fun clearScheduleSettings() {
        _startDate.value = null
        _durationType.value = "continuous"
        _numDays.value = null
        _scheduleType.value = "daily"
        _selectedDays.value = ""
        _daysInterval.value = null
    }
}