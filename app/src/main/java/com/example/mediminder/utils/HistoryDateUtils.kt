package com.example.mediminder.utils

import androidx.fragment.app.FragmentManager
import com.example.mediminder.databinding.ActivityHistoryBinding
import com.example.mediminder.viewmodels.HistoryViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Date utility functions for the HistoryActivity
 */
class HistoryDateUtils(
    private val binding: ActivityHistoryBinding,
    private val viewModel: HistoryViewModel,
    private val supportFragmentManager: FragmentManager
) {

    // Date control buttons to navigate through months
    fun setupDateControls() {
        with (binding) {
            buttonPreviousMonth.setOnClickListener { viewModel.moveMonth(forward = false) }
            buttonNextMonth.setOnClickListener { viewModel.moveMonth(forward = true) }
            buttonCalendar.setOnClickListener { showMonthYearPicker() }
        }
    }

    // Show a date picker to allow users to select a month
    private fun showMonthYearPicker() {
        val currentDate = viewModel.selectedMonth.value

        MaterialDatePicker.Builder.datePicker()
            .setSelection(
                currentDate
                    .atDay(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
            .setTitleText(SELECT_MONTH)
            .build()
            .apply {
                addOnPositiveButtonClickListener { selection ->
                    val selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    viewModel.setSelectedMonth(YearMonth.from(selectedDate))
                }
            }
            .show(supportFragmentManager, MONTH_YEAR_PICKER)
    }

    // Update the text displayed to show the user-selected month
    fun updateMonthYearText(yearMonth: YearMonth) {
        val formatter = DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT)
        binding.monthYearText.text = yearMonth.format(formatter)
    }

    companion object {
        private const val MONTH_YEAR_PICKER = "MONTH_YEAR_PICKER"
        private const val SELECT_MONTH = "Select Month"
        private const val MONTH_YEAR_FORMAT = "MMMM yyyy"
    }
}