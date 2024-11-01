package com.example.mediminder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.mediminder.data.local.classes.MedicationStatus
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
        binding = FragmentUpdateMedicationStatusDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("UpdateDialog testcat", "Dialog created with logId: $logId")


        binding.radioGroupMedStatus.setOnCheckedChangeListener { _, checkedId ->
            selectedStatus = when (checkedId) {
                binding.radioButtonTaken.id -> MedicationStatus.TAKEN
                binding.radioButtonSkipped.id -> MedicationStatus.SKIPPED
                binding.radioButtonMissed.id -> MedicationStatus.MISSED
                else -> MedicationStatus.PENDING
            }
            Log.d("UpdateDialog testcat", "Selected status changed to: $selectedStatus")
        }

        binding.buttonCancelMedStatusDialog.setOnClickListener { dismiss() }

        binding.buttonSetMedStatusDialog.setOnClickListener {
            Log.d("UpdateDialog testcat", "Set button clicked. Updating logId: $logId with status: $selectedStatus")

            viewModel.updateMedicationLogStatus(logId, selectedStatus)
            dismiss()
        }
    }

    companion object {
        fun newInstance(logId: Long): UpdateMedicationStatusDialogFragment {
            return UpdateMedicationStatusDialogFragment().apply {
                this.logId = logId
            }
        }
    }

//    companion object {
//        fun newInstance(
//            medId: Long,
//            onStatusUpdate: (MedicationStatus) -> Unit
//        ): UpdateMedicationStatusDialogFragment {
//            return UpdateMedicationStatusDialogFragment().apply {
//                this.medicationId = medId
//                this.onStatusUpdate = onStatusUpdate
//            }
//        }
//    }
}