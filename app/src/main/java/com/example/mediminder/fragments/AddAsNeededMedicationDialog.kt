package com.example.mediminder.fragments

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
import com.example.mediminder.utils.AppUtils.createDatePicker
import com.example.mediminder.utils.AppUtils.createTimePicker
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.AppUtils.updateDatePickerButtonText
import com.example.mediminder.utils.AppUtils.updateTimePickerButtonText
import com.example.mediminder.utils.Constants.DATE_PICKER_TAG
import com.example.mediminder.utils.Constants.TIME_PICKER_TAG
import com.example.mediminder.utils.ValidationUtils.getValidatedAsNeededData
import com.example.mediminder.viewmodels.MainViewModel
import kotlinx.coroutines.launch

// Dialog fragment for adding an as-needed medication
class AddAsNeededMedicationDialog: DialogFragment() {
    private lateinit var binding: FragmentAddAsNeededMedBinding
    private lateinit var adapter: ArrayAdapter<String>
    private val viewModel: MainViewModel by activityViewModels()
    private val asNeededMedIds = mutableListOf<Long?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
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
            Log.e("AddAsNeededMedicationDialog testcat", "Error fetching as needed medications", e)
        }
    }

    // Setup UI elements
    private fun setupUI() {
        binding.asNeededMedDropdown.setAdapter(adapter)
        setupObservers()
        setupListeners()
    }

    // RepeatOnLifecycle only collects data when the fragment is in the STARTED state to prevent
    // UI updates when the fragment is not visible
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectAsNeededMeds() }
                launch { collectErrorMessage() }
            }
        }
    }

    // Collect as-needed medications from the view model
    private suspend fun collectAsNeededMeds() {
        viewModel.asNeededMedications.collect { medications ->
            if (medications.isEmpty()) { hideAsNeededInputs() }
            else {
                showAsNeededInputs()
                updateAdapter(medications)
            }
        }
    }

    // Collect error messages from the view model
    private suspend fun collectErrorMessage() {
        viewModel.errorMessage.collect { msg ->
            if (msg != null) {
                createToast(requireContext(), msg)
                viewModel.clearError()
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
            layoutAddAsNeededContent.visibility = View.VISIBLE
            layoutAddAsNeededMed.visibility = View.VISIBLE
            textViewAsNeededMedLabel.visibility = View.GONE
            asNeededMedDropdownWrapper.visibility = View.GONE
            addNewButton.visibility = View.VISIBLE
            textViewDosage.visibility = View.GONE
            layoutDosage.visibility = View.GONE
            layoutAsNeededDateTime.visibility = View.GONE
        }
    }

    // Show the as-needed medication inputs
    private fun showAsNeededInputs() {
        with (binding) {
            layoutAddAsNeededMed.visibility = View.VISIBLE
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
                val validAsNeededMed = addAsNeededMedication()
                if (validAsNeededMed) { dismiss() }
            }
        }
    }

    // Add as-needed medication to the database
    private fun addAsNeededMedication(): Boolean {
        return try {
            val selectedMedId = viewModel.selectedAsNeededMedId.value
            val dosageAmount = binding.asNeededDosageAmount.text.toString()
            val dosageUnits = binding.asNeededDosageUnits.text.toString()
            val dateTaken = viewModel.dateTaken.value
            val timeTaken = viewModel.timeTaken.value

            // Validate input
            val validatedData = getValidatedAsNeededData(
                selectedMedId,
                dosageAmount,
                dosageUnits,
                dateTaken,
                timeTaken
            )

            lifecycleScope.launch {
                try {
                    viewModel.addAsNeededLog(validatedData)
                    viewModel.fetchMedicationsForDate(viewModel.selectedDate.value)
                } catch (e: Exception) {
                    Log.e("AddAsNeededMedicationDialog testcat", "Error adding as needed log", e)
                    viewModel.setErrorMessage(e.message ?: "Error adding medication log")
                }
            }
            true
        } catch (e: IllegalArgumentException) {
            Log.e("AddAsNeededMedicationDialog testcat", "Invalid input", e)
            viewModel.setErrorMessage(e.message ?: "Error validating input")
            false
        } catch (e: Exception) {
            Log.e("AddAsNeededMedicationDialog testcat", "Error adding as needed log", e)
            viewModel.setErrorMessage(e.message ?: "Error adding an as-needed medication")
            false
        }
    }

    // Start AddMedicationActivity to add a new as-needed medication
    private fun addNewAsNeededMed() {
        val intent = Intent(requireContext(), AddMedicationActivity::class.java)
        intent.putExtra(ADD_AS_NEEDED, true)
        startActivity(intent)
    }

    // Show time picker dialog
    private fun showTimePicker() {
        val timePicker = createTimePicker(SELECT_TIME_TAKEN)

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            viewModel.setTimeTaken(hour, minute)
            updateTimePickerButtonText(hour, minute, binding.buttonAsNeededTimeTaken)
        }

        timePicker.show(parentFragmentManager, TIME_PICKER_TAG)
    }

    // Show date picker dialog
    private fun showDatePicker() {
        val datePicker = createDatePicker(SELECT_DATE_TAKEN)

        datePicker.addOnPositiveButtonClickListener { selection ->
            viewModel.setDateTaken(selection)
            updateDatePickerButtonText(viewModel.dateTaken.value, binding.buttonAsNeededDateTaken)
        }

        datePicker.show(parentFragmentManager, DATE_PICKER_TAG)
    }

    companion object {
        private const val ADD_AS_NEEDED = "ADD_AS_NEEDED"
        private const val SELECT_TIME_TAKEN = "Select time medication was taken"
        private const val SELECT_DATE_TAKEN = "Select date medication was taken"
    }
}