package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentScheduleBinding
import com.example.mediminder.utils.AppUtils.createDatePicker
import com.example.mediminder.utils.AppUtils.daysOfWeekString
import com.example.mediminder.utils.AppUtils.updateDatePickerButtonText
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.DATE_PICKER_TAG
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.INTERVAL
import com.example.mediminder.utils.Constants.NUM_DAYS
import com.example.mediminder.utils.Constants.SPECIFIC_DAYS
import com.example.mediminder.viewmodels.AppViewModel
import com.example.mediminder.viewmodels.ScheduleViewModel
import kotlinx.coroutines.launch

/**
 * Base fragment for adding or editing a medication's schedule.
 */
open class ScheduleFragment : Fragment() {
    protected lateinit var binding: FragmentScheduleBinding
    open val scheduleViewModel: ScheduleViewModel by activityViewModels()
    protected val appViewModel: AppViewModel by activityViewModels { AppViewModel.Factory }
    private var prevScheduleWasDaily: Boolean = true
    protected lateinit var selectedDays: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInitialVisibility()
        setupListeners()
        setupObservers()
    }

    /**
     * Set the initial visibility of the summary layouts to false
     */
    private fun setupInitialVisibility() {
        with (binding) {
            setViewVisibility(layoutNumDaysSummary, false)
            setViewVisibility(layoutDaySelectionSummary, false)
            setViewVisibility(layoutDaysIntervalSummary, false)
        }
    }

    private fun setupListeners() {
        with (binding) {
            buttonMedStartDate.setOnClickListener { showDatePickerDialog() }
            radioGroupAddMedDuration.setOnCheckedChangeListener { _, _ -> handleDurationSettings() }
            radioGroupAddMedSchedule.setOnCheckedChangeListener { _, _ -> handleScheduleSettings() }
        }
    }

    /**
     * Collect state flow from schedule view model when the fragment is in the STARTED state
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                with (scheduleViewModel) {
                    launch { startDate.collect { date -> appViewModel.schedule.setStartDate(date) } }
                    launch { durationType.collect { type -> appViewModel.schedule.setDurationType(type) } }
                    launch { numDays.collect { numDays -> appViewModel.schedule.setNumDays(numDays) } }
                    launch { scheduleType.collect { type -> appViewModel.schedule.setScheduleType(type) } }
                    launch { selectedDays.collect { days -> appViewModel.schedule.setSelectedDays(days) } }
                    launch { daysInterval.collect { interval -> appViewModel.schedule.setDaysInterval(interval) } }
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = createDatePicker(getString(R.string.select_start_date))
        datePicker.addOnPositiveButtonClickListener { selection ->
            scheduleViewModel.setStartDate(selection)
            updateDatePickerButtonText(scheduleViewModel.startDate.value, binding.buttonMedStartDate)
        }
        datePicker.show(parentFragmentManager, DATE_PICKER_TAG)
    }

    /**
     * Handle the medication duration settings based on the selected radio button
     */
    protected open fun handleDurationSettings() {
        val isContinuous = binding.radioDurationContinuous.isChecked
        if (!isContinuous) { showDurationInputDialog() }
        else {
            scheduleViewModel.setDurationType(CONTINUOUS)
            setDurationRadioToContinuous()
        }
    }

    /**
     * Show the duration input dialog to the user
     */
    private fun showDurationInputDialog(editingNumDays: Boolean = false) {
        val durationDialog = DurationDialogFragment(this, editingNumDays)
        durationDialog.show(parentFragmentManager, DurationDialogFragment.TAG)
    }

    /**
     * Programmatically set the duration radio button to continuous
     */
    fun setDurationRadioToContinuous() {
        updateDurationRadioSelection(CONTINUOUS)
        hideDurationNumDaysSummary()
    }

    /**
     * Set the number of days and update the number of days summary
     */
    fun setDurationNumDays(numDays: String) {
        updateDurationRadioSelection(NUM_DAYS)
        updateDurationInScheduleViewModel(numDays)
        showDurationNumDaysSummary(numDays)
    }

    protected fun updateDurationRadioSelection(checked: String) {
        binding.radioDurationContinuous.isChecked = checked == CONTINUOUS
        binding.radioDurationNumDays.isChecked = checked == NUM_DAYS
    }

    protected fun showDurationNumDaysSummary(numDays: String) {
        with (binding) {
            setViewVisibility(layoutNumDaysSummary, true)
            textViewNumDaysSummaryValue.text = resources.getString(R.string.days_value, numDays)
            buttonEditNumDays.setOnClickListener { showDurationInputDialog(true) }
        }
    }

    protected fun hideDurationNumDaysSummary() {
        setViewVisibility(binding.layoutNumDaysSummary, false)
    }

    private fun updateDurationInScheduleViewModel(numDays: String) {
        scheduleViewModel.setDurationType(NUM_DAYS)
        scheduleViewModel.setNumDays(numDays.toInt())
    }

    protected open fun handleScheduleSettings() {
        with (binding) {
            when {
                radioDaysEveryDay.isChecked -> setupEveryDaySchedule()
                radioDaysSpecificDays.isChecked -> setupSpecificDaysSchedule()
                radioDaysInterval.isChecked -> setupIntervalSchedule()
            }
        }
    }

    /**
     * Helper function to setup every day schedule UI
     */
    private fun setupEveryDaySchedule() {
        prevScheduleWasDaily = true
        hideScheduleSummaries()
        scheduleViewModel.setScheduleType(DAILY)
    }

    /**
     * Helper function to setup specific days schedule UI
     */
    private fun setupSpecificDaysSchedule() {
        if (!prevScheduleWasDaily) { hideDaysIntervalSummary() }
        showDaySelectionDialog()
        scheduleViewModel.setScheduleType(SPECIFIC_DAYS)
    }

    /**
     * Helper function to setup interval schedule UI
     */
    private fun setupIntervalSchedule() {
        if (!prevScheduleWasDaily) { hideDaySelectionSummary() }
        showDaysIntervalDialog()
        scheduleViewModel.setScheduleType(INTERVAL)
    }

    /**
     * Programmatically set the schedule radio button to daily
     */
    fun setScheduleTypeToDaily() {
        with (binding) {
            radioDaysEveryDay.isChecked = true
            radioDaysSpecificDays.isChecked = false
            radioDaysInterval.isChecked = false
        }
        prevScheduleWasDaily = true
        hideScheduleSummaries()
        scheduleViewModel.setScheduleType(DAILY)
    }

    private fun showDaySelectionDialog(editingDaySelection: Boolean = false) {
        hideDaysIntervalSummary()

        val daySelectionDialog: DaySelectionDialogFragment = if (editingDaySelection) {
            DaySelectionDialogFragment(this, true, selectedDays)
        } else { DaySelectionDialogFragment(this, false) }

        daySelectionDialog.show(parentFragmentManager, DaySelectionDialogFragment.TAG)
    }

    /**
     * Set the selected days and update the day selection summary
     */
    fun updateSelectedDays(selectedDaysInt: String) {
        if (selectedDaysInt.isEmpty()) {
            setScheduleTypeToDaily()
            hideScheduleSummaries()
        } else {
            showDaySelectionSummary(selectedDaysInt)
            scheduleViewModel.setSelectedDays(selectedDaysInt)
        }
    }

    protected fun showDaySelectionSummary(selectedDaysInt: String) {
        val daysString = daysOfWeekString(selectedDaysInt)
        selectedDays = selectedDaysInt
        with (binding) {
            setViewVisibility(layoutDaySelectionSummary, true)
            textViewDaySelectionSummaryValue.text = daysString
            buttonEditDaySelection.setOnClickListener{ showDaySelectionDialog(true) }
        }
    }

    private fun setViewVisibility(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    protected fun hideScheduleSummaries() {
        hideDaySelectionSummary()
        hideDaysIntervalSummary()
    }

    private fun hideDaySelectionSummary() {
        setViewVisibility(binding.layoutDaySelectionSummary, false)
        selectedDays = EMPTY_STRING
    }

    private fun hideDaysIntervalSummary() {
        setViewVisibility(binding.layoutDaysIntervalSummary, false)
    }

    private fun showDaysIntervalDialog(editingDaysInterval: Boolean = false) {
        hideDaySelectionSummary()
        val daysIntervalDialog = DaysIntervalDialogFragment(this, editingDaysInterval)
        daysIntervalDialog.show(parentFragmentManager, DaysIntervalDialogFragment.TAG)
    }

    fun setDaysInterval(daysInterval: String) {
        scheduleViewModel.setDaysInterval(daysInterval)
        showDaysIntervalSummary(daysInterval)
    }

    protected fun showDaysIntervalSummary(daysInterval: String) {
        hideDaySelectionSummary()
        with (binding) {
            setViewVisibility(layoutDaysIntervalSummary, true)
            textViewDaysIntervalSummaryValue.text = resources.getString(R.string.days_value, daysInterval)
            buttonEditDaysInterval.setOnClickListener { showDaysIntervalDialog(true) }
        }
    }
}