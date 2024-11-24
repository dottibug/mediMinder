package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.databinding.ActivityAddMedicationBinding
import com.example.mediminder.models.MedicationAction
import com.example.mediminder.utils.AppUtils.cancelActivity
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.Constants.ADD
import com.example.mediminder.utils.Constants.ADD_AS_NEEDED
import com.example.mediminder.utils.Constants.ERR_UNEXPECTED
import kotlinx.coroutines.launch

/**
 * Activity for adding a new medication to the database. Handles both scheduled and as-needed
 * medications, collecting the relevant date from the MedicationInfo, Dosage, Schedule, and Reminder
 * fragments.
 */
class AddMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityAddMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
        getAsNeededIntent()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    /**
     * Set up activity bindings and BaseActivity components
     * (top app bar, navigation drawer, error observer)
     */
    private fun setupActivity() {
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
    }

    /**
     * Sets up state flow observers to update UI based on medication type (scheduled vs as-needed)
     * Dynamically shows/hides dosage, reminder, and schedule fragments based on asScheduled value
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.medication.asScheduled.collect {
                    scheduled -> setFragmentVisibility(scheduled)
                }
            }
        }
    }

    /**
     * Update fragment visibility based on medication type (scheduled vs as-needed)
     * As-needed medications hide the dosage, reminder, and schedule fragments
     * @param scheduled Whether the medication is scheduled or not
     */
    private fun setFragmentVisibility(scheduled: Boolean) {
        with (binding) {
            fragmentDosage.visibility = if (scheduled) View.VISIBLE else View.GONE
            fragmentAddMedReminder.visibility = if (scheduled) View.VISIBLE else View.GONE
            fragmentAddMedSchedule.visibility = if (scheduled) View.VISIBLE else View.GONE
        }
    }

    /**
     * Check if the ADD_AS_NEEDED flag is set in the intent. If it is, set asScheduled to false
     */
    private fun getAsNeededIntent() {
        if (intent.getBooleanExtra(ADD_AS_NEEDED, false)) {
            appViewModel.setAsScheduled(false)
        }
    }

    /**
     * Click listeners for the add medication and cancel buttons
     */
    private fun setupListeners() {
        binding.buttonAddMed.setOnClickListener { handleAddMedication() }
        binding.buttonCancelAddMed.setOnClickListener { cancelActivity(this) }
    }

    /**
     * Add medication to the database. Handles both scheduled and as-needed medications.
     * - Collects data from the relevant fragments
     * - Validates data
     * - Saves medication to the database if valid
     * - Creates workers to set up reminders
     * - Shows errors and success messages
     */
    private fun handleAddMedication() {
        lifecycleScope.launch {
            try {
                val medData = getMedicationData(MedicationAction.ADD)
                val asScheduled = appViewModel.medication.asScheduled.value
                val dosageData = if (asScheduled) getDosageData(MedicationAction.ADD) else null
                val reminderData = if (asScheduled) appViewModel.reminder.getReminders() else null
                val scheduleData = if (asScheduled) appViewModel.schedule.getSchedule() else null

                if (medData != null && (dosageData != null || !asScheduled)) {

                    val success = appViewModel.saveMedication(ADD, medData, dosageData,
                        reminderData, scheduleData)

                    if (success) {
                        createToast(this@AddMedicationActivity, MED_ADDED)
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, ERR_UNEXPECTED, e)
                appViewModel.setErrorMessage(e.message ?: ERR_UNEXPECTED)
            }
        }
    }

    companion object {
        private const val TAG = "AddMedicationActivity"
        private const val MED_ADDED = "Medication added successfully!"
    }
}