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

// Dialog fragment for adding an as-needed medication
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

    // Fetch as-needed medications for the dropdown menu
    private fun fetchAsNeededMedications() {
        try {
            viewModel.fetchAsNeededMedications()
        } catch (e: Exception) {
            Log.e("HistoryViewModel testcat", "Error fetching as needed medications", e)
        }
    }

    // Setup UI elements
    private fun setupUI() {
        binding.asNeededMedDropdown.setAdapter(adapter)
        setupObservers()
        setupListeners()
    }

    // Observe asNeededMedications to update dropdown menu
    // RepeatOnLifecycle only collects data when the fragment is in the STARTED state to prevent
    // UI updates when the fragment is not visible
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
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

    // Update the dropdown menu adapter
    private fun updateAdapter(medications: List<Medication>) {
        adapter.clear()
        adapter.addAll(medications.map { it.name })
        asNeededMedIds.clear()
        asNeededMedIds.addAll(medications.map { it.id })
    }

    // Hide the as-needed medication inputs
    private fun hideAsNeededInputs() {
        with (binding) {
            layoutAddAsNeededMed.visibility = View.GONE
            noAsNeededMedsMessage.visibility = View.GONE
            addNewButton.visibility = View.GONE
            asNeededMedDropdownWrapper.visibility = View.GONE
            layoutDosage.visibility = View.GONE
            layoutAsNeededDateTime.visibility = View.GONE
        }
    }

    // Show the as-needed medication inputs
    private fun showAsNeededInputs() {
        with (binding) {
            layoutAddAsNeededMed.visibility = View.VISIBLE
            noAsNeededMedsMessage.visibility = View.GONE
            addNewButton.visibility = View.VISIBLE
            asNeededMedDropdownWrapper.visibility = View.VISIBLE
            layoutDosage.visibility = View.VISIBLE
            layoutAsNeededDateTime.visibility = View.VISIBLE
        }
    }

    // Setup listeners for the UI elements
    private fun setupListeners() {
        with (binding) {
            asNeededMedDropdown.setOnItemClickListener { _, _, position, _ ->
                viewModel.setSelectedAsNeededMedication(asNeededMedIds.getOrNull(position))
            }

            addNewButton.setOnClickListener { addNewAsNeededMed() }
            buttonAsNeededDateTaken.setOnClickListener { showDatePicker() }
            buttonAsNeededTimeTaken.setOnClickListener { showTimePicker() }
            buttonCancelAddAsNeededMed.setOnClickListener { dismiss() }

            buttonSetAddAsNeededMed.setOnClickListener {
                addAsNeededMedication()
                dismiss()
            }
        }
    }

    // Add as-needed medication to the database
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
                // Refresh medications for the current date
                viewModel.fetchMedicationsForDate(viewModel.selectedDate.value)
                dismiss()
            } catch (e: Exception) {
                Log.e("HistoryViewModel testcat", "Error adding as needed log", e)
            }
        }
    }

    // Start AddMedicationActivity to add a new as-needed medication
    private fun addNewAsNeededMed() {
        val intent = Intent(requireContext(), AddMedicationActivity::class.java)
        intent.putExtra("ADD_AS_NEEDED", true)
        startActivity(intent)
    }

    // Show time picker dialog
    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setInputMode(INPUT_MODE_CLOCK)
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText(SELECT_TIME_TAKEN)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute

            viewModel.setTimeTaken(hour, minute)
            updateTimePickerButtonText()
        }

        timePicker.show(parentFragmentManager, "tag")
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

    // Show date picker dialog
    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(SELECT_DATE_TAKEN)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            viewModel.setDateTaken(selection)
            updateDateButtonText()
        }

        datePicker.show(parentFragmentManager, "tag")
    }

    // Update the date picker button text
    private fun updateDateButtonText() {
        val date = viewModel.dateTaken.value
        val formattedDate = DateTimeFormatter.ofPattern("MMM d, yyyy")
        binding.buttonAsNeededDateTaken.text = date?.format(formattedDate)
    }

    companion object {
        private const val SELECT_TIME_TAKEN = "Select time medication was taken"
        private const val SELECT_DATE_TAKEN = "Select date medication was taken"
    }
}