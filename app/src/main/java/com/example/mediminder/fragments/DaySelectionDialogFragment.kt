package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mediminder.databinding.FragmentDaySelectionDialogBinding
import com.example.mediminder.utils.AppUtils.getDayNames
import com.example.mediminder.utils.Constants.EMPTY_STRING
import java.time.DayOfWeek

/**
 * Dialog fragment for selecting the days of the week for a medication schedule.
 */
class DaySelectionDialogFragment(
    private val parentFragment: ScheduleFragment,
    private val editingDaySelection: Boolean,
    private val selectedDays: String = EMPTY_STRING
) : DialogFragment() {
    private lateinit var binding: FragmentDaySelectionDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentDaySelectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (editingDaySelection) { setupSpecificDays(selectedDays) }
        setupListeners()
    }

    // Select the relevant checkboxes for the specific days
    private fun setupSpecificDays(specificDays: String) {
        // Convert string "1,3,5" to list of days [Monday, Wednesday, Friday]
        val dayNames = getDayNames(specificDays)

        // Map of checkbox names to checkboxes
        val checkboxMap = mapOf(
            "Monday" to binding.checkboxMonday,
            "Tuesday" to binding.checkboxTuesday,
            "Wednesday" to binding.checkboxWednesday,
            "Thursday" to binding.checkboxThursday,
            "Friday" to binding.checkboxFriday,
            "Saturday" to binding.checkboxSaturday,
            "Sunday" to binding.checkboxSunday
        )

        // Set the checkboxes for the specific days
        for (day in dayNames) { checkboxMap[day]?.isChecked = true }
    }

    private fun setupListeners() {
        binding.buttonCancelDaySelectionDialog.setOnClickListener {
            if (!editingDaySelection) { parentFragment.setScheduleTypeToDaily() }
            dismiss()
        }

        binding.buttonSetDaySelectionDialog.setOnClickListener {
            val selectedDays = getSelectedDays()
            parentFragment.updateSelectedDays(selectedDays)
            dismiss()
        }
    }

    // Get the selected days as a comma-separated string of day of the week integers (ex. "1,3,5")
    private fun getSelectedDays(): String {
        val dayOfWeekMap = mapOf(
            binding.checkboxMonday to DayOfWeek.MONDAY.value,
            binding.checkboxTuesday to DayOfWeek.TUESDAY.value,
            binding.checkboxWednesday to DayOfWeek.WEDNESDAY.value,
            binding.checkboxThursday to DayOfWeek.THURSDAY.value,
            binding.checkboxFriday to DayOfWeek.FRIDAY.value,
            binding.checkboxSaturday to DayOfWeek.SATURDAY.value,
            binding.checkboxSunday to DayOfWeek.SUNDAY.value
        )

        return dayOfWeekMap.entries
            .filter { it.key.isChecked }
            .joinToString(",") { it.value.toString() }
    }

    companion object {
        const val TAG = "DaySelectionDialogFragment"
    }
}