package com.example.mediminder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.databinding.FragmentUpdateMedicationStatusDialogBinding

// Dialog fragment for updating the status of a medication
// Note: This fragment should only be constructed using the newInstance method to ensure the
//  correct medicationId is passed to the fragment from the clicked recycler view item
class UpdateMedicationStatusDialogFragment: DialogFragment() {
    private lateinit var binding: FragmentUpdateMedicationStatusDialogBinding
    private lateinit var newMedStatus: MedicationStatus
    private lateinit var onStatusUpdate: (MedicationStatus) -> Unit
    private var medicationId: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdateMedicationStatusDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.radioGroupMedStatus.setOnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = binding.radioGroupMedStatus.findViewById<RadioButton>(checkedId)

            when (checkedRadioButton.text) {
                "Taken" -> newMedStatus = MedicationStatus.TAKEN
                "Skipped" -> newMedStatus = MedicationStatus.SKIPPED
                "Missed" -> newMedStatus = MedicationStatus.MISSED
            }

            Log.i("testcat", "newMedStatus on radio button selection: $newMedStatus")
        }

        binding.buttonCancelMedStatusDialog.setOnClickListener { dismiss() }

        binding.buttonSetMedStatusDialog.setOnClickListener {
            if (::newMedStatus.isInitialized) { onStatusUpdate(newMedStatus) }
            dismiss()
        }
    }

    companion object {
        fun newInstance(
            medId: Long,
            onStatusUpdate: (MedicationStatus) -> Unit
        ): UpdateMedicationStatusDialogFragment {
            return UpdateMedicationStatusDialogFragment().apply {
                this.medicationId = medId
                this.onStatusUpdate = onStatusUpdate
            }
        }
    }
}