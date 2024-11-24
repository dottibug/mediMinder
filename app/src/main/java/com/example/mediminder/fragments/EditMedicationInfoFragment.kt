package com.example.mediminder.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.Medication
import kotlinx.coroutines.launch

/**
 * Fragment for editing a medication's information. Pre-populates the medication fields with the
 * selected medication data
 */
class EditMedicationInfoFragment : MedicationInfoFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAsNeededUI()
        setupObserver()
    }

    /**
     * Hide the switch (users cannot change an as-needed med to scheduled and vice versa)
     * Show/hide the as-needed message based on the state of the switch
     */
    private fun setupAsNeededUI() {
        val asScheduled = appViewModel.medication.asScheduled.value
        binding.asScheduledSwitch.visibility = View.GONE

        binding.asNeededMessage.apply {
            visibility = if (asScheduled) View.GONE else View.VISIBLE
            text = getString(R.string.msg_taken_as_needed)
        }
    }

    /**
     * Observe the current medication and update the UI with the medication data
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            appViewModel.medication.current.collect { medication ->
                medication?.let { updateUIWithMedicationData(it.medication) }
            }
        }
    }

    private fun updateUIWithMedicationData(medication: Medication) {
        with (binding) {
            inputMedName.setText(medication.name)
            inputDoctor.setText(medication.prescribingDoctor)
            inputMedNotes.setText(medication.notes)

            val iconName = medication.icon.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            medicationIconDropdown.setText(iconName, false)
        }
    }
}