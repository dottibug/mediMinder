package com.example.mediminder.viewmodels

import android.util.Log
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

    private val _hourlyReminderStartTime = MutableLiveData<Pair<Int, Int>>() // [hour, minute]
    val hourlyReminderStartTime: LiveData<Pair<Int, Int>> = _hourlyReminderStartTime

    private val _dailyReminderTimes = MutableLiveData<List<Pair<Int, Int>>>(mutableListOf()) // (hour, minute)
    val dailyReminderTimes: LiveData<List<Pair<Int, Int>>> = _dailyReminderTimes


    fun setReminderEnabled(enabled: Boolean) {
        _isReminderEnabled.value = enabled
    }

    fun setReminderFrequency(freq: String) {
        _reminderFrequency.value = freq
    }

    private fun sortDailyReminderTimes(times: List<Pair<Int, Int>>): MutableList<Pair<Int, Int>> {
        val sortedTimes = times.sortedBy { it.first }
        return sortedTimes.toMutableList()
    }

    // Add new time to the list of daily reminder times for a medication
    // The new time defaults to 12:00 PM. The list does not need to be sorted, as the user has yet
    // to select a time
    fun addDailyReminderTime(hour: Int, minute: Int) {
        val times = _dailyReminderTimes.value?.toMutableList() ?: mutableListOf()
        times.add(Pair(hour, minute))
        _dailyReminderTimes.value = times
    }

    // Update the time at the specified index in the list, then sort the list by hour, ascending
    fun updateDailyReminderTime(index: Int, hour: Int, minute: Int) {
        var times = _dailyReminderTimes.value?.toMutableList() ?: mutableListOf()
        if (index in times.indices) {
            times[index] = Pair(hour, minute)
            times = sortDailyReminderTimes(times)
            _dailyReminderTimes.value = times
        }
    }

    // Remove a specific time from the list of daily reminder times for a medication
    fun removeDailyReminderTime(index: Int) {
        val times = _dailyReminderTimes.value?.toMutableList() ?: mutableListOf()
        if (index in times.indices) {
            times.removeAt(index)
            _dailyReminderTimes.value = times
        }
    }


    ///////

    fun setHourlyReminderInterval(interval: String) {
        _hourlyReminderInterval.value = interval
    }

    // Note: minute is 0 to 60, hour is 0 to 23
    fun setHourlyReminderStartTime(hour: Int, minute: Int) {
        _hourlyReminderStartTime.value = Pair(hour, minute)
    }

    // Note: minute is 0 to 60, hour is 0 to 23
    fun setDailyReminderTimes(hour: Int, minute: Int) {
        // Add as a pair to the list
        val times = _dailyReminderTimes.value?.toMutableList() ?: mutableListOf()
        times.add(Pair(hour, minute))
        _dailyReminderTimes.value = times

        // Sort the list by the hour (ascending)
        _dailyReminderTimes.value = times.sortedBy { it.first }

        Log.i("testcat", "setDailyReminderTimes: ${_dailyReminderTimes.value}")
    }

}