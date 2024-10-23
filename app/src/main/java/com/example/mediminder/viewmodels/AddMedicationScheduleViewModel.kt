package com.example.mediminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AddMedicationScheduleViewModel: ViewModel() {

    // Encapsulated LiveData variables
    private val _startDate = MutableLiveData<LocalDate>()
    val startDate: LiveData<LocalDate> = _startDate

    private val _durationType = MutableLiveData<String>() // "continuous" or "numDays"
    val durationType: LiveData<String> = _durationType

    private val _numDays = MutableLiveData<Int>()
    val numDays: LiveData<Int> = _numDays


    private val _scheduleType = MutableLiveData<String>() // "daily", "specificDays", "interval"
    val scheduleType: LiveData<String> = _scheduleType

    private val _selectedDays = MutableLiveData<String>()
    val selectedDays: LiveData<String> = _selectedDays


    fun setStartDate(date: Long) {
        _startDate.value = Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDate()
    }

    fun getStartDateMillis(): Long {
        return _startDate.value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: 0
    }

    fun setDurationType(durationType: String) {
        _durationType.value = durationType
    }

    fun setNumDays(numDays: Int) {
        _numDays.value = numDays
    }

    fun setScheduleType(scheduleType: String) {
        _scheduleType.value = scheduleType
    }

    fun setSelectedDays(selectedDays: String) {
        _selectedDays.value = selectedDays
    }

}