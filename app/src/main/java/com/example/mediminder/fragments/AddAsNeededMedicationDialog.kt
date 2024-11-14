package com.example.mediminder.fragments

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.activities.AddMedicationActivity
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.FragmentAddAsNeededMedBinding
import com.example.mediminder.utils.AppUtils.convert24HourTo12Hour
import com.example.mediminder.utils.ValidationUtils.getValidatedAsNeededData
import com.example.mediminder.viewmodels.MainViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddAsNeededMedicationDialog: DialogFragment() {
    private lateinit var binding: FragmentAddAsNeededMedBinding
    private lateinit var adapter: ArrayAdapter<String>
    private val viewModel: MainViewModel by activityViewModels()
    private val asNeededMedIds = mutableListOf<Long?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, mutableListOf<String>())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentAddAsNeededMedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    // Fetch medications on resume to update dropdown menu of as-needed medications
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { fetchAsNeededMedications() }
    }

    private fun fetchAsNeededMedications() {
        try {
            viewModel.fetchAsNeededMedications()
        } catch (e: Exception) {
            Log.e("HistoryViewModel testcat", "Error fetching as needed medications", e)
        }
    }

    private fun setupUI() {
        binding.asNeededMedDropdown.setAdapter(adapter)
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // Observe asNeededMedications to update dropdown menu
        viewLifecycleOwner.lifecycleScope.launch {
            // RepeatOnLifecycle only collects data when the fragment is in the STARTED state
            // This prevents the UI from being updated when the fragment is not visible
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.asNeededMedications.collect { medications ->
                    if (medications.isEmpty()) { hideAsNeededInputs() }
                    else {
                        showAsNeededInputs()
                        updateAdapter(medications)
                    }
                }
            }
        }
    }

    private fun updateAdapter(medications: List<Medication>) {
        adapter.clear()
        adapter.addAll(medications.map { it.name })
        asNeededMedIds.clear()
        asNeededMedIds.addAll(medications.map { it.id })
    }

    private fun hideAsNeededInputs() {
        binding.layoutAddAsNeededMed.visibility = View.VISIBLE
        binding.noAsNeededMedsMessage.visibility = View.VISIBLE
        binding.addNewButton.visibility = View.VISIBLE
        binding.asNeededMedDropdownWrapper.visibility = View.GONE
        binding.layoutDosage.visibility = View.GONE
        binding.layoutAsNeededDateTime.visibility = View.GONE
    }

    private fun showAsNeededInputs() {
        binding.layoutAddAsNeededMed.visibility = View.VISIBLE
        binding.noAsNeededMedsMessage.visibility = View.GONE
        binding.addNewButton.visibility = View.VISIBLE
        binding.asNeededMedDropdownWrapper.visibility = View.VISIBLE
        binding.layoutDosage.visibility = View.VISIBLE
        binding.layoutAsNeededDateTime.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        binding.asNeededMedDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setSelectedAsNeededMedication(asNeededMedIds.getOrNull(position))
        }

        binding.addNewButton.setOnClickListener { addNewAsNeededMed() }
        binding.buttonAsNeededDateTaken.setOnClickListener { showDatePicker() }
        binding.buttonAsNeededTimeTaken.setOnClickListener { showTimePicker() }
        binding.buttonCancelAddAsNeededMed.setOnClickListener { dismiss() }

        binding.buttonSetAddAsNeededMed.setOnClickListener {
            addAsNeededMedication()
            dismiss()
        }
    }

    private fun addAsNeededMedication() {
        val selectedMedId = viewModel.selectedAsNeededMedId.value
        val dosageAmount = binding.asNeededDosageAmount.text.toString()
        val dosageUnits = binding.asNeededDosageUnits.text.toString()
        val dateTaken = viewModel.dateTaken.value
        val timeTaken = viewModel.timeTaken.value

        // Validate input
        val validatedData = getValidatedAsNeededData(selectedMedId, dosageAmount, dosageUnits, dateTaken, timeTaken)

        lifecycleScope.launch {
            try {
                viewModel.addAsNeededLog(validatedData)

                // Refresh medications for the current date before dismissing
                viewModel.fetchMedicationsForDate(viewModel.selectedDate.value)
                dismiss()
            } catch (e: Exception) {
                Log.e("HistoryViewModel testcat", "Error adding as needed log", e)
            }
        }
    }

    private fun addNewAsNeededMed() {
        val intent = Intent(requireContext(), AddMedicationActivity::class.java)
        intent.putExtra("ADD_AS_NEEDED", true)
        startActivity(intent)
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Time Medication Was Taken")
            .build()

        timePicker.show(parentFragmentManager, "tag")

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            viewModel.setTimeTaken(hour, minute)
            updateTimePickerButtonText()
        }
    }

    // Update the time picker button text
    private fun updateTimePickerButtonText() {
        val hour = viewModel.timeTaken.value?.first
        val minute = viewModel.timeTaken.value?.second

        if (hour != null && minute != null) {
            val convertedHour = convert24HourTo12Hour(hour)
            val amPm = if (hour < 12) "AM" else "PM"
            val formattedTime = String.format(Locale.CANADA, "%1d:%02d %s", convertedHour, minute, amPm)
            binding.buttonAsNeededTimeTaken.text = formattedTime
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date Medication Was Taken")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.show(parentFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener { selection ->
            viewModel.setDateTaken(selection)
            updateDateButtonText()
        }
    }

    private fun updateDateButtonText() {
        val date = viewModel.dateTaken.value
        val formattedDate = DateTimeFormatter.ofPattern("MMM d, yyyy")
        binding.buttonAsNeededDateTaken.text = date?.format(formattedDate)
    }
}