package com.example.mediminder.state

import com.example.mediminder.models.ReminderData
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.EVERY_X_HOURS
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Reminder state class for holding reminder details
 */
class ReminderState {
    val reminderEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val reminderFrequency: MutableStateFlow<String> = MutableStateFlow(EMPTY_STRING)
    val hourlyReminderInterval: MutableStateFlow<String?> = MutableStateFlow(null)
    val hourlyReminderStartTime: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)
    val hourlyReminderEndTime: MutableStateFlow<Pair<Int, Int>?> = MutableStateFlow(null)
    val dailyReminderTimes: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(emptyList())

    fun setReminderFrequency(frequency: String?) { reminderFrequency.value = frequency ?: EMPTY_STRING }
    fun setHourlyReminderInterval(interval: String?) { hourlyReminderInterval.value = interval }
    fun setHourlyReminderStartTime(startTime: Pair<Int, Int>?) { hourlyReminderStartTime.value = startTime }
    fun setHourlyReminderEndTime(endTime: Pair<Int, Int>?) { hourlyReminderEndTime.value = endTime }
    fun setDailyReminderTimes(times: List<Pair<Int, Int>>) { dailyReminderTimes.value = times }

    fun getReminders(): ReminderData {
        return ReminderData(
            reminderEnabled = true, // Always true for schedule medications
            reminderFrequency = getReminderFrequency(),
            hourlyReminderInterval = hourlyReminderInterval.value,
            hourlyReminderStartTime = hourlyReminderStartTime.value,
            hourlyReminderEndTime = hourlyReminderEndTime.value,
            dailyReminderTimes = dailyReminderTimes.value,
        )
    }

    // Helper function to get the reminder frequency
    private fun getReminderFrequency(): String {
        return when (reminderFrequency.value) {
            EVERY_X_HOURS -> EVERY_X_HOURS
            else -> DAILY
        }
    }
}


