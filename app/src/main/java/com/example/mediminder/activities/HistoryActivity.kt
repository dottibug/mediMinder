package com.example.mediminder.activities

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.R
import com.example.mediminder.adapters.HistoryAdapter
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ActivityHistoryBinding
import com.example.mediminder.models.DayLogs
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.HistoryViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// This activity displays a list of medication history for a specific month.
// Users can view all medications taken that month, or filter for a specific medication.
// Users can also select which month to view history for, via the next/prev date controls or the
// date picker dialog.
class HistoryActivity : BaseActivity() {
    private val viewModel: HistoryViewModel by viewModels { HistoryViewModel.Factory }
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var medicationAdapter: ArrayAdapter<String>
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private val medicationIds = mutableListOf<Long?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        fetchMedicationsList()
    }

    // Set up bindings for the base class, then inflate this view into the base layout.
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    private fun setupUI() {
        setupMedicationDropdown()
        setupDateControls()
        setupRecyclerView()
    }

    // Set up the medication dropdown, which allows users to view history of a specific medication or all medications
    private fun setupMedicationDropdown() {
        medicationAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        binding.medicationDropdown.setAdapter(medicationAdapter)
        setDefaultMedicationDropdown()

        binding.medicationDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setSelectedMedication(medicationIds.getOrNull(position))
        }
    }

    // Set the default medication in the dropdown to "All Medications"
    private fun setDefaultMedicationDropdown() {
        if (medicationIds.isEmpty()) {
            binding.medicationDropdown.setText(getString(R.string.all_meds), false)
        } else {
            binding.medicationDropdown.setText(medicationAdapter.getItem(0), false)
        }
    }

    // Set up the RecyclerView for the medication history list
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.historyList.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    // Set up observers to update the UI when state flow changes
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectSelectedMedication() }
                launch { collectSelectedMonth() }
                launch { collectErrorMessage() }
                launch { collectLoadingSpinner() }
            }
        }
    }

    // Collect the selected medication ID from the view model and refresh the medication history
    private suspend fun collectSelectedMedication() {
        viewModel.selectedMedicationId.collect { refreshMedicationHistory(it) }
    }

    // Collect the selected month from view model and refresh the medication history
    private suspend fun collectSelectedMonth() {
        viewModel.selectedMonth.collect { month ->
            refreshMedicationHistory(viewModel.selectedMedicationId.value)
            updateMonthYearText(month)
        }
    }

    // Collect the error message and display to user as a Toast
    private suspend fun collectErrorMessage() {
        viewModel.errorMessage.collect { msg ->
            if (msg != null) {
                createToast(this@HistoryActivity, msg)
                viewModel.clearError()
            }
        }
    }

    // Collect loading spinner state
    private suspend fun collectLoadingSpinner() {
        viewModel.isLoading.collect { isLoading ->
            if (isLoading) loadingSpinnerUtil.show() else loadingSpinnerUtil.hide()
        }
    }

    // Refresh the medication history based on the selected medication and month
    private suspend fun refreshMedicationHistory(medicationId: Long?) {
        val dayLogs = viewModel.fetchMedicationHistory(medicationId)

        if (dayLogs == null) { hideHistoryList() }

        else {
            showHistoryList(dayLogs)
            historyAdapter.submitList(dayLogs)
        }
    }

    private fun hideHistoryList() {
        binding.historyList.visibility = View.GONE
        binding.noDataMessage.visibility = View.VISIBLE
    }

    private fun showHistoryList(dayLogs: List<DayLogs>) {
        binding.historyList.visibility = View.VISIBLE
        binding.noDataMessage.visibility = if (dayLogs.all { it.logs.isEmpty() }) View.VISIBLE else View.GONE
    }

    // Fetch list of medications for the dropdown menu
    private fun fetchMedicationsList() {
        lifecycleScope.launch {
            val medications = viewModel.fetchMedications()
            updateMedicationIds(medications)
            createMedicationDropdown(medications)
            viewModel.setSelectedMedication(null)
        }
    }

    // Update the list of medication IDs used in the dropdown menu
    private fun updateMedicationIds(medications: List<Medication>) {
        medicationIds.apply {
            clear()
            add(null)   // For "All Medications"
            addAll(medications.map { it.id })
        }
    }

    // Create the medication dropdown menu with the list of medications fetched
    private fun createMedicationDropdown(medications: List<Medication>) {
        val dropdownItems = mutableListOf(ALL_MEDS)
        dropdownItems.addAll(medications.map { it.name })
        medicationAdapter.apply {
            clear()
            addAll(dropdownItems)
            notifyDataSetChanged()
        }
    }

    // Date control buttons to navigate through months
    private fun setupDateControls() {
        with (binding) {
            buttonPreviousMonth.setOnClickListener { viewModel.moveMonth(forward = false) }
            buttonNextMonth.setOnClickListener { viewModel.moveMonth(forward = true) }
            buttonCalendar.setOnClickListener { showMonthYearPicker() }
        }
    }

    // Show a date picker to allow users to select a month
    private fun showMonthYearPicker() {
        val currentDate = viewModel.selectedMonth.value

        MaterialDatePicker.Builder.datePicker()
            .setSelection(
                currentDate
                    .atDay(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
            .setTitleText(SELECT_MONTH)
            .build()
            .apply {
                addOnPositiveButtonClickListener { selection ->
                    val selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    viewModel.setSelectedMonth(YearMonth.from(selectedDate))
                }
            }
            .show(supportFragmentManager, MONTH_YEAR_PICKER)
    }

    // Update the text displayed to show the user-selected month
    private fun updateMonthYearText(yearMonth: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT)
        binding.monthYearText.text = yearMonth.format(formatter)
    }

    companion object {
        private const val ALL_MEDS = "All Medications"
        private const val MONTH_YEAR_PICKER = "MONTH_YEAR_PICKER"
        private const val SELECT_MONTH = "Select Month"
        private const val MONTH_YEAR_FORMAT = "MMMM yyyy"
    }
}