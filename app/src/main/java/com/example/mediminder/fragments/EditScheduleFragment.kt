package com.example.mediminder.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.utils.AppUtils.updateDatePickerButtonText
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.INTERVAL
import com.example.mediminder.utils.Constants.NUM_DAYS
import com.example.mediminder.utils.Constants.SPECIFIC_DAYS
import com.example.mediminder.viewmodels.BaseScheduleViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

// Fragment for editing a medication's schedule. Pre-populates the schedule fields with the data for
// the selected medication
class EditScheduleFragment : ScheduleFragment() {
    override val scheduleViewModel: BaseScheduleViewModel by activityViewModels()
    private var isInitialSetup = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCurrentMedication()
    }

    private fun observeCurrentMedication() {
        viewLifecycleOwner.lifecycleScope.launch {
            appViewModel.medication.current.collect { medication ->
                medication?.let { initSchedule(it) }
            }
        }
    }

    // Initialize schedule fields with medication details
    private fun initSchedule(medicationDetails: MedicationWithDetails) {
        isInitialSetup = true

        // Set schedule details for scheduled medications (not needed for as-needed medications)
        if (!medicationDetails.medication.asNeeded) {
            medicationDetails.schedule?.let {
                initStartDate(medicationDetails.schedule.startDate)
                medicationDetails.schedule.numDays?.let { initDuration(it) }
                initScheduleDays(medicationDetails.schedule)
            }
        }

        isInitialSetup = false
    }

    // Set start date for scheduled medication
    private fun initStartDate(startDate: LocalDate) {
        val initialStartDate = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        scheduleViewModel.setStartDate(initialStartDate)
        updateDatePickerButtonText(startDate, binding.buttonMedStartDate)
    }

    // Set duration for scheduled medication
    private fun initDuration(numDays : Int) {
        val durationType = if (numDays != 0) NUM_DAYS else CONTINUOUS
        isInitialSetup = true
        scheduleViewModel.setDurationType(durationType)
        updateDurationRadioSelection(durationType)

        if (durationType == CONTINUOUS) { hideDurationNumDaysSummary() }
        else {
            scheduleViewModel.setNumDays(numDays)
            showDurationNumDaysSummary(numDays.toString())
        }
    }

    // Set days for scheduled medication
    private fun initScheduleDays(schedule: Schedules) {
        val scheduleType = schedule.scheduleType
        isInitialSetup = true
        scheduleViewModel.setScheduleType(scheduleType)
        setRadioButtons(scheduleType)

        when (scheduleType) {
            SPECIFIC_DAYS -> {
                scheduleViewModel.setSelectedDays(schedule.selectedDays)
                showDaySelectionSummary(schedule.selectedDays)
            }
            INTERVAL -> {
                scheduleViewModel.setDaysInterval(schedule.daysInterval.toString())
                showDaysIntervalSummary(schedule.daysInterval.toString())
            }
            else -> hideScheduleSummaries()
        }
    }

    // Set radio button selection based on schedule type
    private fun setRadioButtons(scheduleType: String) {
        binding.radioDaysSpecificDays.isChecked = scheduleType == SPECIFIC_DAYS
        binding.radioDaysInterval.isChecked = scheduleType == INTERVAL
        binding.radioDaysEveryDay.isChecked = scheduleType == DAILY
    }

    // Prevent duration dialog from showing on initial setup
    override fun handleDurationSettings() {
        if (isInitialSetup) { return }
        super.handleDurationSettings()
    }

    // Prevent day selection dialog from showing on initial setup
    override fun handleScheduleSettings() {
        if (isInitialSetup) { return }
        super.handleScheduleSettings()
    }
}