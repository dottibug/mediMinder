package com.example.mediminder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentAddMedicationReminderBinding
import com.example.mediminder.utils.AppUtils
import com.example.mediminder.viewmodels.AddMedicationReminderViewModel
import com.example.mediminder.viewmodels.AddMedicationViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.util.Locale

class AddMedicationReminderFragment : Fragment() {
    private lateinit var binding: FragmentAddMedicationReminderBinding
    private val reminderViewModel: AddMedicationReminderViewModel by activityViewModels()
    private val addMedViewModel: AddMedicationViewModel by activityViewModels { AddMedicationViewModel.Factory }
    private val appUtils = AppUtils()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMedicationReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    // Set up listeners for UI components
    private fun setupListeners() {
        // "Set reminders" switch
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            reminderViewModel.setReminderEnabled(isChecked)
        }

        // "Reminder frequency" menu
        binding.reminderFrequencyMenu.setOnItemClickListener { _, _, position, _ ->
            val selectedFrequency = resources.getStringArray(R.array.reminder_frequency_options)[position]
            reminderViewModel.setReminderFrequency(selectedFrequency)
            if (selectedFrequency == "x times daily") resetDailyFrequencyButtonText()
        }

        // Click listeners
        binding.buttonHourlyRemindEvery.setOnClickListener { showHourlyReminderPopupMenu() }
        binding.buttonReminderStartTime.setOnClickListener { showTimePickerDialog("hourly") }
        binding.buttonAddDailyTimeReminder.setOnClickListener { addDailyTimePicker() }

        // "Reminder type" toggle button
        binding.toggleReminderType.addOnButtonCheckedListener { toggleButton, checkedId, _ ->
            val checkedButton = toggleButton.findViewById<Button>(checkedId)
            val reminderType = checkedButton.text.toString()
            reminderViewModel.setReminderType(reminderType)
        }
    }

    // Reset hourly frequency buttons to default values
    private fun resetDailyFrequencyButtonText() {
        binding.buttonHourlyRemindEvery.text = resources.getString(R.string.hourly_30)
        binding.buttonReminderStartTime.text = resources.getString(R.string.button_time_picker)
    }

    // Set up observers for LiveData
    private fun setupObservers() {
        // Dynamically render reminder components based on the "Set reminders" switch state
        viewLifecycleOwner.lifecycleScope.launch {
            reminderViewModel.isReminderEnabled.collect { isEnabled ->
                if (isEnabled) {
                    binding.layoutReminderSetup.visibility = View.VISIBLE
                    binding.hourlyReminderOptions.visibility = View.GONE
                    binding.dailyReminderOptions.visibility = View.GONE
                } else {
                    binding.layoutReminderSetup.visibility = View.GONE
                }
                Log.d("testcat", "Reminder enabled in observer: $isEnabled")
                addMedViewModel.updateIsReminderEnabled(isEnabled)
            }
        }

        // Dynamically render the frequency options based on the selected frequency
        viewLifecycleOwner.lifecycleScope.launch {
            reminderViewModel.reminderFrequency.collect { frequency ->
                showFrequencyOptions(frequency ?: "")
                addMedViewModel.updateReminderFrequency(frequency)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            reminderViewModel.hourlyReminderInterval.collect { interval ->
                addMedViewModel.updateHourlyReminderInterval(interval)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            reminderViewModel.hourlyReminderStartTime.collect { startTime ->
                addMedViewModel.updateHourlyReminderStartTime(startTime)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            reminderViewModel.dailyReminderTimes.collect { times ->
                updateDailyTimePickers(times)
                addMedViewModel.updateDailyReminderTimes(times)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            reminderViewModel.reminderType.collect { type ->
                addMedViewModel.updateReminderType(type)
            }
        }
    }

    // Add a new time picker button to the list of daily reminder times (defaults to 12:00 PM)
    private fun addDailyTimePicker() { reminderViewModel.addDailyReminderTime(12, 0) }

    private fun updateDailyTimePickers(times: List<Pair<Int, Int>>) {
        binding.dailyTimePickersContainer.removeAllViews()

        // Create a new time picker button for each time in the list (root is the dailyTimePickersContainer)
        times.forEachIndexed { index, time ->
            val timePickersContainer = layoutInflater.inflate(R.layout.daily_reminder_time_picker_item, binding.dailyTimePickersContainer, false)
            val timePickerButton = timePickersContainer.findViewById<Button>(R.id.buttonDailyReminderTimePicker)
            val deleteButton = timePickersContainer.findViewById<Button>(R.id.buttonDeleteDailyTimeReminder)

            // Set the time picker button text (time.first = hour, time.second = minute)
            updateTimePickerButtonText(time.first, time.second, timePickerButton)

            // Click listeners
            timePickerButton.setOnClickListener { showTimePickerDialog("daily", index) }
            deleteButton.setOnClickListener { reminderViewModel.removeDailyReminderTime(index) }

            // Add the time picker button to the container
            binding.dailyTimePickersContainer.addView(timePickersContainer)
        }
    }

    // Show the relevant frequency options based on the selected frequency
    private fun showFrequencyOptions(frequency: String) {
        binding.hourlyReminderOptions.visibility = if (frequency == "hourly") View.VISIBLE else View.GONE
        binding.dailyReminderOptions.visibility = if (frequency == "daily") View.VISIBLE else View.GONE
    }

    // Show hourly reminder popup menu
    // https://github.com/material-components/material-components-android/blob/master/docs/components/Menu.md
    private fun showHourlyReminderPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.buttonHourlyRemindEvery)
        popup.menuInflater.inflate(R.menu.hourly_reminder_popup_menu, popup.menu)

        // Update the view model and button text when a menu item is selected
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            binding.buttonHourlyRemindEvery.text = menuItem.title
            reminderViewModel.setHourlyReminderInterval(menuItem.title.toString())
            true
        }

        popup.show()
    }

    // https://github.com/material-components/material-components-android/blob/master/docs/components/TimePicker.md
    // Note: hour is 0 to 23 regardless of which time format you choose for the time picker
    // Shows a time picker dialog to the user
    // If the reminderType is "daily" and the index is -1, then a new time picker button will be
    // added. If the index is >= 0, then the time picker button at that index will be updated
    private fun showTimePickerDialog(reminderType: String, index: Int = -1) {
        // Build the time picker dialog
        val timePicker = MaterialTimePicker.Builder()
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Start Time")
            .build()

        timePicker.show(parentFragmentManager, "tag")

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            when (reminderType) {
                "hourly" -> {
                    updateTimePickerButtonText(hour, minute, binding.buttonReminderStartTime)
                    reminderViewModel.setHourlyReminderStartTime(hour, minute)
                }

                "daily" -> {
                    if (index >= 0) { reminderViewModel.updateDailyReminderTime(index, hour, minute) }
                    else { reminderViewModel.addDailyReminderTime(hour, minute) }
                }
            }
        }
    }

    // Update the time picker button text
    private fun updateTimePickerButtonText(hour: Int, minute: Int, button: Button) {
        val convertedHour = appUtils.convert24HourTo12Hour(hour)
        val amPm = if (hour < 12) "AM" else "PM"
        val formattedTime = String.format(Locale.CANADA, "%1d:%02d %s", convertedHour, minute, amPm)
        button.text = formattedTime
    }
}