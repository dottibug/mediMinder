package com.example.mediminder.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import kotlinx.coroutines.launch

class EditDosageFragment : BaseDosageFragment() {
    val medicationViewModel: BaseMedicationViewModel by activityViewModels { BaseMedicationViewModel.Factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            medicationViewModel.currentMedication.collect { medication ->
                medication?.dosage?.let { dosage ->
                    binding.inputDosage.setText(dosage.amount)
                    binding.dosageUnitsDropdown.setText(dosage.units, false)
                }
            }
        }
    }
}