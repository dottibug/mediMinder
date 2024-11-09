package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.databinding.FragmentUpdateMedicationStatusDialogBinding
import com.example.mediminder.viewmodels.MainViewModel

// Dialog fragment for updating the status of a medication
// Note: This fragment should only be constructed using the newInstance method to ensure the
//  correct medicationId is passed to the fragment from the clicked recycler view item
class UpdateMedicationStatusDialogFragment: DialogFragment() {
    private lateinit var binding: FragmentUpdateMedicationStatusDialogBinding
    private val viewModel: MainViewModel by activityViewModels { MainViewModel.Factory }
    private var logId: Long = 0
    private var selectedStatus: MedicationStatus = MedicationStatus.TAKEN

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentUpdateMedicationStatusDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.radioGroupMedStatus.setOnCheckedChangeListener { _, checkedId ->
            selectedStatus = when (checkedId) {
                binding.radioButtonTaken.id -> MedicationStatus.TAKEN
                binding.radioButtonSkipped.id -> MedicationStatus.SKIPPED
                binding.radioButtonMissed.id -> MedicationStatus.MISSED
                else -> MedicationStatus.PENDING
            }
        }

        binding.buttonCancelMedStatusDialog.setOnClickListener { dismiss() }

        binding.buttonSetMedStatusDialog.setOnClickListener {
            viewModel.updateMedicationLogStatus(logId, selectedStatus)
            dismiss()
        }
    }

    companion object {
        fun newInstance(logId: Long): UpdateMedicationStatusDialogFragment {
            return UpdateMedicationStatusDialogFragment().apply { this.logId = logId }
        }
    }
}