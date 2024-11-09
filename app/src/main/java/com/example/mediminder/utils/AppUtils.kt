package com.example.mediminder.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediminder.R
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.MedicationStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object AppUtils {

    fun getLocalTimeFromPair(pair: Pair<Int, Int>?): LocalTime? {
        return pair?.let { LocalTime.of(it.first, it.second) }
    }

    // Convert 24-hour digit to 12-hour digit (ex. 13 -> 1)
    fun convert24HourTo12Hour(hour: Int): Int {
        if (hour == 0) return 12
        else if (hour > 12) return hour - 12
        else return hour
    }

    fun formatLocalTimeTo12Hour(localTime: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        return localTime.format(formatter).lowercase()
    }

    // Format LocalDate as Jan 1, 2025
    fun formatToLongDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
        return localDate.format(formatter)
    }

    // Get a list of day names from a string of day numbers (ex. "1,3,5" -> ["Monday", "Wednesday", "Friday"])
    fun getDayNames(days: String): List<String> {
        return days
            .split(",")
            .map { DayOfWeek.of(it.toInt()).getDisplayName(TextStyle.FULL, Locale.ENGLISH) }
    }

    // Get a string of day names from a string of day numbers (ex. "1,3,5" -> "Monday, Wednesday, and Friday")
    fun daysOfWeekString(days: String): String {
        val dayNames = getDayNames(days)

        return when (dayNames.size) {
            1 -> dayNames[0]
            2 -> "${dayNames[0]} and ${dayNames[1]}"
            else -> dayNames.dropLast(1).joinToString(", ") + ", and ${dayNames.last()}"
        }
    }

    // Get the resource id associated with a MedicationStatus
    fun getStatusIcon(status: MedicationStatus): Int {
        return when (status) {
            MedicationStatus.PENDING -> R.drawable.pending
            MedicationStatus.TAKEN -> R.drawable.taken
            MedicationStatus.SKIPPED -> R.drawable.skipped
            MedicationStatus.MISSED -> R.drawable.missed
            MedicationStatus.UNSCHEDULED -> R.drawable.unscheduled
        }
    }

    // Get a list of LocalTime objects representing the reminder times for the given interval and time range
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

    // Create medication repository for a view model factory
    fun createMedicationRepository(database: AppDatabase): MedicationRepository {
        return MedicationRepository(
            database.medicationDao(),
            database.dosageDao(),
            database.remindersDao(),
            database.scheduleDao(),
            database.medicationLogDao(),
        )
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

    fun isScheduledForDate(schedule: Schedules?, date: LocalDate): Boolean {
        if (schedule == null) { return false }
        if (!dateWithinMedicationDuration(date, schedule)) { return false }

        return when (schedule.scheduleType) {
            "daily" -> true
            "specificDays" -> isScheduledForDayOfWeek(schedule, date)
            "interval" -> isScheduledForInterval(schedule, date)
            else -> false
        }
    }

    private fun isScheduledForDayOfWeek(schedule: Schedules, date: LocalDate): Boolean {
        return schedule.selectedDays
            .split(",")
            .contains(date.dayOfWeek.value.toString())
    }

    // Checks if the number of days between the start date and current date is evenly
    // divisible by the interval; if it is, the medication is scheduled for the current day
    // Ex. Interval = 3
    // Day 0 (start date): 0 % 3 == 0, medication is scheduled
    // Day 1: 1 % 3 == 1, medication is not scheduled
    // Day 2: 2 % 3 == 2, medication is not scheduled
    // Day 3: 3 % 3 == 0, medication is scheduled
    // Day 4: 4 % 3 == 1, medication is not scheduled, etc
    private fun isScheduledForInterval(schedule: Schedules, date: LocalDate): Boolean {
        val daysSinceStart = ChronoUnit.DAYS.between(schedule.startDate, date).toInt()
        return daysSinceStart % (schedule.daysInterval ?: 1) == 0
    }

    fun setupWindowInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}