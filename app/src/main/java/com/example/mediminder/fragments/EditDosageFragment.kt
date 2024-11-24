package com.example.mediminder.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Fragment for editing a medication's dosage. Pre-populates the dosage fields with the selected
 * medication data
 */
class EditDosageFragment : DosageFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
    }

    /**
     * Observe the current medication dosage and update the UI with the medication data
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            appViewModel.medication.current.collect { medication ->
                medication?.dosage?.let { dosage ->
                    binding.inputDosage.setText(dosage.amount)
                    binding.dosageUnitsDropdown.setText(dosage.units, false)
                }
            }
        }
    }
}