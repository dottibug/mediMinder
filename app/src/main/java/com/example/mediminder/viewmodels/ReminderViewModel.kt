package com.example.mediminder.viewmodels

import androidx.lifecycle.ViewModel
import com.example.mediminder.utils.Constants.EVERY_X_HOURS
import com.example.mediminder.utils.Constants.X_TIMES_DAILY
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the ReminderFragment
 */
class ReminderViewModel: ViewModel() {
    private val _reminderFrequency = MutableStateFlow<String?>(null) // hourly or daily
    val reminderFrequency: StateFlow<String?> = _reminderFrequency.asStateFlow()

    private val _dailyReminderTimes = MutableStateFlow<List<Pair<Int, Int>>>(emptyList()) // (hour, minute)
    val dailyReminderTimes: StateFlow<List<Pair<Int, Int>>> = _dailyReminderTimes.asStateFlow()

    private val _hourlyReminderInterval = MutableStateFlow<String?>(null) // <String>() // 30 minutes, 1 hour, .. 12 hours
    val hourlyReminderInterval: StateFlow<String?> = _hourlyReminderInterval.asStateFlow()

    private val _hourlyReminderStartTime = MutableStateFlow<Pair<Int, Int>?>(null) // (hour, minute)
    val hourlyReminderStartTime: StateFlow<Pair<Int, Int>?> = _hourlyReminderStartTime.asStateFlow()

    private val _hourlyReminderEndTime = MutableStateFlow<Pair<Int, Int>?>(null) // (hour, minute)
    val hourlyReminderEndTime: StateFlow<Pair<Int, Int>?> = _hourlyReminderEndTime.asStateFlow()

    fun setReminderFrequency(freq: String) {
        _reminderFrequency.value = when (freq) {
            EVERY_X_HOURS -> {
                clearDailyReminderTimes()
                EVERY_X_HOURS
            }
            else -> {
                clearHourlyReminderSettings()
                X_TIMES_DAILY
            }
        }
    }

    private fun clearHourlyReminderSettings() {
        _hourlyReminderInterval.value = null
        _hourlyReminderStartTime.value = null
        _hourlyReminderEndTime.value = null
    }

    // Add a daily reminder time (defaults to 12:00 PM)
    fun addDailyReminderTime(hour: Int, minute: Int) {
        val times = _dailyReminderTimes.value.toMutableList()
        times.add(Pair(hour, minute))
        _dailyReminderTimes.value = times
    }

    // Remove a specific daily reminder time
    fun removeDailyReminderTime(index: Int) {
        val times = _dailyReminderTimes.value.toMutableList()

        if (index in times.indices) {
            times.removeAt(index)
            _dailyReminderTimes.value = times

            // If there are no times left, set the reminder frequency to "every x hours"
            if (times.size == 0) { setReminderFrequency(EVERY_X_HOURS) }
        }
    }

    fun setHourlyReminderInterval(interval: String?) { _hourlyReminderInterval.value = interval }

    // Note: minute is 0 to 60, hour is 0 to 23
    fun setHourlyReminderStartTime(hour: Int, minute: Int) { _hourlyReminderStartTime.value = Pair(hour, minute) }
    fun setHourlyReminderEndTime(hour: Int, minute: Int) { _hourlyReminderEndTime.value = Pair(hour, minute) }

    // Update daily reminder time at the specified index. Sorts the list by hour (ascending)
    fun updateDailyReminderTime(index: Int, hour: Int, minute: Int) {
        var times = _dailyReminderTimes.value.toMutableList()
        if (index in times.indices) {
            times[index] = Pair(hour, minute)
            times = sortDailyReminderTimes(times)
            _dailyReminderTimes.value = times
        }
    }

    fun setDailyReminderTimes(times: List<Pair<Int, Int>>) {
        _dailyReminderTimes.value = sortDailyReminderTimes(times)
    }

    private fun clearDailyReminderTimes() {
        _dailyReminderTimes.value = emptyList()
    }

    // Sort daily reminder times by hour (ascending)
    private fun sortDailyReminderTimes(times: List<Pair<Int, Int>>): MutableList<Pair<Int, Int>> {
        val sortedTimes = times.sortedBy { it.first }
        return sortedTimes.toMutableList()
    }
}