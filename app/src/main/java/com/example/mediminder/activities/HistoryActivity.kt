package com.example.mediminder.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.HistoryAdapter
import com.example.mediminder.databinding.ActivityHistoryBinding
import com.example.mediminder.models.DayLogs
import com.example.mediminder.utils.HistoryDateUtils
import com.example.mediminder.utils.HistoryMedicationDropdownUtils
import com.example.mediminder.viewmodels.HistoryViewModel
import kotlinx.coroutines.launch

// This activity displays a list of medication history for a specific month.
// Users can view all medications taken that month, or filter for a specific medication.
// Users can also select which month to view history for, via the next/prev date controls or the
// date picker dialog.
class HistoryActivity : BaseActivity() {
    private val viewModel: HistoryViewModel by viewModels { HistoryViewModel.Factory }
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var dateUtils: HistoryDateUtils
    private lateinit var dropdownUtils: HistoryMedicationDropdownUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
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

    // Set up bindings and utilities for this activity
    private fun setupActivity() {
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
        dateUtils = HistoryDateUtils(binding, viewModel, supportFragmentManager)
        dropdownUtils = HistoryMedicationDropdownUtils(this, binding, viewModel, resources)
    }

    private fun setupUI() {
        dropdownUtils.setupMedicationDropdown()
        dateUtils.setupDateControls()
        setupRecyclerView()
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
            dateUtils.updateMonthYearText(month)
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
            dropdownUtils.updateMedicationIds(medications)
            dropdownUtils.createMedicationDropdown(medications)
            viewModel.setSelectedMedication(null)
        }
    }
}