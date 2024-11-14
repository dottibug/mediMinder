package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.activities.BaseActivity.Companion.CONTINUOUS
import com.example.mediminder.activities.BaseActivity.Companion.DAILY
import com.example.mediminder.activities.BaseActivity.Companion.INTERVAL
import com.example.mediminder.activities.BaseActivity.Companion.NUM_DAYS
import com.example.mediminder.activities.BaseActivity.Companion.SPECIFIC_DAYS
import com.example.mediminder.databinding.FragmentBaseScheduleBinding
import com.example.mediminder.utils.AppUtils.daysOfWeekString
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.example.mediminder.viewmodels.BaseScheduleViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


abstract class BaseScheduleFragment : Fragment() {
    protected lateinit var binding: FragmentBaseScheduleBinding
    protected open val scheduleViewModel: BaseScheduleViewModel by activityViewModels()
    protected abstract val medicationViewModel: BaseMedicationViewModel
    private var prevScheduleWasDaily: Boolean = true
    protected lateinit var selectedDays: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBaseScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInitialVisibility()
        setupListeners()
        setupObservers()
    }

    // Hide summary layouts
    private fun setupInitialVisibility() {
        binding.layoutNumDaysSummary.visibility = View.GONE
        binding.layoutDaySelectionSummary.visibility = View.GONE
        binding.layoutDaysIntervalSummary.visibility = View.GONE
    }

    // Listeners
    private fun setupListeners() {
//        binding.switchSchedule.setOnCheckedChangeListener { _, isChecked ->
//            scheduleViewModel.setScheduleEnabled(isChecked)
//            updateSwitchThumbTint(isChecked)
//        }

        binding.buttonMedStartDate.setOnClickListener { showDatePickerDialog() }
        binding.radioGroupAddMedDuration.setOnCheckedChangeListener { _, _ -> handleDurationSettings() }
        binding.radioGroupAddMedSchedule.setOnCheckedChangeListener { _, _ -> handleScheduleSettings() }
    }

//    private fun updateSwitchThumbTint(isChecked: Boolean) {
//        val thumbColor = if (isChecked) { requireContext().getColor(R.color.indigoDye) }
//        else { requireContext().getColor(R.color.cadetGray) }
//        binding.switchSchedule.thumbTintList = android.content.res.ColorStateList.valueOf(thumbColor)
//    }

    // Sets up observers for schedule data. Collects state flow in the scheduleViewModel and updates
    // the corresponding data in the addMedViewModel.
    private fun setupObservers() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            scheduleViewModel.isScheduleEnabled.collect { isEnabled ->
//                if (binding.switchSchedule.isChecked != isEnabled) {
//                    binding.switchSchedule.isChecked = isEnabled
//                }
//
//                if (isEnabled) {
//                    binding.layoutScheduleSettings.visibility = View.VISIBLE
//                    binding.asNeededMessage.visibility = View.GONE
//                } else {
//                    // If the schedule is disabled, reminders must also be disabled; which means
//                    // the medication will be an as-needed medication
//                    medicationViewModel.setAsNeeded(true)
//
//
////                    binding.layoutScheduleSettings.visibility = View.GONE
////                    binding.asNeededMessage.visibility = View.VISIBLE
//                    resetScheduleSettings()
//                }
//
//                medicationViewModel.updateIsScheduledMedication(isEnabled)
//            }
//        }

        // Start date observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.startDate.collect { date -> medicationViewModel.updateStartDate(date) }
        }

        // Duration type observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.durationType.collect { type -> medicationViewModel.updateDurationType(type) }
        }

        // Schedule type observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.scheduleType.collect { type -> medicationViewModel.updateScheduleType(type) }
        }

        // Num days observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.numDays.collect { numDays -> medicationViewModel.updateNumDays(numDays) }
        }

        // Selected days observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.selectedDays.collect { days -> medicationViewModel.updateSelectedDays(days) }
        }

        // Days interval observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.daysInterval.collect { interval -> medicationViewModel.updateDaysInterval(interval) }
        }
    }

    private fun resetScheduleSettings() {
        // Reset schedule switch (to default to enabled if user switches the as-needed toggle)
//        scheduleViewModel.setScheduleEnabled(true)
//        binding.switchSchedule.isChecked = true

        // Reset start date
        scheduleViewModel.setStartDate(null)
        updateDateButtonText(binding.buttonMedStartDate)

        // Reset duration type
        scheduleViewModel.setDurationType(CONTINUOUS)
        setDurationRadioToContinuous()
        scheduleViewModel.setNumDays(null)

        // Reset schedule type
        setScheduleTypeToDaily()
        scheduleViewModel.setSelectedDays("")
        scheduleViewModel.setDaysInterval("")
    }

    // Date Picker Dialog (selects current date by default)
    private fun showDatePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_start_date))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(parentFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            scheduleViewModel.setStartDate(selection)
            updateDateButtonText(binding.buttonMedStartDate)
        }
    }

    // Update the button text with the selected date
    protected fun updateDateButtonText(button: Button) {
        val date = scheduleViewModel.startDate.value

        if (date == null) {
            button.text = getString(R.string.select_start_date)
        } else {
            val formattedDate = DateTimeFormatter.ofPattern("MMM d, yyyy")
            button.text = date.format(formattedDate)
        }
    }

    // Medication duration settings
    protected open fun handleDurationSettings() {
        val isContinuous = binding.radioDurationContinuous.isChecked

        if (!isContinuous) { showDurationInputDialog() }

        else {
            scheduleViewModel.setDurationType(CONTINUOUS)
            setDurationRadioToContinuous()
        }
    }

    // Displays the duration input dialog to the user
    private fun showDurationInputDialog(editingNumDays: Boolean = false) {
        val durationDialog = DurationDialogFragment(this, editingNumDays)
        durationDialog.show(parentFragmentManager, DurationDialogFragment.TAG)
    }

    // Programmatically set the duration radio button to continuous
    fun setDurationRadioToContinuous() {
        updateDurationRadioSelection(CONTINUOUS)
        hideDurationNumDaysSummary()
    }

    // Set the number of days and update the number of days summary
    fun setDurationNumDays(numDays: String) {
        updateDurationRadioSelection(NUM_DAYS)
        updateDurationInScheduleViewModel(numDays)
        showDurationNumDaysSummary(numDays)
    }

    // Update the duration radio button selection
    protected fun updateDurationRadioSelection(checked: String) {
        if (checked == CONTINUOUS) {
            binding.radioDurationContinuous.isChecked = true
            binding.radioDurationNumDays.isChecked = false
        } else {
            binding.radioDurationContinuous.isChecked = false
            binding.radioDurationNumDays.isChecked = true
        }
    }

    // Show the number of days summary to the user
    protected fun showDurationNumDaysSummary(numDays: String) {
        binding.layoutNumDaysSummary.visibility = View.VISIBLE
        binding.textViewNumDaysSummaryValue.text = resources.getString(R.string.num_days_summary_value, numDays)
        binding.buttonEditNumDays.setOnClickListener { showDurationInputDialog(true) }
    }

    protected fun hideDurationNumDaysSummary() {
        binding.layoutNumDaysSummary.visibility = View.GONE
    }

    private fun updateDurationInScheduleViewModel(numDays: String) {
        scheduleViewModel.setDurationType(NUM_DAYS)
        scheduleViewModel.setNumDays(numDays.toInt())
    }

    // Medication schedule settings
    protected open fun handleScheduleSettings() {
        val isEveryDay = binding.radioDaysEveryDay.isChecked
        val isSpecificDays = binding.radioDaysSpecificDays.isChecked
        val isInterval = binding.radioDaysInterval.isChecked

        if (isEveryDay) {
            prevScheduleWasDaily = true
            hideScheduleSummaries()
            scheduleViewModel.setScheduleType(DAILY)
        }

        else if (isSpecificDays) {
            if (!prevScheduleWasDaily) { hideDaysIntervalSummary() }
            showDaySelectionDialog()
            scheduleViewModel.setScheduleType(SPECIFIC_DAYS)
        }

        else if (isInterval) {
            if (!prevScheduleWasDaily) { hideDaySelectionSummary() }
            showDaysIntervalDialog()
            scheduleViewModel.setScheduleType(INTERVAL)
        }
    }

    // Programmatically set the schedule radio button to daily
    fun setScheduleTypeToDaily() {
        binding.radioDaysEveryDay.isChecked = true
        binding.radioDaysSpecificDays.isChecked = false
        binding.radioDaysInterval.isChecked = false
        prevScheduleWasDaily = true
        hideScheduleSummaries()
        scheduleViewModel.setScheduleType(DAILY)
    }

    // Display the day selection dialog to the user
    private fun showDaySelectionDialog(editingDaySelection: Boolean = false) {
        hideDaysIntervalSummary()

        val daySelectionDialog: DaySelectionDialogFragment = if (editingDaySelection) {
            DaySelectionDialogFragment(this, true, selectedDays)
        } else {
            DaySelectionDialogFragment(this, false)
        }

        daySelectionDialog.show(parentFragmentManager, DaySelectionDialogFragment.TAG)
    }

    // Set the selected days and update the day selection summary
    fun updateSelectedDays(selectedDaysInt: String) {
        if (selectedDaysInt.isEmpty()) {
            setScheduleTypeToDaily()
            hideScheduleSummaries()
        } else {
            showDaySelectionSummary(selectedDaysInt)
            scheduleViewModel.setSelectedDays(selectedDaysInt)
        }
    }

    // Show the day selection summary to the user
    protected fun showDaySelectionSummary(selectedDaysInt: String) {
        val daysString = daysOfWeekString(selectedDaysInt)
        selectedDays = selectedDaysInt
        binding.layoutDaySelectionSummary.visibility = View.VISIBLE
        binding.textViewDaySelectionSummaryValue.text = daysString
        binding.buttonEditDaySelection.setOnClickListener{ showDaySelectionDialog(true) }
    }

    protected fun hideScheduleSummaries() {
        hideDaySelectionSummary()
        hideDaysIntervalSummary()
    }

    private fun hideDaySelectionSummary() {
        binding.layoutDaySelectionSummary.visibility = View.GONE
        selectedDays = ""
    }

    private fun hideDaysIntervalSummary() {
        binding.layoutDaysIntervalSummary.visibility = View.GONE
    }

    // Schedule interval settings
    private fun showDaysIntervalDialog(editingDaysInterval: Boolean = false) {
        hideDaySelectionSummary()
        val daysIntervalDialog = DaysIntervalDialogFragment(this, editingDaysInterval)
        daysIntervalDialog.show(parentFragmentManager, DaysIntervalDialogFragment.TAG)
    }

    // Set the days interval and update the days interval summary
    fun setDaysInterval(daysInterval: String) {
        scheduleViewModel.setDaysInterval(daysInterval)
        showDaysIntervalSummary(daysInterval)
    }

    protected fun showDaysIntervalSummary(daysInterval: String) {
        hideDaySelectionSummary()
        binding.layoutDaysIntervalSummary.visibility = View.VISIBLE
        binding.textViewDaysIntervalSummaryValue.text = resources.getString(R.string.days_interval_summary_value, daysInterval)
        binding.buttonEditDaysInterval.setOnClickListener { showDaysIntervalDialog(true) }
    }
}