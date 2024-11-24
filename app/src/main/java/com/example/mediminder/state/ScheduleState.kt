package com.example.mediminder.state

import com.example.mediminder.models.ScheduleData
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EMPTY_STRING
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

/**
 * Schedule state class for holding schedule details
 */
class ScheduleState {
    val isScheduled: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val startDate: MutableStateFlow<LocalDate?> = MutableStateFlow(null)

    val durationType: MutableStateFlow<String> = MutableStateFlow(CONTINUOUS)
    val numDays: MutableStateFlow<Int?> = MutableStateFlow(0)
    val scheduleType: MutableStateFlow<String> = MutableStateFlow(DAILY)
    val selectedDays: MutableStateFlow<String> = MutableStateFlow(EMPTY_STRING)
    val daysInterval: MutableStateFlow<Int?> = MutableStateFlow(0)

    fun setStartDate(date: LocalDate?) { startDate.value = date }
    fun setDurationType(type: String) { durationType.value = type }
    fun setNumDays(days: Int?) { numDays.value = days }
    fun setScheduleType(type: String) { scheduleType.value = type }

    fun setSelectedDays(days: String) {
        selectedDays.value = days
        daysInterval.value = 0
    }

    fun setDaysInterval(interval: Int?) {
        daysInterval.value = interval
        selectedDays.value = EMPTY_STRING
    }

    fun getSchedule(): ScheduleData {
        return ScheduleData(
            isScheduled = isScheduled.value,
            startDate = startDate.value,
            durationType = durationType.value,
            numDays = numDays.value,
            scheduleType = scheduleType.value,
            selectedDays = selectedDays.value,
            daysInterval = daysInterval.value
        )
    }
}