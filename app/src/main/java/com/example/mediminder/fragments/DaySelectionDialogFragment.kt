package com.example.mediminder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.mediminder.databinding.FragmentDaySelectionDialogBinding
import com.example.mediminder.viewmodels.AddMedicationScheduleViewModel

class DaySelectionDialogFragment(
    private val parentFragment: AddMedicationScheduleFragment,
    private val editingDaySelection: Boolean,
    private val selectedDays: List<String> = emptyList()
) : DialogFragment() {
    private lateinit var binding: FragmentDaySelectionDialogBinding
    private val viewModel: AddMedicationScheduleViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDaySelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editingDaySelection) {
            checkSpecificDays(selectedDays)
        }

        binding.buttonCancelDaySelectionDialog.setOnClickListener {
            if (!editingDaySelection) { parentFragment.setScheduleTypeToDaily() }
            dismiss()
        }

        binding.buttonSetDaySelectionDialog.setOnClickListener {
            val selectedDays = getSelectedDays()
            Log.d("testcat days", "Selected days: $selectedDays")
            parentFragment.setSelectedDays(selectedDays)
            dismiss()
        }
    }

    private fun checkSpecificDays(specificDays: List<String>) {
        // [ Sunday, Monday, Tuesday ]
        val checkboxes = listOf(
            binding.checkboxMonday,     // 1
            binding.checkboxTuesday,    // 2
            binding.checkboxWednesday,  // 3
            binding.checkboxThursday,   // 4
            binding.checkboxFriday,     // 5
            binding.checkboxSaturday,   // 6
            binding.checkboxSunday,     // 7
        )

        for ((index, checkbox) in checkboxes.withIndex()) {
            val dayValue = (index + 1).toString()
            checkbox.isChecked = specificDays.contains(dayValue)
        }
    }

    private fun getSelectedDays(): String {
        val days = mutableListOf<String>()

        val checkboxes = listOf(
            binding.checkboxMonday,
            binding.checkboxTuesday,
            binding.checkboxWednesday,
            binding.checkboxThursday,
            binding.checkboxFriday,
            binding.checkboxSaturday,
            binding.checkboxSunday,
        )

        return checkboxes.mapIndexedNotNull { index, checkbox ->
            if (checkbox.isChecked) (index + 1).toString() else null
        }.joinToString(",")
    }

    companion object {
        const val TAG = "DaySelectionDialogFragment"
    }
}