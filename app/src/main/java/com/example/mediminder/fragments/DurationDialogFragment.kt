package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mediminder.databinding.FragmentDurationDialogBinding

class DurationDialogFragment(
    private val parentFragment: AddMedicationScheduleFragment,
    private val editingNumDays: Boolean
) : DialogFragment() {
    private lateinit var binding: FragmentDurationDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDurationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancelDurationDialog.setOnClickListener {
            if (!editingNumDays) { parentFragment.setDurationRadioToContinuous() }
            dismiss()
        }

        binding.buttonSetDurationDialog.setOnClickListener {
            val numDays = binding.inputDurationNumOfDays.text.toString()
            parentFragment.setDurationNumDays(numDays)
            dismiss()
        }
    }

    companion object {
        const val TAG = "DurationDialogFragment"
    }
}