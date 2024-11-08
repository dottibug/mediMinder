package com.example.mediminder.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mediminder.databinding.FragmentDaySelectionDialogBinding
import com.example.mediminder.utils.AppUtils.getDayNames

class DaySelectionDialogFragment(
    private val parentFragment: BaseScheduleFragment,
    private val editingDaySelection: Boolean,
    private val selectedDays: String = ""
//    private val selectedDays: List<String> = emptyList()
) : DialogFragment() {
    private lateinit var binding: FragmentDaySelectionDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaySelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editingDaySelection) { checkSpecificDays(selectedDays) }

        binding.buttonCancelDaySelectionDialog.setOnClickListener {
            if (!editingDaySelection) { parentFragment.setScheduleTypeToDaily() }
            dismiss()
        }

        binding.buttonSetDaySelectionDialog.setOnClickListener {
            val selectedDays = getSelectedDays()
            Log.d("testcat days", "Selected days: $selectedDays")
            parentFragment.updateSelectedDays(selectedDays)
            dismiss()
        }
    }

    private fun checkSpecificDays(specificDays: String) {
        // Convert string "1,3,5" to list of days [Monday, Wednesday, Friday]
        val dayNames = getDayNames(specificDays)

        val checkboxMap = mapOf(
            "Monday" to binding.checkboxMonday,
            "Tuesday" to binding.checkboxTuesday,
            "Wednesday" to binding.checkboxWednesday,
            "Thursday" to binding.checkboxThursday,
            "Friday" to binding.checkboxFriday,
            "Saturday" to binding.checkboxSaturday,
            "Sunday" to binding.checkboxSunday
        )

        for (day in dayNames) {
            checkboxMap[day]?.isChecked = true
        }
    }

    private fun getSelectedDays(): String {

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