package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.R
import com.example.mediminder.adapters.HistoryAdapter
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ActivityHistoryBinding
import com.example.mediminder.models.DayLogs
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.HistoryViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        setupUI()
        lifecycleScope.launch { fetchMedications() }
    }

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
        observeViewModel()
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

    // Observe changes in the ViewModel and update UI accordingly
    private fun observeViewModel() {
        // Observe medication dropdown list selection
        lifecycleScope.launch {
            viewModel.selectedMedicationId.collect { refreshMedicationHistory(it) }
        }

        // Refresh medication history when selected month changes
        lifecycleScope.launch {
            viewModel.selectedMonth.collect { month ->
                refreshMedicationHistory(viewModel.selectedMedicationId.value)
                updateMonthYearText(month)
            }
        }
    }

    // Refresh the medication history based on the selected medication and month
    private suspend fun refreshMedicationHistory(medicationId: Long?) {
        loadingSpinnerUtil.whileLoading {
            try {
                val dayLogs = viewModel.fetchMedicationHistory(medicationId)

                if (dayLogs == null) { hideHistoryList() }
                else {
                    showHistoryList(dayLogs)
                    historyAdapter.submitList(dayLogs)
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity testcat", "Error getting medication history", e)
            }
        }
    }

    private fun hideHistoryList() {
        val selectedMonth = viewModel.selectedMonth.value
        val currentMonth = YearMonth.now()

        binding.historyList.visibility = View.GONE
        binding.noDataMessage.visibility = View.VISIBLE

        // Show "no medication" message based on whether selected month is in the future or past
        binding.noDataMessage.text = when {
            selectedMonth.isAfter(currentMonth) -> getString(R.string.no_logs_future_month)
            else -> getString(R.string.no_logs_past_month)
        }
    }

    private fun showHistoryList(dayLogs: List<DayLogs>) {
        binding.historyList.visibility = View.VISIBLE
        binding.noDataMessage.visibility = if (dayLogs.all { it.logs.isEmpty() }) View.VISIBLE else View.GONE
    }

    private suspend fun fetchMedications() {
        loadingSpinnerUtil.whileLoading {
            try {
                val medications = viewModel.fetchMedications()
                updateMedicationIds(medications)
                createMedicationDropdown(medications)
                viewModel.setSelectedMedication(null)
            } catch (e: Exception) {
                Log.e("HistoryActivity testcat", "Error in fetchMedications", e)
            }
        }
    }

    private fun updateMedicationIds(medications: List<Medication>) {
        medicationIds.clear()
        medicationIds.add(null)  // For "All Medications"
        medicationIds.addAll(medications.map { it.id })
    }

    private fun createMedicationDropdown(medications: List<Medication>) {
        val dropdownItems = mutableListOf("All Medications")
        dropdownItems.addAll(medications.map { it.name })
        medicationAdapter.clear()
        medicationAdapter.addAll(dropdownItems)
        medicationAdapter.notifyDataSetChanged()
    }

    private fun setupDateControls() {
        binding.buttonPreviousMonth.setOnClickListener { viewModel.moveMonth(forward = false) }
        binding.buttonNextMonth.setOnClickListener { viewModel.moveMonth(forward = true) }
        binding.buttonCalendar.setOnClickListener { showMonthYearPicker() }
    }

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
            .setTitleText("Select Month")
            .build()
            .apply {
                addOnPositiveButtonClickListener { selection ->
                    val selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    viewModel.setSelectedMonth(YearMonth.from(selectedDate))
                }
            }
            .show(supportFragmentManager, "MONTH_YEAR_PICKER")
    }

    private fun updateMonthYearText(yearMonth: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        binding.monthYearText.text = yearMonth.format(formatter)
    }
}