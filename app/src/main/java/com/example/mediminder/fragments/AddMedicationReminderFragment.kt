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
import androidx.fragment.app.viewModels
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentAddMedicationReminderBinding
import com.example.mediminder.utils.AppUtils
import com.example.mediminder.viewmodels.AddMedicationReminderViewModel
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import java.util.Locale

class AddMedicationReminderFragment : Fragment() {

    private lateinit var binding: FragmentAddMedicationReminderBinding
    private val viewModel: AddMedicationReminderViewModel by viewModels()
    private val appUtils = AppUtils()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
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
            viewModel.setReminderEnabled(isChecked)
        }

        // "Reminder frequency" menu
        binding.reminderFrequencyMenu.setOnItemClickListener { _, _, position, _ ->
            val selectedFrequency = resources.getStringArray(R.array.reminder_frequency_options)[position]
            viewModel.setReminderFrequency(selectedFrequency) // Update the ViewModel with the selected frequency
        }

        // "Hourly reminder" menu button
        binding.buttonHourlyRemindEvery.setOnClickListener { showHourlyReminderPopupMenu() }

        // Time picker buttons
        binding.buttonReminderStartTime.setOnClickListener { showTimePickerDialog("hourly") }
//        binding.buttonDailyReminderTimePicker.setOnClickListener { showTimePickerDialog("daily") }

        // "Add reminder" button
        binding.buttonAddDailyTimeReminder.setOnClickListener { addDailyTimePicker() }
    }

    // Set up observers for LiveData
    private fun setupObservers() {
        // Dynamically render reminder components based on the "Set reminders" switch state
        viewModel.isReminderEnabled.observe(viewLifecycleOwner) { isEnabled ->
            if (isEnabled) {
                binding.layoutReminderSetup.visibility = View.VISIBLE
                binding.hourlyReminderOptions.visibility = View.GONE
                binding.dailyReminderOptions.visibility = View.GONE
            }
            else { binding.layoutReminderSetup.visibility = View.GONE }
        }

        // Dynamically render the frequency options based on the selected frequency
        viewModel.reminderFrequency.observe(viewLifecycleOwner) { frequency ->
            showFrequencyOptions(frequency)
        }

        // Observe the list of daily reminder times (to update the UI accordingly)
        viewModel.dailyReminderTimes.observe(viewLifecycleOwner) { times ->
            updateDailyTimePickers(times)
        }
    }

    // Add new default time to the list of daily reminder times (the UI will update with a new
    // time picker button for the user)
    private fun addDailyTimePicker() {
        viewModel.addDailyReminderTime(12, 0) // Default 12:00 PM
    }

    private fun updateDailyTimePickers(times: List<Pair<Int, Int>>) {
        binding.dailyTimePickersContainer.removeAllViews()

        // Create a new time picker button for each time in the list (root is the dailyTimePickersContainer)
        times.forEachIndexed { index, time ->
            val timePickersContainer = layoutInflater.inflate(R.layout.daily_reminder_time_picker_item, binding.dailyTimePickersContainer, false)
            val timePickerButton = timePickersContainer.findViewById<Button>(R.id.buttonDailyReminderTimePicker)
            val deleteButton = timePickersContainer.findViewById<Button>(R.id.buttonDeleteDailyTimeReminder)

            // Set the time picker button text (time.first = hour, time.second = minute)
            updateTimePickerButtonText(time.first, time.second, timePickerButton)

            // Set click listeners for the time picker button and delete button
            timePickerButton.setOnClickListener { showTimePickerDialog("daily", index) }
            deleteButton.setOnClickListener { viewModel.removeDailyReminderTime(index) }

            // Add the time picker button to the container
            binding.dailyTimePickersContainer.addView(timePickersContainer)
        }
    }

    // Show the relevant frequency options based on the selected frequency
    private fun showFrequencyOptions(frequency: String) {
        binding.hourlyReminderOptions.visibility = if (frequency == "every x hours") View.VISIBLE else View.GONE
        binding.dailyReminderOptions.visibility = if (frequency == "x times daily") View.VISIBLE else View.GONE
    }

    // Show hourly reminder popup menu
    // https://github.com/material-components/material-components-android/blob/master/docs/components/Menu.md
    private fun showHourlyReminderPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.buttonHourlyRemindEvery)
        popup.menuInflater.inflate(R.menu.hourly_reminder_popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            // Set button text to the selected menu item
            binding.buttonHourlyRemindEvery.text = menuItem.title
            // Update the ViewModel with the selected interval
            viewModel.setHourlyReminderInterval(menuItem.title.toString())
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

        // Show the time picker dialog
        timePicker.show(parentFragmentManager, "tag")

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            when (reminderType) {
                "hourly" -> {
                    // Update the UI with the selected time
                    updateTimePickerButtonText(hour, minute, binding.buttonReminderStartTime)
                    // Update the ViewModel with the selected time
                    viewModel.setHourlyReminderStartTime(hour, minute)
            }
                "daily" -> {
                    Log.i("testcat", "time picker dialog: $hour, minute: $minute")
                    Log.i("testcat", "index: $index")
                    if (index >= 0) {
                        viewModel.updateDailyReminderTime(index, hour, minute)
                    }

                    else { viewModel.addDailyReminderTime(hour, minute) }

                }
            }
        }
    }

    // Update the time picker button text
    private fun updateTimePickerButtonText(hour: Int, minute: Int, button: Button) {
        Log.i("testcat", "button text should update")


        // Convert hour to 12-hour format
        val convertedHour = appUtils.convert24HourTo12Hour(hour)

        // Get the AM/PM indicator
        val amPm = if (hour < 12) "AM" else "PM"

        // Create the formatted string: "12:00 AM" or "5:00 PM"
        val formattedTime = String.format(Locale.CANADA, "%1d:%02d %s", convertedHour, minute, amPm)

        Log.i("testcat", "formatted time: $formattedTime")

        button.text = formattedTime
    }
}