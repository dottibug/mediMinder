package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.HistoryAdapter
import com.example.mediminder.databinding.ActivityHistoryBinding
import com.example.mediminder.models.DayLogs
import com.example.mediminder.utils.Constants.ERR_FETCHING_MEDS
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_HISTORY
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_HISTORY_USER
import com.example.mediminder.utils.HistoryDateUtils
import com.example.mediminder.utils.HistoryMedicationDropdownUtils
import com.example.mediminder.viewmodels.HistoryViewModel
import kotlinx.coroutines.launch

/**
 * Activity to display a list of medication history for a specific month. Users can view all
 * medications taken that month, or filter for a specific medication. Users can also select which
 * month to view history fo, via the next/prev date controls or the date picker dialog.
 */
class HistoryActivity : BaseActivity() {
    private val historyViewModel: HistoryViewModel by viewModels { HistoryViewModel.Factory }
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

    /**
     * Set up activity bindings and BaseActivity components
     * (top app bar, navigation drawer, error observer)
     */
    private fun setupActivity() {
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
        dateUtils = HistoryDateUtils(binding, historyViewModel, supportFragmentManager)
        dropdownUtils = HistoryMedicationDropdownUtils(this, binding, historyViewModel, resources)
    }

    /**
     * Set up UI components, including medication dropdown, date controls, and recycler view
     */
    private fun setupUI() {
        dropdownUtils.setupMedicationDropdown()
        dateUtils.setupDateControls()
        setupRecyclerView()
    }

    /**
     * RecyclerView for the medication history list
     */
    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.historyList.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    /**
     * Set up state flow observers to update UI when medication or month changes
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectSelectedMedication() }
                launch { collectSelectedMonth() }
            }
        }
    }

    /**
     * Collect the selected medication ID from the view model and refresh the medication history
     */
    private suspend fun collectSelectedMedication() {
        historyViewModel.selectedMedicationId.collect { refreshMedicationHistory(it) }
    }

    /**
     * Collect the selected month from the view model and refresh the medication history
     */
    private suspend fun collectSelectedMonth() {
        historyViewModel.selectedMonth.collect { month ->
            refreshMedicationHistory(historyViewModel.selectedMedicationId.value)
            dateUtils.updateMonthYearText(month)
        }
    }

    /**
     * Refresh the medication history based on the selected medication and month
     * @param medicationId The ID of the selected medication (or null)
     */
    private suspend fun refreshMedicationHistory(medicationId: Long?) {
        lifecycleScope.launch {
            try {
                val dayLogs = historyViewModel.fetchMedicationHistory(medicationId)
                if (dayLogs == null) { hideHistoryList() }
                else {
                    showHistoryList(dayLogs)
                    historyAdapter.submitList(dayLogs)
                }
            } catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_MED_HISTORY, e)
                appViewModel.setErrorMessage(ERR_FETCHING_MED_HISTORY_USER)
            }
        }
    }

    /**
     * Fetch a list of medications from the database and update the medication dropdown
     */
    private fun fetchMedicationsList() {
        lifecycleScope.launch {
            try {
                val medications = historyViewModel.fetchMedications()
                dropdownUtils.updateMedicationIds(medications)
                dropdownUtils.createMedicationDropdown(medications)
                historyViewModel.setSelectedMedication(null)
            } catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_MEDS, e)
                appViewModel.setErrorMessage(ERR_FETCHING_MEDS)
            }
        }
    }

    /**
     * Helper functions to toggle the visibility of the history list and no data message
     */
    private fun hideHistoryList() {
        binding.historyList.visibility = View.GONE
        binding.noDataMessage.visibility = View.VISIBLE
    }

    private fun showHistoryList(dayLogs: List<DayLogs>) {
        binding.historyList.visibility = View.VISIBLE
        binding.noDataMessage.visibility = if (dayLogs.all { it.logs.isEmpty() }) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "HistoryActivity"
    }
}