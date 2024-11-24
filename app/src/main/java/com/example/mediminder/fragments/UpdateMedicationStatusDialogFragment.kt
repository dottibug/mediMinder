package com.example.mediminder.fragments

import android.os.Bundle
import android.util.Log
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
import com.example.mediminder.utils.Constants.ERR_UPDATING_STATUS
import com.example.mediminder.utils.Constants.ERR_UPDATING_STATUS_USER
import com.example.mediminder.viewmodels.AppViewModel
import com.example.mediminder.viewmodels.MainViewModel
import kotlinx.coroutines.launch

/**
 * Dialog fragment for updating the status of a medication
 * Note: This fragment should only be constructed using the newInstance method to ensure the
 * correct medicationId is passed to the fragment from the clicked medication in the recycler view
 */
class UpdateMedicationStatusDialogFragment: DialogFragment() {
    private lateinit var binding: FragmentUpdateMedicationStatusDialogBinding
    private val mainViewModel: MainViewModel by activityViewModels { MainViewModel.Factory }
    private val appViewModel: AppViewModel by activityViewModels { AppViewModel.Factory }
    private var logId: Long = 0
    private var selectedStatus: MedicationStatus = MedicationStatus.TAKEN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.setInitialMedStatus(logId)
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
            try {
                mainViewModel.updateMedicationLogStatus(logId, selectedStatus)
                dismiss()
            } catch (e: Exception) {
                Log.e(TAG, ERR_UPDATING_STATUS, e)
                appViewModel.setErrorMessage(ERR_UPDATING_STATUS_USER)
            }

        }
    }

    /**
     * Collect state flow from main view model when the fragment is in the STARTED state
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectInitialStatus()
            }
        }
    }

    /**
     * Collect the initial medication status from the main view model and set the status radio button
     */
    private suspend fun collectInitialStatus() {
        mainViewModel.initialMedStatus.collect { status ->
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
        private const val TAG = "UpdateMedicationStatusDialogFragment"

        fun newInstance(logId: Long): UpdateMedicationStatusDialogFragment {
            return UpdateMedicationStatusDialogFragment().apply { this.logId = logId }
        }
    }
}