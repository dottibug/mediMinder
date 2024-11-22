package com.example.mediminder.utils

import android.content.Context
import android.content.res.Resources
import android.widget.ArrayAdapter
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ActivityHistoryBinding
import com.example.mediminder.viewmodels.HistoryViewModel

// Medication dropdown utils for the HistoryActivity
class HistoryMedicationDropdownUtils(
    private val context: Context,
    private val binding: ActivityHistoryBinding,
    private val viewModel: HistoryViewModel,
    private val resources: Resources
) {
    private val medicationIds = mutableListOf<Long?>()
    private lateinit var medicationAdapter: ArrayAdapter<String>

    // Set up the medication dropdown, which allows users to view history of a specific medication or all medications
    fun setupMedicationDropdown() {
        medicationAdapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        binding.medicationDropdown.setAdapter(medicationAdapter)
        setDefaultMedicationDropdown()

        binding.medicationDropdown.setOnItemClickListener { _, _, position, _ ->
            viewModel.setSelectedMedication(medicationIds.getOrNull(position))
        }
    }

    // Set the default medication in the dropdown to "All Medications"
    private fun setDefaultMedicationDropdown() {
        if (medicationIds.isEmpty()) {
            binding.medicationDropdown.setText(resources.getString(R.string.all_meds), false)
        } else {
            binding.medicationDropdown.setText(medicationAdapter.getItem(0), false)
        }
    }

    // Update the list of medication IDs used in the dropdown menu
    fun updateMedicationIds(medications: List<Medication>) {
        medicationIds.apply {
            clear()
            add(null)   // For "All Medications"
            addAll(medications.map { it.id })
        }
    }

    // Create the medication dropdown menu with the list of medications fetched
    fun createMedicationDropdown(medications: List<Medication>) {
        val dropdownItems = mutableListOf(ALL_MEDS)
        dropdownItems.addAll(medications.map { it.name })
        medicationAdapter.apply {
            clear()
            addAll(dropdownItems)
            notifyDataSetChanged()
        }
    }

    companion object {
        private const val ALL_MEDS = "All Medications"
    }
}