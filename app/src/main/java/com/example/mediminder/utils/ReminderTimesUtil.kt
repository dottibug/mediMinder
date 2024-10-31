package com.example.mediminder.utils

import java.time.LocalTime

object ReminderTimesUtil {
    fun getHourlyReminderTimes(
        interval: String?,
        startTime: Pair<Int, Int>?,
        endTime: Pair<Int, Int>?
    ): List<LocalTime> {
        if (interval == null || startTime == null || endTime == null) return emptyList()

        // Parse interval to hours (extract first number from string like "2 hours")
        val intervalHours = interval.split(" ")[0].toIntOrNull()?.toLong() ?: return emptyList()

        val startLocalTime = LocalTime.of(startTime.first, startTime.second)
        val endLocalTime = LocalTime.of(endTime.first, endTime.second)

        val times = mutableListOf<LocalTime>()

        // Calculate how many intervals fit between start and end time
        val startMinutes = startLocalTime.hour * 60 + startLocalTime.minute
        val endMinutes = endLocalTime.hour * 60 + endLocalTime.minute
        val intervalMinutes = intervalHours * 60
        val numberOfIntervals = ((endMinutes - startMinutes) / intervalMinutes).toInt()

        // Generate times
        for (i in 0..numberOfIntervals) {
            val time = startLocalTime.plusHours(intervalHours * i)
            if (time.isAfter(endLocalTime)) break
            times.add(time)
        }

        return times
    }
}