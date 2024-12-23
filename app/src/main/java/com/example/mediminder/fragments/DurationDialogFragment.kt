package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mediminder.databinding.FragmentDurationDialogBinding


/**
 * Dialog fragment for setting the duration of a medication schedule.
 */
class DurationDialogFragment(
    private val parentFragment: ScheduleFragment,
    private val editingNumDays: Boolean
) : DialogFragment() {
    private lateinit var binding: FragmentDurationDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentDurationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editingNumDays) {
            val initialNumDays = parentFragment.scheduleViewModel.numDays.value
            binding.inputDurationNumOfDays.setText(initialNumDays.toString())
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonCancelDurationDialog.setOnClickListener {
            if (!editingNumDays) { parentFragment.setDurationRadioToContinuous() }
            dismiss()
        }

        binding.buttonSetDurationDialog.setOnClickListener {
            val numDays = binding.inputDurationNumOfDays.text.toString()

            if (numDays.isEmpty()) {
                if (!editingNumDays) { parentFragment.setDurationRadioToContinuous() }
                dismiss()
            } else {
                parentFragment.setDurationNumDays(numDays)
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "DurationDialogFragment"
    }
}