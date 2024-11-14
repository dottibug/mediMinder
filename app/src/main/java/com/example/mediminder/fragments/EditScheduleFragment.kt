package com.example.mediminder.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.activities.BaseActivity.Companion.CONTINUOUS
import com.example.mediminder.activities.BaseActivity.Companion.DAILY
import com.example.mediminder.activities.BaseActivity.Companion.INTERVAL
import com.example.mediminder.activities.BaseActivity.Companion.NUM_DAYS
import com.example.mediminder.activities.BaseActivity.Companion.SPECIFIC_DAYS
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.example.mediminder.viewmodels.BaseScheduleViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class EditScheduleFragment : BaseScheduleFragment() {
    override val scheduleViewModel: BaseScheduleViewModel by activityViewModels()
    override val medicationViewModel: BaseMedicationViewModel by activityViewModels { BaseMedicationViewModel.Factory }
    private var isInitialSetup = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCurrentMedication()
    }

    private fun observeCurrentMedication() {
        viewLifecycleOwner.lifecycleScope.launch {
            medicationViewModel.currentMedication.collect { medication ->
                medication?.let { initSchedule(it) }
            }
        }
    }

    private fun initSchedule(medicationDetails: MedicationWithDetails) {
        isInitialSetup = true

        medicationDetails.medication.let { med ->
//            val isScheduled = !med.asNeeded
//            scheduleViewModel.setScheduleEnabled(isScheduled)
//
//            // Clear all schedule settings if scheduled is disabled
//            if (!isScheduled) { scheduleViewModel.clearScheduleSettings() }
        }

        // Set schedule details if enabled
        if (!medicationDetails.medication.asNeeded) {
            medicationDetails.schedule?.let {
                initStartDate(medicationDetails.schedule.startDate)
                medicationDetails.schedule.numDays?.let { initDuration(it) }
                initScheduleDays(medicationDetails.schedule)
            }
        }
    }

    private fun initStartDate(startDate: LocalDate) {
        scheduleViewModel.setStartDate(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        updateDateButtonText(binding.buttonMedStartDate)
    }

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
        isInitialSetup = false
    }

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
        isInitialSetup = false
    }

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