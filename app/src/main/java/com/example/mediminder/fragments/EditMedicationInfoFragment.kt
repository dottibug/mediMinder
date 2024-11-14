package com.example.mediminder.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import kotlinx.coroutines.launch

class EditMedicationInfoFragment : BaseMedicationInfoFragment() {
    override val medicationViewModel: BaseMedicationViewModel by activityViewModels { BaseMedicationViewModel.Factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide the as-needed switch (users cannot change as-needed medications to
        // a scheduled medication and vice versa)
        binding.asNeededSwitch.visibility = View.GONE

        val isAsNeeded = medicationViewModel.asNeeded.value

        if (isAsNeeded) {
            binding.asNeededMessage.visibility = View.VISIBLE
            binding.asNeededMessage.text = "This medication is taken as needed."
        } else {
            binding.asNeededMessage.visibility = View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            medicationViewModel.currentMedication.collect { medication ->
                medication?.let {
                    binding.inputMedName.setText(it.medication.name)
                    binding.inputDoctor.setText(it.medication.prescribingDoctor)
                    binding.inputMedNotes.setText(it.medication.notes)

                    val iconName = it.medication.icon.name.lowercase().replaceFirstChar { it.uppercase() }
                    binding.medicationIconDropdown.setText(iconName, false)
                }
            }
        }
    }
}