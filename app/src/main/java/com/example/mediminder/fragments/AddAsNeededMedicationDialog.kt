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
import com.example.mediminder.utils.AppUtils.updateDatePickerButtonText
import com.example.mediminder.utils.AppUtils.updateTimePickerButtonText
import com.example.mediminder.utils.Constants.DATE_PICKER_TAG
import com.example.mediminder.utils.Constants.ERR_ADDING_AS_NEEDED_LOG
import com.example.mediminder.utils.Constants.ERR_FETCHING_AS_NEEDED_MEDS
import com.example.mediminder.utils.Constants.ERR_VALIDATING_INPUT
import com.example.mediminder.utils.Constants.TIME_PICKER_TAG
import com.example.mediminder.utils.ValidationUtils.getValidatedAsNeededData
import com.example.mediminder.viewmodels.AppViewModel
import com.example.mediminder.viewmodels.MainViewModel
import kotlinx.coroutines.launch

/**
 * Dialog fragment for adding an as-needed medication.
 */
class AddAsNeededMedicationDialog: DialogFragment() {
    private lateinit var binding: FragmentAddAsNeededMedBinding
    private lateinit var adapter: ArrayAdapter<String>
    private val mainViewModel: MainViewModel by activityViewModels()
    private val appViewModel: AppViewModel by activityViewModels { AppViewModel.Factory }
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
        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    /**
     * Fetch medications on resume to update dropdown menu of as-needed medications
     */
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try { mainViewModel.fetchAsNeededMedications() }
            catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_AS_NEEDED_MEDS, e)
                appViewModel.setErrorMessage(ERR_FETCHING_AS_NEEDED_MEDS)
            }
        }
    }

    private fun setupUI() {
        binding.asNeededMedDropdown.setAdapter(adapter)
        setupListeners()
    }

    /**
     * Collect as-needed medication data from the view model.
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.asNeededMedications.collect { medications ->
                    if (medications.isEmpty()) { hideAsNeededInputs() }
                    else {
                        showAsNeededInputs()
                        updateAdapter(medications)
                    }
                }
            }
        }
    }

    /**
     * Update the dropdown menu adapter with the list of medications.
     */
    private fun updateAdapter(medications: List<Medication>) {
        adapter.clear()
        adapter.addAll(medications.map { it.name })
        asNeededMedIds.clear()
        asNeededMedIds.addAll(medications.map { it.id })
    }

    /**
     * Helper functions to show/hide the as-needed medication inputs based on whether
     * there are any medications
     */
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

    private fun showAsNeededInputs() {
        with (binding) {
            layoutAddAsNeededMed.visibility = View.VISIBLE
            addNewButton.visibility = View.VISIBLE
            asNeededMedDropdownWrapper.visibility = View.VISIBLE
            layoutDosage.visibility = View.VISIBLE
            layoutAsNeededDateTime.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        with (binding) {
            asNeededMedDropdown.setOnItemClickListener { _, _, position, _ ->
                mainViewModel.setSelectedAsNeededMedication(asNeededMedIds.getOrNull(position))
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

    /**
     * Add as-needed medication to the database.
     */
    private fun addAsNeededMedication(): Boolean {
        return try {
            val selectedMedId = mainViewModel.selectedAsNeededMedId.value
            val dosageAmount = binding.asNeededDosageAmount.text.toString()
            val dosageUnits = binding.asNeededDosageUnits.text.toString()
            val dateTaken = mainViewModel.dateTaken.value
            val timeTaken = mainViewModel.timeTaken.value

            val validatedData = getValidatedAsNeededData(
                selectedMedId,
                dosageAmount,
                dosageUnits,
                dateTaken,
                timeTaken
            )

            mainViewModel.addAsNeededLog(validatedData)
            mainViewModel.fetchMedicationsForDate(mainViewModel.selectedDate.value)
            true
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, ERR_VALIDATING_INPUT, e)
            appViewModel.setErrorMessage(e.message ?: ERR_VALIDATING_INPUT)
            false
        } catch (e: Exception) {
            Log.e(TAG, ERR_ADDING_AS_NEEDED_LOG, e)
            appViewModel.setErrorMessage(e.message ?: ERR_ADDING_AS_NEEDED_LOG)
            false
        }
    }

    private fun addNewAsNeededMed() {
        val intent = Intent(requireContext(), AddMedicationActivity::class.java)
        intent.putExtra(ADD_AS_NEEDED, true)
        startActivity(intent)
    }

    private fun showTimePicker() {
        val timePicker = createTimePicker(SELECT_TIME_TAKEN)
        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            mainViewModel.setTimeTaken(hour, minute)
            updateTimePickerButtonText(hour, minute, binding.buttonAsNeededTimeTaken)
        }
        timePicker.show(parentFragmentManager, TIME_PICKER_TAG)
    }

    private fun showDatePicker() {
        val datePicker = createDatePicker(SELECT_DATE_TAKEN)
        datePicker.addOnPositiveButtonClickListener { selection ->
            mainViewModel.setDateTaken(selection)
            updateDatePickerButtonText(mainViewModel.dateTaken.value, binding.buttonAsNeededDateTaken)
        }
        datePicker.show(parentFragmentManager, DATE_PICKER_TAG)
    }

    companion object {
        private const val TAG = "AddAsNeededMedicationDialog"
        private const val ADD_AS_NEEDED = "ADD_AS_NEEDED"
        private const val SELECT_TIME_TAKEN = "Select time medication was taken"
        private const val SELECT_DATE_TAKEN = "Select date medication was taken"
    }
}