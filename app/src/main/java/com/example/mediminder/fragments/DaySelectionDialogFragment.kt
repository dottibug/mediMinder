package com.example.mediminder.fragments

import android.os.Bundle
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
            parentFragment.setSelectedDays(selectedDays)
            dismiss()
        }
    }

    private fun checkSpecificDays(specificDays: List<String>) {
        // [ Sunday, Monday, Tuesday ]
        val checkboxes = listOf(
            binding.checkboxSunday,
            binding.checkboxMonday,
            binding.checkboxTuesday,
            binding.checkboxWednesday,
            binding.checkboxThursday,
            binding.checkboxFriday,
            binding.checkboxSaturday
        )

        for (checkbox in checkboxes) {
            val checkboxDay = checkbox.text.toString()
            checkbox.isChecked = specificDays.contains(checkboxDay)
        }
    }

    private fun getSelectedDays(): String {
        val days = mutableListOf<String>()

        val checkboxes = listOf(
            binding.checkboxSunday,
            binding.checkboxMonday,
            binding.checkboxTuesday,
            binding.checkboxWednesday,
            binding.checkboxThursday,
            binding.checkboxFriday,
            binding.checkboxSaturday
        )

        // Return the index of the checked checkboxes (0 = Sunday, 1 = Monday, etc.)
        for (i in checkboxes.indices) {
            if (checkboxes[i].isChecked) { days.add(i.toString()) }
        }
        return days.joinToString(",") // "0,1,2" -> Sunday, Monday, Tuesday
    }

    companion object {
        const val TAG = "DaySelectionDialogFragment"
    }
}