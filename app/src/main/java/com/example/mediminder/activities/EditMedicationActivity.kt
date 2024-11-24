package com.example.mediminder.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.databinding.ActivityEditMedicationBinding
import com.example.mediminder.models.MedicationAction
import com.example.mediminder.utils.AppUtils.cancelActivity
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.AppUtils.getMedicationId
import com.example.mediminder.utils.Constants.ERR_UNEXPECTED
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.SHOW
import com.example.mediminder.utils.Constants.UPDATE
import kotlinx.coroutines.launch

/**
 * Activity for editing an existing medication. Users can edit medication information, dosage,
 * schedule, and reminders.
 */
class EditMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityEditMedicationBinding
    private var medicationId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = getMedicationId(this) ?: return
        setupActivity()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchMedicationData()
    }

    /**
     * Set up activity bindings and BaseActivity components
     * (top app bar, navigation drawer, error observer)
     */
    private fun setupActivity() {
        binding = ActivityEditMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
    }

    private fun setupListeners() {
        binding.buttonUpdateMed.setOnClickListener { handleUpdateMedication(medicationId) }
        binding.buttonCancelUpdateMed.setOnClickListener { cancelActivity(this) }
    }

    /**
     * Set up state flow observers to update UI based on medication type (scheduled vs as-needed)
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectAsScheduled() }
                launch { collectCurrentMedication() }
            }
        }
    }

    /**
     * Collect asScheduled state from AppViewModel to show/hide relevant fragments
     * (dosage, reminder, and schedule fragments are hidden for as-needed medications)
     */
    private suspend fun collectAsScheduled() {
        appViewModel.medication.asScheduled.collect { asScheduled -> updateFragmentVisibility(asScheduled) }
    }

    /**
     * Collect current medication state from AppViewModel to show/hide the content layout
     */
    private suspend fun collectCurrentMedication() {
        appViewModel.medication.current.collect { med ->
            if (med == null) { toggleContentVisibility(HIDE) }
            else { toggleContentVisibility(SHOW) }
        }
    }

    /**
     * Helper function to toggle the visibility of the content layout and edit/delete buttons
     * @param action The action to perform (SHOW or HIDE)
     */
    private fun toggleContentVisibility(action: String) {
        with (binding) {
            fragmentEditMedInfo.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            fragmentEditMedDosage.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            fragmentEditMedSchedule.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            fragmentEditMedReminder.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonUpdateMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonCancelUpdateMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        }
    }

    /**
     * Helper function to toggle the visibility of the dosage, reminder, and schedule fragments
     * @param asScheduled Whether the medication is scheduled or not
     */
    private fun updateFragmentVisibility(asScheduled: Boolean) {
        with (binding) {
            fragmentEditMedDosage.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedReminder.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedSchedule.visibility = if (asScheduled) View.VISIBLE else View.GONE
        }
    }

    /**
     * Update medication in the database.
     * - Collect medication data from the relevant fragments
     * - Validate data
     * - Update medication in the database if valid
     * - Show errors and success messages
     * @param medicationId The ID of the medication to update
     */
    private fun handleUpdateMedication(medicationId: Long) {
        lifecycleScope.launch {
            try {
                val medData = getMedicationData(MedicationAction.EDIT)
                val asScheduled = appViewModel.medication.asScheduled.value
                val dosageData = if (asScheduled) getDosageData(MedicationAction.EDIT) else null
                val reminderData = if (asScheduled) appViewModel.reminder.getReminders() else null
                val scheduleData = if (asScheduled) appViewModel.schedule.getSchedule() else null

                if (medData != null && (dosageData != null || !asScheduled)) {
                    val success = appViewModel.saveMedication(UPDATE, medData, dosageData,
                        reminderData, scheduleData, medicationId)

                    if (success) {
                        createToast(this@EditMedicationActivity, MED_UPDATED)
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } catch (e: Exception) {
                appViewModel.setErrorMessage(e.message ?: ERR_UNEXPECTED)
            }
        }
    }

    /**
     * Fetch medication details from the database. Errors are handled by the error observer in
     * BaseActivity
     */
    private fun fetchMedicationData() {
        lifecycleScope.launch { appViewModel.fetchMedicationDetails(medicationId) }
    }

    companion object {
        private const val MED_UPDATED = "Medication updated successfully!"
    }
}