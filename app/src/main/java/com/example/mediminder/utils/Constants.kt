package com.example.mediminder.utils

object Constants {
    // Medication data
    const val MED_ID = "medicationId"
    const val LOG_ID = "logId"
    const val NEW_STATUS = "newStatus"
    const val MED_NAME = "medicationName"
    const val MED_NAME_DEFAULT = "Medication"
    const val DOSAGE = "dosage"

    // Schedule duration
    const val CONTINUOUS = "continuous"
    const val NUM_DAYS = "numDays"

    // Schedule frequency
    const val DAILY = "daily"
    const val SPECIFIC_DAYS = "specificDays"
    const val INTERVAL = "interval"

    // Reminder frequency
    const val EVERY_X_HOURS = "every x hours"
    const val X_TIMES_DAILY = "x times daily"

    // Nulls and empty strings
    const val EMPTY_STRING = ""
    const val SPACE = " "

    // Broadcasts
    const val MED_STATUS_CHANGED = "com.example.mediminder.MEDICATION_STATUS_CHANGED"
    const val SCHEDULE_NEW_MEDICATION = "com.example.mediminder.SCHEDULE_NEW_MEDICATION"
    const val SCHEDULE_DAILY_MEDICATIONS = "com.example.mediminder.SCHEDULE_DAILY_MEDICATIONS"
    const val MIDNIGHT_SCHEDULER = "midnight_scheduler"
    const val ALARM_PERMISSION_DENIED = "Permission to schedule exact alarms not granted"

    // Date and time patterns
    const val TIME_PATTERN = "h:mm a"
    const val DATE_PATTERN = "MMM d, yyyy"
    const val DATE_PATTERN_DAY = "EEE, MMM d"
    const val AM = "AM"
    const val PM = "PM"
    const val TIME_PICKER_TAG = "time_picker_dialog"
    const val DATE_PICKER_TAG = "date_picker_dialog"

    // Medication actions
    const val TAKE_MEDICATION = "take_medication"
    const val SKIP_MEDICATION = "skip_medication"

    // Channels
    const val MED_REMINDER_CHANNEL_ID = "medication_reminder_channel"
    const val MED_REMINDER_CHANNEL_NAME = "Medication Reminder"
    const val MED_REMINDER_CHANNEL_DESC = "Channel for medication reminders"

    // Preferences
    const val MED_PRIVACY = "show_medication_name"
}