package com.example.mediminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddMedicationReminderViewModel: ViewModel() {

    private val _isReminderEnabled = MutableLiveData<Boolean>(false)
    val isReminderEnabled: LiveData<Boolean> = _isReminderEnabled

    private val _reminderFrequency = MutableLiveData<String>() // hourly or daily
    val reminderFrequency: LiveData<String> = _reminderFrequency

    private val _hourlyReminderInterval = MutableLiveData<String>() // 30 minutes, 1 hour, .. 12 hours
    val hourlyReminderInterval: LiveData<String> = _hourlyReminderInterval

    private val _hourlyReminderStartTime = MutableLiveData<Pair<Int, Int>>() // (hour, minute)
    val hourlyReminderStartTime: LiveData<Pair<Int, Int>> = _hourlyReminderStartTime

    private val _dailyReminderTimes = MutableLiveData<List<Pair<Int, Int>>>(mutableListOf()) // (hour, minute)
    val dailyReminderTimes: LiveData<List<Pair<Int, Int>>> = _dailyReminderTimes

    private val _reminderType = MutableLiveData<String>("Alarm") // default to alarm
    val reminderType: LiveData<String> = _reminderType


    fun setReminderEnabled(enabled: Boolean) {
        _isReminderEnabled.value = enabled
    }

    fun setReminderFrequency(freq: String) {
        _reminderFrequency.value = freq
    }

    fun setHourlyReminderInterval(interval: String) {
        _hourlyReminderInterval.value = interval
    }

    // Note: minute is 0 to 60, hour is 0 to 23
    fun setHourlyReminderStartTime(hour: Int, minute: Int) {
        _hourlyReminderStartTime.value = Pair(hour, minute)
    }

    private fun sortDailyReminderTimes(times: List<Pair<Int, Int>>): MutableList<Pair<Int, Int>> {
        val sortedTimes = times.sortedBy { it.first }
        return sortedTimes.toMutableList()
    }

    // Add a daily reminder time (defaults to 12:00 PM)
    fun addDailyReminderTime(hour: Int, minute: Int) {
        val times = _dailyReminderTimes.value?.toMutableList() ?: mutableListOf()
        times.add(Pair(hour, minute))
        _dailyReminderTimes.value = times
    }

    // Update daily reminder time at the specified index. Sorts the list by hour (ascending)
    fun updateDailyReminderTime(index: Int, hour: Int, minute: Int) {
        var times = _dailyReminderTimes.value?.toMutableList() ?: mutableListOf()
        if (index in times.indices) {
            times[index] = Pair(hour, minute)
            times = sortDailyReminderTimes(times)
            _dailyReminderTimes.value = times
        }
    }

    // Remove a specific daily reminder time
    fun removeDailyReminderTime(index: Int) {
        val times = _dailyReminderTimes.value?.toMutableList() ?: mutableListOf()
        if (index in times.indices) {
            times.removeAt(index)
            _dailyReminderTimes.value = times
        }
    }

    fun setReminderType(type: String) {
        _reminderType.value = type
    }
}