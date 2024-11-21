package com.example.mediminder.utils

object Constants {
    // Medication data
    const val MED_ID = "medicationId"
    const val LOG_ID = "logId"
    const val NEW_STATUS = "newStatus"
    const val MED_NAME = "medicationName"
    const val MED_NAME_DEFAULT = "Medication"
    const val DOSAGE = "dosage"
    const val DOSAGE_DEFAULT_UNIT = "units"

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
    const val ADD_AS_NEEDED = "ADD_AS_NEEDED"

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
    const val MED_PRIVACY = "medication_privacy"

    // Error messages
    const val ERR_FETCHING_MED = "Error fetching medication."
    const val ERR_FETCHING_MEDS = "Error fetching medications."
    const val ERR_FETCHING_AS_NEEDED_MEDS = "Error fetching medications."
    const val ERR_ADDING_MED = "Error adding medication."
    const val ERR_ADDING_AS_NEEDED_LOG = "Error adding as-needed log."
    const val ERR_INSERTING_MED = "Error inserting medication."
    const val ERR_INSERTING_DOSAGE = "Error inserting dosage."
    const val ERR_INVALID_TIME_PAIR = "Invalid time pair."
    const val ERR_INSERTING_REMINDER = "Error inserting reminder."
    const val ERR_INSERTING_SCHEDULE = "Error inserting schedule."
    const val ERR_UPDATING_MED = "Error updating medication."
    const val ERR_UPDATING_MED_INFO = "Error updating medication info."
    const val ERR_UPDATING_DOSAGE = "Error updating dosage."
    const val ERR_UPDATING_REMINDER = "Error updating reminder."
    const val ERR_UPDATING_SCHEDULE = "Error updating schedule."
    const val ERR_SCHEDULE_NOT_FOUND = "Scheduled medication not found."
    const val ERR_DELETING_MED = "Error deleting medication."
    const val ERR_FETCHING_MED_HISTORY = "Error fetching medication history."
    const val ERR_SETTING_STATUS = "Error setting medication status."
    const val ERR_UPDATING_STATUS = "Error updating medication status."
    const val ERR_VALIDATING_INPUT = "Error validating input."

    const val ERR_FETCHING_MED_USER = "Error fetching medication. Please try again."
    const val ERR_ADDING_MED_USER = "Error adding medication. Please try again."
    const val ERR_UPDATING_MED_USER = "Error updating medication. Please try again."
    const val ERR_DELETING_MED_USER = "Error deleting medication. Please try again."
    const val ERR_FETCHING_MED_HISTORY_USER = "Error fetching medication history. Please try again."
    const val ERR_UPDATING_STATUS_USER = "Error updating medication status. Please try again."

    const val ERR_UNEXPECTED = "Unexpected error occurred."
    const val ERR_SEEDING_DB = "Error seeding database."
    const val ERR_CLEARING_DB = "Error clearing database."
}