package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.MedReminders
import com.example.mediminder.data.local.classes.Schedules
import com.example.mediminder.databinding.ActivityViewMedicationBinding
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.AppUtils.daysOfWeekString
import com.example.mediminder.utils.AppUtils.formatLocalTimeTo12Hour
import com.example.mediminder.utils.AppUtils.formatToLongDate
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.Constants.CONTINUOUS
import com.example.mediminder.utils.Constants.DAILY
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.EVERY_X_HOURS
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.INTERVAL
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.Constants.NUM_DAYS
import com.example.mediminder.utils.Constants.SHOW
import com.example.mediminder.utils.Constants.SPECIFIC_DAYS
import com.example.mediminder.utils.Constants.TIME_PATTERN
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.ViewMedicationViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// This activity displays the details of a medication, including icon, name, dosage, doctor, notes,
// reminders, schedule, start date, and end date. It uses the ViewMedicationViewModel to fetch the
// medication details.
class ViewMedicationActivity(): BaseActivity() {
    private val viewModel: ViewMedicationViewModel by viewModels { ViewMedicationViewModel.Factory }
    private lateinit var binding: ActivityViewMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private var medicationId: Long = -1L

    // Initialize variables and setup bindings
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = intent.getLongExtra(MED_ID, -1L)
        if (medicationId == -1L) { finish() }
        setupBindings()
        setupObservers()
    }

    // Set up listeners and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    // Fetch/refresh medication data (observers will update UI when data is available)
    override fun onResume() {
        super.onResume()
        fetchMedicationData(medicationId)
    }

    // Set up bindings for the base class, then inflate this view into the base layout
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityViewMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    private fun setupListeners() {
        binding.buttonEditMed.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            intent.putExtra(MED_ID, medicationId)
            startActivity(intent)
        }

        binding.buttonDeleteMed.setOnClickListener {
            val intent = Intent(this, DeleteMedicationActivity::class.java)
            intent.putExtra(MED_ID, medicationId)
            startActivity(intent)
        }
    }

    // Set up observers to update the UI when state flow changes
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMedication() }
                launch { collectErrorMessage() }
                launch { collectLoadingSpinner() }
            }
        }
    }

    // Collect medication details from the view model
    private suspend fun collectMedication() {
        viewModel.medication.collect { medicationDetails ->
            if (medicationDetails == null) { toggleContentVisibility(HIDE) }

            else {
                toggleContentVisibility(SHOW)
                setupMedicationDetails(medicationDetails)
            }
        }
    }

    // Collect error messages from the view model and display them as toasts
    private suspend fun collectErrorMessage() {
        viewModel.errorMessage.collect { msg ->
            if (msg != null) {
                createToast(this@ViewMedicationActivity, msg)
                viewModel.clearError()
            }
        }
    }

    // Collect loading spinner state
    private suspend fun collectLoadingSpinner() {
        viewModel.isLoading.collect { isLoading ->
            if (isLoading) loadingSpinnerUtil.show() else loadingSpinnerUtil.hide()
        }
    }

    // Toggle the visibility of the layout and edit/delete buttons based on the action
    private fun toggleContentVisibility(action: String) {
        binding.layoutMedSummary.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        binding.layoutEditDeleteButtons.visibility = if (action == HIDE) View.GONE else View.VISIBLE
    }

    // Fetch medication data from the ViewModel (no need to catch errors here, as they are handled
    // in the ViewModel and the error observer for this activity will handle showing the error message)
    private fun fetchMedicationData(medicationId: Long) {
        lifecycleScope.launch {
            viewModel.fetchMedication(medicationId)
        }
    }

    // -------------------------------------------------------------------------------------------
    // UI Setup Functions
    // -------------------------------------------------------------------------------------------
    private fun setupMedicationDetails(details: MedicationWithDetails) {
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

    private fun setDailyReminders(remindersList: List<LocalTime>) {
        val reminderString = getDailyReminderString(remindersList)
        binding.medReminderContent.text = resources.getString(R.string.dynamic_text, reminderString)
    }

    private fun setHourlyReminders(interval: String?, startTime: LocalTime?, endTime: LocalTime?) {
        if (interval == null || startTime == null || endTime == null) {
            binding.medReminder.visibility = View.GONE
            return
        } else {
            val reminderString = getHourlyReminderString(interval, startTime, endTime)
            binding.medReminderContent.text = resources.getString(R.string.dynamic_text, reminderString)
        }
    }

    // Helper function to format daily reminders into a string
    private fun getDailyReminderString(remindersList: List<LocalTime>): String {
        val formatter = DateTimeFormatter.ofPattern(TIME_PATTERN, Locale.ENGLISH)
        val times = remindersList.map { it.format(formatter).lowercase() }

        val timeString = when (times.size) {
            1 -> times[0]
            2 -> "${times[0]} and ${times[1]}"
            else -> times.dropLast(1).joinToString(", ") + ", and ${times.last()}"
        }
        return "Daily reminders at $timeString"
    }

    // Helper function to format hourly reminders into a string
    private fun getHourlyReminderString(interval: String, startTime: LocalTime, endTime: LocalTime): String {
        val startTimeString = formatLocalTimeTo12Hour(startTime)
        val endTimeString = formatLocalTimeTo12Hour(endTime)
        return "Hourly reminders every $interval from $startTimeString to $endTimeString"
    }

    private fun setSchedule(schedule: Schedules?, asNeeded: Boolean) {
        if (schedule == null) {
            binding.medScheduleContent.text = getString(R.string.take_as_needed)
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

    private fun setStartDate(startDate: LocalDate) {
        binding.medStartDate.visibility = View.VISIBLE
        val startDateString = formatToLongDate(startDate)
        binding.medStartDateContent.text = resources.getString(R.string.dynamic_text, startDateString)
    }

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

    // Helper function to format schedule into a string
    private fun getScheduleString(schedule: Schedules, asNeeded: Boolean): String {
        val scheduleTypeString = getScheduleTypeString(schedule.scheduleType, asNeeded, schedule)
        val durationString = getDurationString(schedule)
        return "Take $scheduleTypeString $durationString"
    }

    // Helper function to format schedule type into a string
    private fun getScheduleTypeString(scheduleType: String, asNeeded: Boolean, schedule: Schedules): String {
        return when (scheduleType) {
            DAILY -> if (asNeeded) "as needed" else DAILY
            SPECIFIC_DAYS -> "every " + daysOfWeekString(schedule.selectedDays)
            INTERVAL -> getDaysIntervalString(schedule.daysInterval)
            else -> ""
        }
    }

    // Helper function to format days interval into a string
    private fun getDaysIntervalString(daysInterval: Int?): String {
        return when (daysInterval) {
            1 -> "every day"
            else -> "every ${daysInterval.toString()} days"
        }
    }

    // Helper function to format duration type into a string
    private fun getDurationString(schedule: Schedules): String {
        return when (schedule.durationType) {
            NUM_DAYS -> "for ${schedule.numDays.toString()} days"
            else -> EMPTY_STRING
        }
    }
}