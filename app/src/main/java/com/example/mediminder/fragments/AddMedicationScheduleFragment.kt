package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentAddMedicationScheduleBinding
import com.example.mediminder.utils.AppUtils
import com.example.mediminder.viewmodels.AddMedicationScheduleViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.format.DateTimeFormatter

class AddMedicationScheduleFragment : Fragment() {
    private lateinit var binding: FragmentAddMedicationScheduleBinding
    private val viewModel: AddMedicationScheduleViewModel by viewModels()
    private val appUtils = AppUtils()
    private lateinit var durationNumDays: String
    private lateinit var selectedDays: List<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddMedicationScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        binding.layoutNumDaysSummary.visibility = View.GONE
        binding.layoutDaySelectionSummary.visibility = View.GONE
    }

    private fun setupListeners() {
        binding.buttonAddMedStartDate.setOnClickListener { showDatePickerDialog() }
        binding.radioGroupAddMedDuration.setOnCheckedChangeListener { _, _ -> handleDurationSettings() }
        binding.radioGroupAddMedSchedule.setOnCheckedChangeListener { _, _ -> handleScheduleSettings() }
    }

    // Date Picker Dialog
    private fun showDatePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a reminder start date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // select today by default
            .build()

        datePicker.show(parentFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            viewModel.setStartDate(selection)
            updateDateButtonText(binding.buttonAddMedStartDate)
        }
    }

    // Update the button text with the selected date
    private fun updateDateButtonText(button: Button) {
        val date = viewModel.startDate.value
        val formattedDate = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        button.text = date?.format(formattedDate) ?: "Select a date"
    }

    // Medication duration settings
    private fun handleDurationSettings() {
        val isContinuous = binding.radioDurationContinuous.isChecked

        if (!isContinuous) { showDurationInputDialog() }
        else {
            viewModel.setDurationType("continuous")
            setDurationRadioToContinuous()
        }
    }

    // Displays the duration input dialog to the user
    private fun showDurationInputDialog(editingNumDays: Boolean = false) {
        val durationDialog = DurationDialogFragment(this, editingNumDays)
        durationDialog.show(parentFragmentManager, DurationDialogFragment.TAG)
    }

    // Programmatically set the duration radio button to continuous and hide the number of days summary
    fun setDurationRadioToContinuous() {
        binding.radioDurationContinuous.isChecked = true
        binding.radioDurationNumDays.isChecked = false
        binding.layoutNumDaysSummary.visibility = View.GONE
        durationNumDays = ""
    }

    // Set the number of days and update the number of days summary
    fun setDurationNumDays(numDays: String) {
        durationNumDays = numDays
        viewModel.setDurationType("numDays")
        viewModel.setNumDays(numDays.toInt())
        binding.radioDurationNumDays.isChecked = true
        binding.radioDurationContinuous.isChecked = false
        showDurationNumDaysSummary(numDays)
    }

    // Show the number of days summary to the user
    private fun showDurationNumDaysSummary(numDays: String) {
        binding.layoutNumDaysSummary.visibility = View.VISIBLE
        binding.textViewNumDaysSummaryValue.text = resources.getString(R.string.num_days_summary_value, numDays)
        binding.buttonEditNumDays.setOnClickListener { showDurationInputDialog(true) }
    }

    // Medication schedule settings
    private fun handleScheduleSettings() {
        val isEveryDay = binding.radioDaysEveryDay.isChecked
        val isSpecificDays = binding.radioDaysSpecificDays.isChecked
        val isInterval = binding.radioDaysInterval.isChecked

        if (isEveryDay) { viewModel.setScheduleType("daily") }

        else if (isSpecificDays) {
            showDaySelectionDialog()
            viewModel.setScheduleType("specificDays")
        }

        else if (isInterval) {
            // todo: show the interval dialog
            viewModel.setScheduleType("interval")
        }
    }

    // Programmatically set the schedule radio button to daily and hide the selected days summary
    fun setScheduleTypeToDaily() {
        binding.radioDaysEveryDay.isChecked = true
        binding.radioDaysSpecificDays.isChecked = false
        binding.radioDaysInterval.isChecked = false

        viewModel.setScheduleType("daily")

        binding.layoutDaySelectionSummary.visibility = View.GONE
        selectedDays = emptyList()
    }

    // Display the day selection dialog to the user
    private fun showDaySelectionDialog(editingDaySelection: Boolean = false) {
        val daySelectionDialog: DaySelectionDialogFragment

        if (editingDaySelection) {
            daySelectionDialog = DaySelectionDialogFragment(this, true, selectedDays)
        } else {
            daySelectionDialog = DaySelectionDialogFragment(this, false)
        }

        daySelectionDialog.show(parentFragmentManager, DaySelectionDialogFragment.TAG)
    }

    // Set the selected days and update the day selection summary
    fun setSelectedDays(selectedDaysInt: String) {
        if (selectedDaysInt.isEmpty()) {
            setScheduleTypeToDaily()
            binding.layoutDaySelectionSummary.visibility = View.GONE
        } else {
            viewModel.setSelectedDays(selectedDaysInt) // "0,1,2" -> Sunday, Monday, Tuesday
            val daysStringList = appUtils.convertDaysIntToDaysStringList(selectedDaysInt)
            selectedDays = daysStringList
            showDaySelectionSummary(daysStringList)
        }
    }

    // Show the day selection summary to the user
    private fun showDaySelectionSummary(daysStringList: List<String>) {
        binding.layoutDaySelectionSummary.visibility = View.VISIBLE
        binding.textViewDaySelectionSummaryValue.text = daysStringList.joinToString(", ")
        binding.buttonEditDaySelection.setOnClickListener{ showDaySelectionDialog(true) }
    }
}