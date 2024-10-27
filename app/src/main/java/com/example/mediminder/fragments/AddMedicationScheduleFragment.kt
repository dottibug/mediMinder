package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentAddMedicationScheduleBinding
import com.example.mediminder.utils.AppUtils
import com.example.mediminder.viewmodels.AddMedicationScheduleViewModel
import com.example.mediminder.viewmodels.AddMedicationViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class AddMedicationScheduleFragment : Fragment() {
    private lateinit var binding: FragmentAddMedicationScheduleBinding
    private val scheduleViewModel: AddMedicationScheduleViewModel by viewModels()
    private val addMedViewModel: AddMedicationViewModel by viewModels { AddMedicationViewModel.Factory }
    private val appUtils = AppUtils()
    private var prevScheduleWasDaily: Boolean = true
    private lateinit var selectedDays: List<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMedicationScheduleBinding.inflate(inflater, container, false)
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
        binding.buttonAddMedStartDate.setOnClickListener { showDatePickerDialog() }
        binding.radioGroupAddMedDuration.setOnCheckedChangeListener { _, _ -> handleDurationSettings() }
        binding.radioGroupAddMedSchedule.setOnCheckedChangeListener { _, _ -> handleScheduleSettings() }
    }

    // Sets up observers for schedule data. Collects state flow in the scheduleViewModel and updates
    // the corresponding data in the addMedViewModel.
    private fun setupObservers() {
        // Start date observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.startDate.collect { date -> addMedViewModel.updateStartDate(date) }
        }

        // Duration type observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.durationType.collect { type -> addMedViewModel.updateDurationType(type) }
        }

        // Schedule type observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.scheduleType.collect { type -> addMedViewModel.updateScheduleType(type) }
        }

        // Num days observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.numDays.collect { numDays -> addMedViewModel.updateNumDays(numDays) }
        }

        // Selected days observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.selectedDays.collect { days -> addMedViewModel.updateSelectedDays(days) }
        }

        // Days interval observer
        viewLifecycleOwner.lifecycleScope.launch {
            scheduleViewModel.daysInterval.collect { interval -> addMedViewModel.updateDaysInterval(interval) }
        }
    }

    // Date Picker Dialog (selects current date by default)
    private fun showDatePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a reminder start date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(parentFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            scheduleViewModel.setStartDate(selection)
            updateDateButtonText(binding.buttonAddMedStartDate)
        }
    }

    // Update the button text with the selected date
    private fun updateDateButtonText(button: Button) {
        val date = scheduleViewModel.startDate.value
        val formattedDate = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        button.text = date?.format(formattedDate) ?: "Select a date"
    }

    // Medication duration settings
    private fun handleDurationSettings() {
        val isContinuous = binding.radioDurationContinuous.isChecked

        if (!isContinuous) { showDurationInputDialog() }

        else {
            scheduleViewModel.setDurationType("continuous")
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
    private fun updateDurationRadioSelection(checked: String) {
        if (checked == "continuous") {
            binding.radioDurationContinuous.isChecked = true
            binding.radioDurationNumDays.isChecked = false
        } else {
            binding.radioDurationContinuous.isChecked = false
            binding.radioDurationNumDays.isChecked = true
        }
    }

    // Show the number of days summary to the user
    private fun showDurationNumDaysSummary(numDays: String) {
        binding.layoutNumDaysSummary.visibility = View.VISIBLE
        binding.textViewNumDaysSummaryValue.text = resources.getString(R.string.num_days_summary_value, numDays)
        binding.buttonEditNumDays.setOnClickListener { showDurationInputDialog(true) }
    }

    private fun hideDurationNumDaysSummary() {
        binding.layoutNumDaysSummary.visibility = View.GONE
    }

    private fun updateDurationInScheduleViewModel(numDays: String) {
        scheduleViewModel.setDurationType("numDays")
        scheduleViewModel.setNumDays(numDays.toInt())
    }

    // Medication schedule settings
    private fun handleScheduleSettings() {
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
    private fun showDaySelectionSummary(selectedDaysInt: String) {
        val daysStringList = appUtils.convertDaysIntToDaysStringList(selectedDaysInt)
        selectedDays = daysStringList
        binding.layoutDaySelectionSummary.visibility = View.VISIBLE
        binding.textViewDaySelectionSummaryValue.text = daysStringList.joinToString(", ")
        binding.buttonEditDaySelection.setOnClickListener{ showDaySelectionDialog(true) }
    }

    private fun hideScheduleSummaries() {
        hideDaySelectionSummary()
        hideDaysIntervalSummary()
    }

    private fun hideDaySelectionSummary() {
        binding.layoutDaySelectionSummary.visibility = View.GONE
        selectedDays = emptyList()
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

    private fun showDaysIntervalSummary(daysInterval: String) {
        hideDaySelectionSummary()
        binding.layoutDaysIntervalSummary.visibility = View.VISIBLE
        binding.textViewDaysIntervalSummaryValue.text = resources.getString(R.string.days_interval_summary_value, daysInterval)
        binding.buttonEditDaysInterval.setOnClickListener { showDaysIntervalDialog(true) }
    }
}