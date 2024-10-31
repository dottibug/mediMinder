package com.example.mediminder.utils

import com.example.mediminder.data.local.classes.Schedules
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object MedScheduledForDateUtil {
    fun isScheduledForDate(schedule: Schedules?, date: LocalDate): Boolean {
        if (schedule == null) return false
        if (!dateWithinMedicationDuration(date, schedule)) return false

        return when (schedule.scheduleType) {
            "daily" -> true

            "specificDays" -> {
                schedule.selectedDays.split(",").contains(date.dayOfWeek.value.toString())
            }

            // Checks if the number of days between the start date and current date is evenly
            // divisible by the interval; if it is, the medication is scheduled for the current day
            // Ex. Interval = 3
            // Day 0 (start date): 0 % 3 == 0, medication is scheduled
            // Day 1: 1 % 3 == 1, medication is not scheduled
            // Day 2: 2 % 3 == 2, medication is not scheduled
            // Day 3: 3 % 3 == 0, medication is scheduled
            // Day 4: 4 % 3 == 1, medication is not scheduled, etc
            "interval" -> {
                val daysSinceStart = ChronoUnit.DAYS.between(schedule.startDate, date).toInt()
                daysSinceStart % (schedule.daysInterval ?: 1) == 0
            }

            else -> false
        }

    }

    // Checks if the medication is within the scheduled number of days from the start date
    private fun dateWithinMedicationDuration(date: LocalDate, schedule: Schedules): Boolean {
        if (date < schedule.startDate) return false

        return when (schedule.durationType) {
            "continuous" -> true
            "numDays" -> date <= schedule.startDate.plusDays(schedule.numDays?.toLong() ?: 0)
            else -> false
        }
    }
}