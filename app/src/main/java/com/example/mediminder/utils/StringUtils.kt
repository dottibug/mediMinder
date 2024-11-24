package com.example.mediminder.utils

import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.utils.AppUtils.daysOfWeekString
import com.example.mediminder.utils.AppUtils.formatLocalTimeTo12Hour
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.INTERVAL
import com.example.mediminder.utils.Constants.NUM_DAYS
import com.example.mediminder.utils.Constants.SPECIFIC_DAYS
import com.example.mediminder.utils.Constants.TIME_PATTERN
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Object class with helper functions for formatting strings
 */
object StringUtils {
    // Helper function to format daily reminders into a string
    fun getDailyReminderString(remindersList: List<LocalTime>): String {
        val formatter = DateTimeFormatter.ofPattern(TIME_PATTERN, Locale.ENGLISH)
        val times = remindersList.map { it.format(formatter).lowercase() }

        val timeString = when (times.size) {
            1 -> times[0]
            2 -> "${times[0]} and ${times[1]}"
            else -> times.dropLast(1).joinToString(", ") + ", and ${times.last()}"
        }
        return "Daily reminders at $timeString"
    }

    // Helper function to format hourly reminders into a string
    fun getHourlyReminderString(interval: String, startTime: LocalTime, endTime: LocalTime): String {
        val startTimeString = formatLocalTimeTo12Hour(startTime)
        val endTimeString = formatLocalTimeTo12Hour(endTime)
        return "Hourly reminders every $interval from $startTimeString to $endTimeString"
    }

    // Helper function to format schedule into a string
    fun getScheduleString(schedule: Schedules, asNeeded: Boolean): String {
        val scheduleTypeString = getScheduleTypeString(schedule.scheduleType, asNeeded, schedule)
        val durationString = getDurationString(schedule)
        return "Take $scheduleTypeString $durationString"
    }

    // Helper function to format schedule type into a string
    private fun getScheduleTypeString(scheduleType: String, asNeeded: Boolean, schedule: Schedules): String {
        return when (scheduleType) {
            DAILY -> if (asNeeded) "as needed" else DAILY
            SPECIFIC_DAYS -> "every " + daysOfWeekString(schedule.selectedDays)
            INTERVAL -> getDaysIntervalString(schedule.daysInterval)
            else -> ""
        }
    }

    // Helper function to format duration type into a string
    private fun getDurationString(schedule: Schedules): String {
        return when (schedule.durationType) {
            NUM_DAYS -> "for ${schedule.numDays.toString()} days"
            else -> EMPTY_STRING
        }
    }

    // Helper function to format days interval into a string
    private fun getDaysIntervalString(daysInterval: Int?): String {
        return when (daysInterval) {
            1 -> "every day"
            else -> "every ${daysInterval.toString()} days"
        }
    }
}