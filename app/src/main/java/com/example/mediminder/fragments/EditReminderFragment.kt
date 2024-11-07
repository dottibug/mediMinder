package com.example.mediminder.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.example.mediminder.viewmodels.BaseReminderViewModel
import kotlinx.coroutines.launch


class EditReminderFragment : BaseReminderFragment() {
    override val reminderViewModel: BaseReminderViewModel by activityViewModels()
    override val medicationViewModel: BaseMedicationViewModel by activityViewModels { BaseMedicationViewModel.Factory }
    private var isInitialSetup = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCurrentMedication()
    }

    private fun observeCurrentMedication() {
        viewLifecycleOwner.lifecycleScope.launch {
            medicationViewModel.currentMedication.collect { medication ->
                medication?.let { initReminders(it) }
            }
        }
    }

    private fun initReminders(medicationDetails: MedicationWithDetails) {
        isInitialSetup = true

        // Set reminder enabled
        medicationDetails.medication.let { med ->
            reminderViewModel.setReminderEnabled(med.reminderEnabled)

            // Clear all reminder settings if reminders are disabled
            if (!med.reminderEnabled) {
                reminderViewModel.clearReminderSettings()
            }
        }

        // Set reminder details if enabled
        if (medicationDetails.medication.reminderEnabled) {
            medicationDetails.reminders?.let { initReminderSettings(it) }
        }

        isInitialSetup = false
    }

    private fun initReminderSettings(reminders: MedReminders) {
        val reminderFrequency = reminders.reminderFrequency
        reminderViewModel.setReminderFrequency(reminderFrequency)
        binding.reminderFrequencyMenu.setText(reminderFrequency)

        when (reminderFrequency) {
            "every x hours" -> initHourlyReminderSettings(reminders)
            else -> initDailyReminderSettings(reminders)
        }
    }

    private fun initHourlyReminderSettings(reminders: MedReminders) {
        val interval = reminders.hourlyReminderInterval
        reminderViewModel.setHourlyReminderInterval(interval)

        val intervalResourceId = getIntervalResourceId(interval)
        binding.buttonHourlyRemindEvery.text = getString(intervalResourceId)

        reminders.hourlyReminderStartTime?.let { time ->
            reminderViewModel.setHourlyReminderStartTime(time.hour, time.minute)
            updateTimePickerButtonText(time.hour, time.minute, binding.buttonReminderStartTime)
        }
        reminders.hourlyReminderEndTime?.let { time ->
            reminderViewModel.setHourlyReminderEndTime(time.hour, time.minute)
            updateTimePickerButtonText(time.hour, time.minute, binding.buttonReminderEndTime)
        }
    }

    // Helper function to get the correct string resource ID for the reminder interval
    // Two possible formats (ex. "30 minutes" or "30") allow for values during initial setup or editing
    private fun getIntervalResourceId(interval: String?): Int {
        return when (interval) {
            "30 minutes", "30" -> R.string.hourly_30
            "1 hour", "1" -> R.string.hourly_1
            "2 hours", "2" -> R.string.hourly_2
            "3 hours", "3" -> R.string.hourly_3
            "4 hours", "4" -> R.string.hourly_4
            "5 hours", "5" -> R.string.hourly_5
            "6 hours", "6" -> R.string.hourly_6
            "7 hours", "7" -> R.string.hourly_7
            "8 hours", "8" -> R.string.hourly_8
            "9 hours", "9" -> R.string.hourly_9
            "10 hours", "10" -> R.string.hourly_10
            "11 hours", "11" -> R.string.hourly_11
            "12 hours", "12" -> R.string.hourly_12
            else -> R.string.hourly_30 // default to 30 minutes
        }
    }

    private fun initDailyReminderSettings(reminders: MedReminders) {
        // Convert LocalTime list to List<Pair<Int, Int>>
        val reminderTimes = reminders.dailyReminderTimes.map { time ->
            Pair(time.hour, time.minute)
        }
        reminderViewModel.setDailyReminderTimes(reminderTimes)
    }
}