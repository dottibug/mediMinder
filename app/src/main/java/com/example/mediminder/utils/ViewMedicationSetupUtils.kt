package com.example.mediminder.utils

import android.content.res.Resources
import android.view.View
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.databinding.ActivityViewMedicationBinding
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.utils.AppUtils.formatToLongDate
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EVERY_X_HOURS
import com.example.mediminder.utils.StringUtils.getDailyReminderString
import com.example.mediminder.utils.StringUtils.getHourlyReminderString
import com.example.mediminder.utils.StringUtils.getScheduleString
import java.time.LocalDate
import java.time.LocalTime

// This class handles the UI setup for the ViewMedicationActivity
class ViewMedicationSetupUtils(
    private val binding: ActivityViewMedicationBinding,
    private val resources: Resources
) {
    fun setupMedicationDetails(details: MedicationWithDetails) {
        with(details) {
            setIcon(medication.icon)
            setName(medication.name)
            setDosage(dosage)
            setDoctor(medication.prescribingDoctor)
            setNotes(medication.notes)
            setReminders(reminders)
            setSchedule(schedule, medication.asNeeded)
        }
    }

    // Helper function to set the icon of the medication
    private fun setIcon(icon: MedicationIcon) {
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

    private fun setName(name: String) {
        binding.medNameContent.text = resources.getString(R.string.dynamic_text, name)
    }

    private fun setDosage(dosage: Dosage?) {
        if (dosage == null) { binding.medDosage.visibility = View.GONE }
        else {
            binding.medDosage.visibility = View.VISIBLE
            val amount = dosage.amount
            val unit = dosage.units
            val dosageString = "$amount $unit"
            binding.medDosageContent.text = resources.getString(R.string.dynamic_text, dosageString)
        }
    }

    private fun setDoctor(doctor: String?) {
        if (doctor.isNullOrEmpty()) { binding.medDoctor.visibility = View.GONE }
        else {
            binding.medDoctor.visibility = View.VISIBLE
            binding.medDoctorContent.text = resources.getString(R.string.dynamic_text, doctor)
        }
    }

    private fun setNotes(notes: String?) {
        if (notes.isNullOrEmpty()) { binding.medNotes.visibility = View.GONE }
        else {
            binding.medNotes.visibility = View.VISIBLE
            binding.medNotesContent.text = resources.getString(R.string.dynamic_text, notes)
        }
    }

    private fun setReminders(reminders: MedReminders?) {
        if (reminders == null) { binding.medReminder.visibility = View.GONE }
        else {
            binding.medReminder.visibility = View.VISIBLE

            with(reminders) {
                when (reminderFrequency) {
                    DAILY -> setDailyReminders(dailyReminderTimes)
                    EVERY_X_HOURS -> setHourlyReminders(
                        hourlyReminderInterval,
                        hourlyReminderStartTime,
                        hourlyReminderEndTime)
                }
            }
        }
    }

    private fun setSchedule(schedule: Schedules?, asNeeded: Boolean) {
        if (schedule == null) {
            binding.medScheduleContent.text = resources.getString(R.string.take_as_needed)
            binding.medStartDate.visibility = View.GONE
            binding.medEndDate.visibility = View.GONE
        }
        else {
            binding.medSchedule.visibility = View.VISIBLE
            val scheduleString = getScheduleString(schedule, asNeeded)
            binding.medScheduleContent.text = resources.getString(R.string.dynamic_text, scheduleString)
            setStartDate(schedule.startDate)
            setEndDate(schedule)
        }
    }

    // Helper function to set daily reminders
    private fun setDailyReminders(remindersList: List<LocalTime>) {
        val reminderString = getDailyReminderString(remindersList)
        binding.medReminderContent.text = resources.getString(R.string.dynamic_text, reminderString)
    }

    // Helper function to set hourly reminders
    private fun setHourlyReminders(interval: String?, startTime: LocalTime?, endTime: LocalTime?) {
        if (interval == null || startTime == null || endTime == null) {
            binding.medReminder.visibility = View.GONE
            return
        } else {
            val reminderString = getHourlyReminderString(interval, startTime, endTime)
            binding.medReminderContent.text = resources.getString(R.string.dynamic_text, reminderString)
        }
    }

    // Helper function to set start date
    private fun setStartDate(startDate: LocalDate) {
        binding.medStartDate.visibility = View.VISIBLE
        val startDateString = formatToLongDate(startDate)
        binding.medStartDateContent.text = resources.getString(R.string.dynamic_text, startDateString)
    }

    // Helper function to set end date
    private fun setEndDate(schedule: Schedules?) {
        if (schedule?.durationType == CONTINUOUS) {
            binding.medEndDate.visibility = View.GONE
        } else {
            binding.medEndDate.visibility = View.VISIBLE
            val startDate = schedule?.startDate
            val numDays = schedule?.numDays
            val endDate = numDays?.let { startDate?.plusDays(it.toLong()) }
            val endDateString = formatToLongDate(endDate ?: LocalDate.now())
            binding.medEndDateContent.text = resources.getString(R.string.dynamic_text, endDateString)
        }
    }
}