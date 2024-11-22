package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.databinding.FragmentUpdateMedicationStatusDialogBinding
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.viewmodels.MainViewModel
import kotlinx.coroutines.launch

// Dialog fragment for updating the status of a medication
// Note: This fragment should only be constructed using the newInstance method to ensure the
//  correct medicationId is passed to the fragment from the clicked medication in the recycler view
class UpdateMedicationStatusDialogFragment: DialogFragment() {
    private lateinit var binding: FragmentUpdateMedicationStatusDialogBinding
    private val viewModel: MainViewModel by activityViewModels { MainViewModel.Factory }
    private var logId: Long = 0
    private var selectedStatus: MedicationStatus = MedicationStatus.TAKEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setInitialMedStatus(logId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentUpdateMedicationStatusDialogBinding.inflate(inflater, container, false)
        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        // Radio group listener
        binding.radioGroupMedStatus.setOnCheckedChangeListener { _, checkedId ->
            selectedStatus = when (checkedId) {
                binding.radioButtonTaken.id -> MedicationStatus.TAKEN
                binding.radioButtonSkipped.id -> MedicationStatus.SKIPPED
                binding.radioButtonMissed.id -> MedicationStatus.MISSED
                else -> MedicationStatus.PENDING
            }
        }

        // Cancel button listener
        binding.buttonCancelMedStatusDialog.setOnClickListener { dismiss() }

        // Set button listener
        binding.buttonSetMedStatusDialog.setOnClickListener {
            viewModel.updateMedicationLogStatus(logId, selectedStatus)
            dismiss()
        }
    }

    // Observe initial status and set radio button
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectInitialStatus() }
            }
        }
    }

    // Collect initial status and set status radio button
    private suspend fun collectInitialStatus() {
        viewModel.initialMedStatus.collect { status ->
            status?.let {
                val radioButtonId = when (it) {
                    MedicationStatus.TAKEN -> binding.radioButtonTaken.id
                    MedicationStatus.SKIPPED -> binding.radioButtonSkipped.id
                    MedicationStatus.MISSED -> binding.radioButtonMissed.id
                    else -> binding.radioButtonTaken.id  // Default to taken if no status is found
                }
                binding.radioGroupMedStatus.check(radioButtonId)
                selectedStatus = it
            }
        }
    }

    companion object {
        fun newInstance(logId: Long): UpdateMedicationStatusDialogFragment {
            return UpdateMedicationStatusDialogFragment().apply { this.logId = logId }
        }
    }
}