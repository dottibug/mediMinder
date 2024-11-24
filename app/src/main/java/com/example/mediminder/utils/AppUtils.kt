package com.example.mediminder.utils

import android.app.Activity
import android.content.Context
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED
import com.example.mediminder.R
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.utils.Constants.AM
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.DATE_PATTERN
import com.example.mediminder.utils.Constants.INTERVAL
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.Constants.NUM_DAYS
import com.example.mediminder.utils.Constants.PM
import com.example.mediminder.utils.Constants.SPACE
import com.example.mediminder.utils.Constants.SPECIFIC_DAYS
import com.example.mediminder.utils.Constants.TIME_PATTERN
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Object class for utility functions
 */
object AppUtils {

    fun cancelActivity(activity: Activity) {
        activity.setResult(RESULT_CANCELED)
        activity.finish()
    }

    // Check if medication ID exists from the intent.
    // Returns the id if it exists; otherwise finishes the activity
    fun getMedicationId(activity: Activity): Long? {
        val medicationId = activity.intent.getLongExtra(MED_ID, -1L)
        if (medicationId == -1L) {
            activity.finish()
            return null
        }
        return medicationId
    }

    // Create and show a toast message (cancels previous toast, if one exists
    private var currentToast: Toast? = null
    fun createToast(context: Context, message: String) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    // Create LocalTime from a pair of Ints
    fun getLocalTimeFromPair(pair: Pair<Int, Int>?): LocalTime? {
        return pair?.let { LocalTime.of(it.first, it.second) }
    }

    // Convert 24-hour digit to 12-hour digit (ex. 13 -> 1)
    private fun convert24HourTo12Hour(hour: Int): Int {
        if (hour == 0) return 12
        else if (hour > 12) return hour - 12
        else return hour
    }

    // Format LocalTime as 1:00 pm
    fun formatLocalTimeTo12Hour(localTime: LocalTime): String {
        val formatter = DateTimeFormatter.ofPattern(TIME_PATTERN, Locale.ENGLISH)
        return localTime.format(formatter).lowercase()
    }

    // Format LocalDate as Jan 1, 2025
    fun formatToLongDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.ENGLISH)
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
            MedicationStatus.PENDING -> R.drawable.alarm_clock
            MedicationStatus.TAKEN -> R.drawable.taken
            MedicationStatus.SKIPPED -> R.drawable.skip
            MedicationStatus.MISSED -> R.drawable.warning
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
        val intervalHours = interval.split(SPACE)[0].toIntOrNull()?.toLong() ?: return emptyList()
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
            CONTINUOUS -> true
            NUM_DAYS -> date <= schedule.startDate.plusDays(schedule.numDays?.toLong() ?: 0)
            else -> false
        }
    }

    // Checks if the medication is scheduled for the given date based on the schedule type
    fun isScheduledForDate(schedule: Schedules?, date: LocalDate): Boolean {
        if (schedule == null) {
            return false
        }
        if (!dateWithinMedicationDuration(date, schedule)) {
            return false
        }

        return when (schedule.scheduleType) {
            DAILY -> true
            SPECIFIC_DAYS -> isScheduledForDayOfWeek(schedule, date)
            INTERVAL -> isScheduledForInterval(schedule, date)
            else -> false
        }
    }

    // Checks if the medication is scheduled for a certain day of the week
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

    // Create time picker
    fun createTimePicker(title: String): MaterialTimePicker {
        val timePicker = MaterialTimePicker.Builder()
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText(title)
            .build()

        return timePicker
    }

    // Update text of button that triggered time picker dialog
    fun updateTimePickerButtonText(hour: Int, minute: Int, button: Button) {
        val convertedHour = convert24HourTo12Hour(hour)
        val amPm = if (hour < 12) AM else PM
        val formattedTime = String.format(Locale.CANADA, "%1d:%02d %s", convertedHour, minute, amPm)
        button.text = formattedTime
    }

    // Create date picker
    fun createDatePicker(title: String): MaterialDatePicker<Long> {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        return datePicker
    }

    // Update text of button that triggered date picker dialog
    fun updateDatePickerButtonText(date: LocalDate?, button: Button) {
        val formattedDate = DateTimeFormatter.ofPattern(DATE_PATTERN)
        button.text = date?.format(formattedDate)
    }
}