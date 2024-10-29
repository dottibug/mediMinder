package com.example.mediminder.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddMedicationReminderViewModel: ViewModel() {

    private val _isReminderEnabled = MutableStateFlow(false)
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    private val _reminderFrequency = MutableStateFlow<String?>(null) // hourly or daily
    val reminderFrequency: StateFlow<String?> = _reminderFrequency.asStateFlow()

    private val _hourlyReminderInterval = MutableStateFlow<String?>(null) // <String>() // 30 minutes, 1 hour, .. 12 hours
    val hourlyReminderInterval: StateFlow<String?> = _hourlyReminderInterval.asStateFlow()

    private val _hourlyReminderStartTime = MutableStateFlow<Pair<Int, Int>?>(null) // (hour, minute)
    val hourlyReminderStartTime: StateFlow<Pair<Int, Int>?> = _hourlyReminderStartTime.asStateFlow()

    private val _hourlyReminderEndTime = MutableStateFlow<Pair<Int, Int>?>(null) // (hour, minute)
    val hourlyReminderEndTime: StateFlow<Pair<Int, Int>?> = _hourlyReminderEndTime.asStateFlow()

    private val _dailyReminderTimes = MutableStateFlow<List<Pair<Int, Int>>>(emptyList()) // (hour, minute)
    val dailyReminderTimes: StateFlow<List<Pair<Int, Int>>> = _dailyReminderTimes.asStateFlow()

    fun setReminderEnabled(enabled: Boolean) { _isReminderEnabled.value = enabled }

    fun setReminderFrequency(freq: String) {
        when (freq) {
            "every x hours" -> {
                _reminderFrequency.value = "hourly"
                clearDailyReminderTimes()
            }
            else -> {
                _reminderFrequency.value = "daily"
                clearHourlyReminderInterval()
                clearHourlyReminderStartTime()
                clearHourlyReminderEndTime()
            }
        }
    }

    fun setHourlyReminderInterval(interval: String) { _hourlyReminderInterval.value = interval }

    private fun clearHourlyReminderInterval() { _hourlyReminderInterval.value = null }

    // Note: minute is 0 to 60, hour is 0 to 23
    fun setHourlyReminderStartTime(hour: Int, minute: Int) {
        _hourlyReminderStartTime.value = Pair(hour, minute)
    }

    fun setHourlyReminderEndTime(hour: Int, minute: Int) {
        _hourlyReminderEndTime.value = Pair(hour, minute)
    }

    private fun clearHourlyReminderStartTime() { _hourlyReminderStartTime.value = null }
    private fun clearHourlyReminderEndTime() { _hourlyReminderEndTime.value = null }

    private fun clearDailyReminderTimes() { _dailyReminderTimes.value = emptyList() }

    // Add a daily reminder time (defaults to 12:00 PM)
    fun addDailyReminderTime(hour: Int, minute: Int) {
        val times = _dailyReminderTimes.value.toMutableList()
        times.add(Pair(hour, minute))
        _dailyReminderTimes.value = times
    }

    // Update daily reminder time at the specified index. Sorts the list by hour (ascending)
    fun updateDailyReminderTime(index: Int, hour: Int, minute: Int) {
        var times = _dailyReminderTimes.value.toMutableList()
        if (index in times.indices) {
            times[index] = Pair(hour, minute)
            times = sortDailyReminderTimes(times)
            _dailyReminderTimes.value = times
        }
    }

    // Remove a specific daily reminder time
    fun removeDailyReminderTime(index: Int) {
        val times = _dailyReminderTimes.value.toMutableList()
        if (index in times.indices) {
            times.removeAt(index)
            _dailyReminderTimes.value = times
        }
    }

    private fun sortDailyReminderTimes(times: List<Pair<Int, Int>>): MutableList<Pair<Int, Int>> {
        val sortedTimes = times.sortedBy { it.first }
        return sortedTimes.toMutableList()
    }
}