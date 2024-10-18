package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.FragmentAddAsNeededMedicationDialogBinding
import com.example.mediminder.viewmodels.MainViewModel
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddAsNeededMedicationDialog : DialogFragment() {

    private var _binding: FragmentAddAsNeededMedicationDialogBinding? = null
    private val binding get() = _binding!! // access only non-null _binding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the shared element transition animation
        // https://github.com/material-components/material-components-android/blob/master/docs/theming/Motion.md#using-the-container-transform-pattern
        sharedElementEnterTransition = MaterialContainerTransform()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAsNeededMedicationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Dynamically render the existing as-needed medications section (only renders if there are
        // as-needed medications in the database)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.asNeededMedications.collectLatest { medications ->
                if (medications.isNotEmpty()) {
                    binding.existingMedicationsSection.visibility = View.VISIBLE
                    // Populate dropdown with existing medications
                } else {
                    binding.existingMedicationsSection.visibility = View.GONE
                }
            }
        }

    }

    private fun showExistingAsNeededMedicationsDialog() {
        Toast.makeText(requireContext(), "Existing medication dialog not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun showNewMedicationDialog() {
        Toast.makeText(requireContext(), "New medication dialog not implemented yet", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Tag for the dialog fragment
        const val TAG = "AddAsNeededMedicationDialog"
    }
}