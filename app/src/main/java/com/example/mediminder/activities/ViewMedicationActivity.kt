package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.MedicationIcon
import com.example.mediminder.databinding.ActivityViewMedicationBinding
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.utils.AppUtils.daysOfWeekString
import com.example.mediminder.utils.AppUtils.formatLocalTimeTo12Hour
import com.example.mediminder.utils.AppUtils.formatToLongDate
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.ViewMedicationViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ViewMedicationActivity(): BaseActivity() {
    private val viewModel: ViewMedicationViewModel by viewModels { ViewMedicationViewModel.Factory }
    private lateinit var binding: ActivityViewMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()

        val medicationId = intent.getLongExtra("medicationId", -1)
        checkMedicationId(medicationId)

        setupUI(medicationId)
        setupObservers()

        lifecycleScope.launch { fetchMedication(medicationId) }
    }

    override fun onResume() {
        super.onResume()
        val medicationId = intent.getLongExtra("medicationId", -1)
        if (medicationId != -1L) {
            lifecycleScope.launch { fetchMedication(medicationId) }
        }
    }

    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityViewMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    private fun checkMedicationId(medId: Long) {
        if (medId == -1L) {
            finish()
            return
        }
    }

    private fun setupUI(medicationId: Long) {
        binding.buttonEditMed.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            intent.putExtra("medicationId", medicationId)
            startActivity(intent)
        }

        binding.buttonDeleteMed.setOnClickListener {
            val intent = Intent(this, DeleteMedicationActivity::class.java)
            intent.putExtra("medicationId", medicationId)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.medication.collect { medicationDetails ->
                medicationDetails?.let { setupMedicationDetails(it) }
            }
        }
    }

    private fun setupMedicationDetails(details: MedicationWithDetails) {
        setIcon(details)
        setName(details)
        setDosage(details)
        setDoctor(details)
        setNotes(details)
        setReminders(details)
        setSchedule(details)
        setStartDate(details)
        setEndDate(details)
    }

    private fun setIcon(details: MedicationWithDetails) {
        val icon = details.medication.icon

        val iconResId = when (icon) {
            MedicationIcon.CAPSULE -> R.drawable.capsule
            MedicationIcon.DROP -> R.drawable.drop
            MedicationIcon.INHALER -> R.drawable.inhaler
            MedicationIcon.INJECTION -> R.drawable.injection
            MedicationIcon.LIQUID -> R.drawable.liquid
            MedicationIcon.TABLET -> R.drawable.tablet
        }
        binding.medIcon.setImageResource(iconResId)
    }

    private fun setName(details: MedicationWithDetails) {
        binding.medNameContent.text = resources.getString(R.string.view_med_name_content, details.medication.name)
    }

    private fun setDosage(details: MedicationWithDetails) {
        val amount = details.dosage?.amount
        val unit = details.dosage?.units
        val dosage = "$amount $unit"
        binding.medDosageContent.text = resources.getString(R.string.view_med_dosage_content, dosage)
    }

    private fun setDoctor(details: MedicationWithDetails) {
        if (details.medication.prescribingDoctor.isNullOrEmpty()) {
            binding.medDoctor.visibility = View.GONE
        } else {
            binding.medDoctor.visibility = View.VISIBLE
            binding.medDoctorContent.text = resources.getString(R.string.view_med_doctor_content, details.medication.prescribingDoctor)
        }
    }

    private fun setNotes(details: MedicationWithDetails) {
        if (details.medication.notes.isNullOrEmpty()) {
            binding.medNotes.visibility = View.GONE
        } else {
            binding.medNotes.visibility = View.VISIBLE
            binding.medNotesContent.text = resources.getString(R.string.view_med_notes_content, details.medication.notes)
        }
    }

    private fun setReminders(details: MedicationWithDetails) {
        if (details.medication.reminderEnabled) {
            binding.medReminder.visibility = View.VISIBLE

            when (details.reminders?.reminderFrequency) {
                "daily" -> setDailyReminders(details.reminders)
                "every x hours" -> setHourlyReminders(details.reminders)
            }
        } else { binding.medReminder.visibility = View.GONE }
    }

    private fun setDailyReminders(reminders: MedReminders) {
        val remindersList = reminders.dailyReminderTimes

        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        val formattedTimes = remindersList.map { it.format(formatter).lowercase() }

        val timesString = when (formattedTimes.size) {
            1 -> formattedTimes[0]
            2 -> "${formattedTimes[0]} and ${formattedTimes[1]}"
            else -> formattedTimes.dropLast(1).joinToString(", ") + ", and ${formattedTimes.last()}"
        }

        val reminderString = "Daily reminders at $timesString"
        binding.medReminderContent.text = resources.getString(R.string.view_med_reminder_content, reminderString)
    }

    private fun setHourlyReminders(reminders: MedReminders) {
        val interval = reminders.hourlyReminderInterval

        val startTime = reminders.hourlyReminderStartTime ?: LocalTime.now()
        val startTimeString = formatLocalTimeTo12Hour(startTime)

        val endTime = reminders.hourlyReminderEndTime ?: LocalTime.of(23, 59) // default to midnight
        val endTimeString = formatLocalTimeTo12Hour(endTime)

        val reminderString = "Hourly reminders every $interval from $startTimeString to $endTimeString"
        binding.medReminderContent.text = resources.getString(R.string.view_med_reminder_content, reminderString)
    }

    private fun setSchedule(details: MedicationWithDetails) {
        val scheduleType = details.schedule?.scheduleType

        val scheduleTypeString = when (scheduleType) {
            "daily" -> "daily"
            "specificDays" -> "every " + daysOfWeekString(details.schedule.selectedDays)
            "daysIntervals" -> daysIntervalString(details.schedule.daysInterval)
            else -> ""
        }

        val durationString = when (details.schedule?.durationType) {
            "numDays" -> "for ${details.schedule.numDays.toString()} days"
            else -> ""
        }

        val scheduleString = "Take $scheduleTypeString $durationString"
        binding.medScheduleContent.text = resources.getString(R.string.view_med_schedule_content, scheduleString)
    }

    private fun daysIntervalString(daysInterval: Int?): String {
        return when (daysInterval) {
            1 -> "every day"
            else -> "every ${daysInterval.toString()} days"
        }
    }

    private fun setStartDate(details: MedicationWithDetails) {
        val startDate = details.schedule?.startDate
        val startDateString = formatToLongDate(startDate ?: LocalDate.now())
        binding.medStartDateContent.text = resources.getString(R.string.view_med_start_date_content, startDateString)
    }

    private fun setEndDate(details: MedicationWithDetails) {
        if (details.schedule?.numDays == null) {
            binding.medEndDate.visibility = View.GONE
        } else {
            binding.medEndDate.visibility = View.VISIBLE
            val startDate = details.schedule.startDate
            val numDays = details.schedule.numDays
            val endDate = startDate.plusDays(numDays.toLong())
            val endDateString = formatToLongDate(endDate ?: LocalDate.now())
            binding.medEndDateContent.text = resources.getString(R.string.view_med_end_date_content, endDateString)
        }
    }

    private suspend fun fetchMedication(medicationId: Long) {
        loadingSpinnerUtil.whileLoading {
            try {
                viewModel.fetchMedication(medicationId)
            } catch (e: Exception) {
                Log.e("ViewMedicationActivity testcat", "Error fetching medication", e)
            }
        }
    }

}