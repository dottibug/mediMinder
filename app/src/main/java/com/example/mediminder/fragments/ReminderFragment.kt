package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentReminderBinding
import com.example.mediminder.utils.AppUtils.createTimePicker
import com.example.mediminder.utils.AppUtils.updateTimePickerButtonText
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EVERY_X_HOURS
import com.example.mediminder.utils.Constants.TIME_PICKER_TAG
import com.example.mediminder.utils.Constants.X_TIMES_DAILY
import com.example.mediminder.viewmodels.AppViewModel
import com.example.mediminder.viewmodels.ReminderViewModel
import kotlinx.coroutines.launch

/**
 * Base fragment for adding or editing a medication's reminder settings.
 */
open class ReminderFragment : Fragment() {
    protected lateinit var binding: FragmentReminderBinding
    protected val appViewModel: AppViewModel by activityViewModels { AppViewModel.Factory }
    protected val reminderViewModel: ReminderViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDefaultFrequency()
        setupListeners()
        setupObservers()
    }

    private fun setDefaultFrequency() {
        reminderViewModel.setReminderFrequency(EVERY_X_HOURS)
        binding.reminderFrequencyMenu.setText(EVERY_X_HOURS, false)
        showFrequencyOptions(EVERY_X_HOURS)
    }

    protected open fun setupListeners() {
        with (binding) {
            // Frequency menu
            reminderFrequencyMenu.setOnItemClickListener { _, _, position, _ ->
                val selectedFrequency = resources.getStringArray(R.array.reminder_frequency_options)[position]
                reminderViewModel.setReminderFrequency(selectedFrequency)
                if (selectedFrequency == X_TIMES_DAILY) resetDailyFrequencyButtonText()
            }

            // Click listeners
            buttonHourlyRemindEvery.setOnClickListener { showHourlyReminderPopupMenu() }
            buttonReminderStartTime.setOnClickListener { showTimePickerDialog(EVERY_X_HOURS) }
            buttonReminderEndTime.setOnClickListener { showTimePickerDialog(EVERY_X_HOURS, isEndTime = true) }
            buttonAddDailyTimeReminder.setOnClickListener { addDailyTimePicker() }
        }
    }

    /**
     * Reset the hourly frequency buttons to their default values
     */
    private fun resetDailyFrequencyButtonText() {
        with (binding) {
            buttonHourlyRemindEvery.text = resources.getString(R.string.hourly_30)
            buttonReminderStartTime.text = resources.getString(R.string.select_time)
            buttonReminderEndTime.text = resources.getString(R.string.select_time)
        }
    }

    /**
     * Collect state flow from reminder view model when the fragment is in the STARTED state
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectReminderFrequency() }
                launch { collectHourlyReminderInterval() }
                launch { collectHourlyReminderStartTime() }
                launch { collectHourlyReminderEndTime() }
                launch { collectDailyReminderTimes() }
            }
        }
    }

    /**
     * Collect reminder frequency from reminder view model
     */
    private suspend fun collectReminderFrequency() {
        reminderViewModel.reminderFrequency.collect { frequency ->
            showFrequencyOptions(frequency ?: EVERY_X_HOURS)
            appViewModel.reminder.setReminderFrequency(frequency)
            binding.reminderFrequencyMenu.setText(frequency, false)
        }
    }

    /**
     * Collect hourly reminder interval from reminder view model
     */
    private suspend fun collectHourlyReminderInterval() {
        reminderViewModel.hourlyReminderInterval.collect { interval ->
            appViewModel.reminder.setHourlyReminderInterval(interval)
        }
    }

    /**
     * Collect hourly reminder start time from reminder view model
     */
    private suspend fun collectHourlyReminderStartTime() {
        reminderViewModel.hourlyReminderStartTime.collect { startTime ->
            appViewModel.reminder.setHourlyReminderStartTime(startTime)
        }
    }

    /**
     * Collect hourly reminder end time from reminder view model
     */
    private suspend fun collectHourlyReminderEndTime() {
        reminderViewModel.hourlyReminderEndTime.collect { endTime ->
            appViewModel.reminder.setHourlyReminderEndTime(endTime)
        }
    }

    /**
     * Collect daily reminder times from reminder view model
     */
    private suspend fun collectDailyReminderTimes() {
        reminderViewModel.dailyReminderTimes.collect { times ->
            updateDailyTimePickers(times)
            appViewModel.reminder.setDailyReminderTimes(times)
        }
    }

    /**
     * Add a new time picker button to the list of daily reminder times (defaults to 12:00 PM)
     */
    private fun addDailyTimePicker() { reminderViewModel.addDailyReminderTime(12, 0) }

    /**
     * Update the time picker buttons in the list of daily reminder times
     */
    private fun updateDailyTimePickers(times: List<Pair<Int, Int>>) {
        binding.dailyTimePickersContainer.removeAllViews()

        // Create a new time picker button for each time in the list (root is the dailyTimePickersContainer)
        times.forEachIndexed { index, time ->
            val timePickersContainer = layoutInflater.inflate(R.layout.item_daily_reminder_time_picker, binding.dailyTimePickersContainer, false)
            val timePickerButton = timePickersContainer.findViewById<Button>(R.id.buttonDailyReminderTimePicker)
            val deleteButton = timePickersContainer.findViewById<Button>(R.id.buttonDeleteDailyTimeReminder)

            // Set the time picker button text (time.first = hour, time.second = minute)
            updateTimePickerButtonText(time.first, time.second, timePickerButton)

            // Click listeners
            timePickerButton.setOnClickListener { showTimePickerDialog(DAILY, index) }
            deleteButton.setOnClickListener { reminderViewModel.removeDailyReminderTime(index) }

            // Add the time picker button to the container
            binding.dailyTimePickersContainer.addView(timePickersContainer)
        }
    }

    /**
     * Show the relevant frequency options based on the selected frequency
     */
    private fun showFrequencyOptions(frequency: String) {
        with (binding) {
            hourlyReminderOptions.visibility = if (frequency == EVERY_X_HOURS) View.VISIBLE else View.GONE
            dailyReminderOptions.visibility = if (frequency == X_TIMES_DAILY) View.VISIBLE else View.GONE
            // Programmatically add a daily reminder time of 12:00 PM if there are no times
            if (frequency == X_TIMES_DAILY && reminderViewModel.dailyReminderTimes.value.isEmpty()) {
                addDailyTimePicker()
            }
        }
    }

    /**
     * Show the hourly reminder popup menu
     */
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

    /**
     * Show the time picker dialog to the user
     * Note: hour is 0 to 23 regardless of which time format you choose for the time picker
     * If the reminderType is "daily" and the index is -1, then a new time picker button will be
     * added. If the index is >= 0, then the time picker button at that index will be updated
     */
    private fun showTimePickerDialog(reminderType: String, index: Int = -1, isEndTime: Boolean = false) {
        val titleText = if (isEndTime) SELECT_END_TIME else SELECT_START_TIME
        val timePicker = createTimePicker(titleText)

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            when (reminderType) {
                EVERY_X_HOURS -> setReminderTimes(isEndTime, hour, minute)
                DAILY -> setDailyReminderTimes(index, hour, minute)
            }
        }

        timePicker.show(parentFragmentManager, TIME_PICKER_TAG)
    }

    /**
     * Update the reminder times in the view model based on the selected time
     */
    private fun setReminderTimes(isEndTime: Boolean, hour: Int, minute: Int) {
        if (isEndTime) {
            updateTimePickerButtonText(hour, minute, binding.buttonReminderEndTime)
            reminderViewModel.setHourlyReminderEndTime(hour, minute)
        } else {
            updateTimePickerButtonText(hour, minute, binding.buttonReminderStartTime)
            reminderViewModel.setHourlyReminderStartTime(hour, minute)
        }
    }

    /**
     * Update the daily reminder times in the view model based on the selected time
     */
    private fun setDailyReminderTimes(index: Int, hour: Int, minute: Int) {
        if (index >= 0) { reminderViewModel.updateDailyReminderTime(index, hour, minute) }
        else { reminderViewModel.addDailyReminderTime(hour, minute) }
    }

    companion object {
        private const val SELECT_END_TIME = "Select end time"
        private const val SELECT_START_TIME  = "Select start time"
    }
}