package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.R
import com.example.mediminder.adapters.HistoryAdapter
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ActivityHistoryBinding
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.utils.WindowInsetsUtil
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
    private var medicationsList = mutableListOf<Medication>()
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private val medicationIds = mutableListOf<Long?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupBaseLayout()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)

        setupUI()

        lifecycleScope.launch { fetchMedications() }
    }

    private fun setupUI() {
        setupMedicationDropdown()
        setupDateControls()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupMedicationDropdown() {
        medicationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
            .apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        binding.medicationDropdown.adapter = medicationAdapter

        binding.medicationDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Simply use the selected medication ID directly
                val selectedId = medicationIds.getOrNull(position)
                Log.d("HistoryActivity testcat", "Selected position: $position, ID: $selectedId")
                viewModel.setSelectedMedication(selectedId)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setSelectedMedication(null)
            }
        }
    }

//    private fun setupMedicationDropdown() {
//        medicationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
//            .apply{
//                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        }
//
//        binding.medicationDropdown.adapter = medicationAdapter
//
//        binding.medicationDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                // Position 0 indicates "All Medications" and returns null to indicate we are not
//                // filtering the query by medication id
//                val selectedId = if (position == 0) {
//                    null
//                } else if (position - 1 < medicationsList.size) {
//                    medicationsList[position - 1].id
//                } else {
//                    Log.e("HistoryActivity testcat", "Invalid position: $position for medications list size: ${medicationsList.size}")
//                    null  // Default to "All Medications" if position is invalid
//                }
//
//                viewModel.setSelectedMedication(selectedId)
//            }
//
//            // Default to "all medications" if nothing is selected
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                    viewModel.setSelectedMedication(null)
//            }
//        }
//    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.historyList.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
        Log.d("HistoryActivity testcat", "RecyclerView setup complete")
    }

    private fun observeViewModel() {
        // Handle medication selection changes
        lifecycleScope.launch {
            viewModel.selectedMedicationId.collect { medicationId ->
                refreshMedicationHistory(medicationId)
//                if (medicationId != null) { refreshMedicationHistory(medicationId) }
            }
        }

        // Refresh medication history when selected date changes
        lifecycleScope.launch {
            viewModel.selectedDate.collect { selectedMonth ->
                Log.d("HistoryActivity testcat", "Selected date changed to: $selectedMonth")
                // Here too, we should refresh regardless of medicationId being null
                refreshMedicationHistory(viewModel.selectedMedicationId.value)
            }
        }
    }

    private suspend fun refreshMedicationHistory(medicationId: Long?) {
        Log.d("HistoryActivity testcat", "Refreshing history for medicationId: $medicationId")

        loadingSpinnerUtil.whileLoading {
            try {
                val dayLogs = viewModel.fetchMedicationHistory(medicationId)
                Log.d("HistoryActivity testcat", "Submitting list to adapter: $dayLogs")


                if (dayLogs == null) {
                    // If no logs for this month, show no data message
                    binding.historyList.visibility = View.GONE
                    binding.noDataMessage.visibility = View.VISIBLE

                    val selectedMonth = viewModel.selectedDate.value
                    val currentMonth = YearMonth.now()

                    binding.noDataMessage.text = if (selectedMonth.isAfter(currentMonth)) {
                        getString(R.string.no_logs_future_month)
                    } else {
                        getString(R.string.no_logs_past_month)
                    }
                } else {
                    binding.historyList.visibility = View.VISIBLE
                    binding.noDataMessage.visibility = if (dayLogs.all { it.logs.isEmpty() }) View.VISIBLE else View.GONE
                    historyAdapter.submitList(dayLogs)
                }
            } catch (e: Exception) {
                Log.e("HistoryActivity testcat", "Error getting medication history", e)
            }
        }
    }

    private suspend fun fetchMedications() {
        loadingSpinnerUtil.whileLoading {
            try {
                val medications = viewModel.fetchMedications()

                // Clear and update the IDs list
                medicationIds.clear()
                medicationIds.add(null)  // For "All Medications"
                medicationIds.addAll(medications.map { it.id })

                // Create list with "All Medications" option at the top
                val dropdownItems = mutableListOf("All Medications")
                dropdownItems.addAll(medications.map { it.name })

                medicationAdapter.clear()
                medicationAdapter.addAll(dropdownItems)
                medicationAdapter.notifyDataSetChanged()

                viewModel.setSelectedMedication(null)

            } catch (e: Exception) {
                Log.e("HistoryActivity testcat", "Error in fetchMedications", e)
                e.printStackTrace()
            }
        }
    }

    private fun setupDateControls() {
        // Prev month button
        binding.buttonPreviousMonth.setOnClickListener {
            viewModel.moveMonth(forward = false)
        }

        // Next month button
        binding.buttonNextMonth.setOnClickListener {
            viewModel.moveMonth(forward = true)
        }

        // Calendar button
        binding.buttonCalendar.setOnClickListener {
            showMonthYearPicker()
        }

        // Observe selected month
        lifecycleScope.launch {
            viewModel.selectedDate.collect { selectedMonth ->
                // Update UI to reflect selected month
                updateMonthYearText(selectedMonth)
            }
        }
    }

    private fun showMonthYearPicker() {
        val currentDate = viewModel.selectedDate.value

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