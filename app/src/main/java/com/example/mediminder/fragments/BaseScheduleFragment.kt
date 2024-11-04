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
import com.example.mediminder.databinding.FragmentBaseScheduleBinding
import com.example.mediminder.utils.AppUtils
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.example.mediminder.viewmodels.BaseScheduleViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


abstract class BaseScheduleFragment : Fragment() {

    protected lateinit var binding: FragmentBaseScheduleBinding
    protected val scheduleViewModel: BaseScheduleViewModel by activityViewModels()
    protected abstract val medicationViewModel: BaseMedicationViewModel
    protected val appUtils = AppUtils()
    protected var prevScheduleWasDaily: Boolean = true
    protected lateinit var selectedDays: List<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
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
    protected fun setupInitialVisibility() {
        binding.layoutNumDaysSummary.visibility = View.GONE
        binding.layoutDaySelectionSummary.visibility = View.GONE
        binding.layoutDaysIntervalSummary.visibility = View.GONE
    }

    // Listeners
    protected fun setupListeners() {
        binding.buttonMedStartDate.setOnClickListener { showDatePickerDialog() }
        binding.radioGroupAddMedDuration.setOnCheckedChangeListener { _, _ -> handleDurationSettings() }
        binding.radioGroupAddMedSchedule.setOnCheckedChangeListener { _, _ -> handleScheduleSettings() }
    }

    // Sets up observers for schedule data. Collects state flow in the scheduleViewModel and updates
    // the corresponding data in the addMedViewModel.
    protected fun setupObservers() {
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

    // Date Picker Dialog (selects current date by default)
    protected fun showDatePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select start date")
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
        val formattedDate = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        button.text = date?.format(formattedDate) ?: "Select a date"
    }

    // Medication duration settings
    protected open fun handleDurationSettings() {
        val isContinuous = binding.radioDurationContinuous.isChecked

        if (!isContinuous) { showDurationInputDialog() }

        else {
            scheduleViewModel.setDurationType("continuous")
            setDurationRadioToContinuous()
        }
    }

    // Displays the duration input dialog to the user
    protected fun showDurationInputDialog(editingNumDays: Boolean = false) {
        val durationDialog = DurationDialogFragment(this, editingNumDays)
        durationDialog.show(parentFragmentManager, DurationDialogFragment.TAG)
    }

    // Programmatically set the duration radio button to continuous
    fun setDurationRadioToContinuous() {
        updateDurationRadioSelection("continuous")
        hideDurationNumDaysSummary()
    }

    // Set the number of days and update the number of days summary
    fun setDurationNumDays(numDays: String) {
        updateDurationRadioSelection("numDays")
        updateDurationInScheduleViewModel(numDays)
        showDurationNumDaysSummary(numDays)
    }

    // Update the duration radio button selection
    protected fun updateDurationRadioSelection(checked: String) {
        if (checked == "continuous") {
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

    protected fun updateDurationInScheduleViewModel(numDays: String) {
        scheduleViewModel.setDurationType("numDays")
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
            scheduleViewModel.setScheduleType("daily")
        }

        else if (isSpecificDays) {
            if (!prevScheduleWasDaily) { hideDaysIntervalSummary() }
            showDaySelectionDialog()
            scheduleViewModel.setScheduleType("specificDays")
        }

        else if (isInterval) {
            if (!prevScheduleWasDaily) { hideDaySelectionSummary() }
            showDaysIntervalDialog()
            scheduleViewModel.setScheduleType("interval")
        }
    }

    // Programmatically set the schedule radio button to daily
    fun setScheduleTypeToDaily() {
        binding.radioDaysEveryDay.isChecked = true
        binding.radioDaysSpecificDays.isChecked = false
        binding.radioDaysInterval.isChecked = false
        prevScheduleWasDaily = true
        hideScheduleSummaries()
        scheduleViewModel.setScheduleType("daily")
    }

    // Display the day selection dialog to the user
    protected fun showDaySelectionDialog(editingDaySelection: Boolean = false) {
        hideDaysIntervalSummary()

        val daySelectionDialog: DaySelectionDialogFragment = if (editingDaySelection) {
            DaySelectionDialogFragment(this, true, selectedDays)
        } else {
            DaySelectionDialogFragment(this, false)
        }

        daySelectionDialog.show(parentFragmentManager, DaySelectionDialogFragment.TAG)
    }

    // Set the selected days and update the day selection summary
    fun setSelectedDays(selectedDaysInt: String) {
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
        val daysStringList = appUtils.convertDaysIntToDaysStringList(selectedDaysInt)
        selectedDays = daysStringList
        binding.layoutDaySelectionSummary.visibility = View.VISIBLE
        binding.textViewDaySelectionSummaryValue.text = daysStringList.joinToString(", ")
        binding.buttonEditDaySelection.setOnClickListener{ showDaySelectionDialog(true) }
    }

    protected fun hideScheduleSummaries() {
        hideDaySelectionSummary()
        hideDaysIntervalSummary()
    }

    protected fun hideDaySelectionSummary() {
        binding.layoutDaySelectionSummary.visibility = View.GONE
        selectedDays = emptyList()
    }

    protected fun hideDaysIntervalSummary() {
        binding.layoutDaysIntervalSummary.visibility = View.GONE
    }

    // Schedule interval settings
    protected fun showDaysIntervalDialog(editingDaysInterval: Boolean = false) {
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