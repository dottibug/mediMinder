package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mediminder.databinding.FragmentDaysIntervalDialogBinding

class DaysIntervalDialogFragment(
    private val parentFragment: BaseScheduleFragment,
    private val editingDaysInterval: Boolean
) : DialogFragment() {
    private lateinit var binding: FragmentDaysIntervalDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDaysIntervalDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editingDaysInterval) {
            // todo
        }

        binding.buttonCancelDaysIntervalDialog.setOnClickListener {
            if (!editingDaysInterval) { parentFragment.setScheduleTypeToDaily() }
            dismiss()
        }

        binding.buttonSetDaysIntervalDialog.setOnClickListener {
            val daysInterval = binding.inputIntervalDays.text.toString()
            parentFragment.setDaysInterval(daysInterval)
            dismiss()
        }
    }

    companion object {
        val TAG = "DaysIntervalDialogFragment"
    }
}