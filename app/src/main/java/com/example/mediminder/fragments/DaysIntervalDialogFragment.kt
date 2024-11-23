package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mediminder.databinding.FragmentDaysIntervalDialogBinding

// Dialog fragment for setting the days interval of a medication schedule
class DaysIntervalDialogFragment(
    private val parentFragment: ScheduleFragment,
    private val editingDaysInterval: Boolean
) : DialogFragment() {
    private lateinit var binding: FragmentDaysIntervalDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentDaysIntervalDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editingDaysInterval) {
            val initialDaysInterval = parentFragment.scheduleViewModel.daysInterval.value
            binding.inputIntervalDays.setText(initialDaysInterval.toString())
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonCancelDaysIntervalDialog.setOnClickListener {
            if (!editingDaysInterval) { parentFragment.setScheduleTypeToDaily() }
            dismiss()
        }

        binding.buttonSetDaysIntervalDialog.setOnClickListener {
            val daysInterval = binding.inputIntervalDays.text.toString()

            if (daysInterval.isEmpty()) {
                if (!editingDaysInterval) { parentFragment.setScheduleTypeToDaily() }
                dismiss()
            } else {
                parentFragment.setDaysInterval(daysInterval)
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "DaysIntervalDialogFragment"
    }
}