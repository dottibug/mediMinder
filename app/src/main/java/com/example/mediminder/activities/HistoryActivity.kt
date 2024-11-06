package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
            .apply{
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.medicationDropdown.adapter = medicationAdapter
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()

        binding.historyList.layoutManager = LinearLayoutManager(this)
        binding.historyList.adapter = historyAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.selectedMedicationId.collect { medicationId ->
                // Handle medication selection changes
                Log.d("HistoryActivity testcat", "Selected medication ID: $medicationId")
            }
        }
    }

    private suspend fun fetchMedications() {
        loadingSpinnerUtil.whileLoading {
            try {
                val medications = viewModel.fetchMedications()
                val medicationNames = medications.map { it.name }

                medicationAdapter.clear()
                medicationAdapter.addAll(medicationNames)
                medicationAdapter.notifyDataSetChanged()

                // Update local list
                medicationsList.clear()
                medicationsList.addAll(medications)

                // Select first medication by default if available
                if (medications.isNotEmpty()) {
                    viewModel.setSelectedMedication(medications.first().id)
                }

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