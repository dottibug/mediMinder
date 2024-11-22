package com.example.mediminder.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.databinding.ActivityEditMedicationBinding
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.AppUtils.getMedicationId
import com.example.mediminder.utils.Constants.ERR_UNEXPECTED
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.SHOW
import kotlinx.coroutines.launch

// This activity allows the user to edit an existing medication. It uses the MedicationViewModel to
// fetch and update medication data.
class EditMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityEditMedicationBinding
    private var medicationId: Long = -1L

    // Initialize the ViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = getMedicationId(this) ?: return
        setupActivity()
        setupObservers()
    }

    // Set up listeners and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    // Fetch medication data when the activity is resumed
    override fun onResume() {
        super.onResume()
        fetchMedicationData()
    }

    // Set up bindings for this activity
    private fun setupActivity() {
        binding = ActivityEditMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding, binding.loadingSpinner)
    }

    // Click listeners for buttons
    private fun setupListeners() {
        binding.buttonUpdateMed.setOnClickListener { handleUpdateMedication(medicationId) }

        binding.buttonCancelUpdateMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // Update UI when asScheduled changes
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectAsScheduled() }
                launch { collectCurrentMedication() }
                launch { collectErrorMessage() }
                launch { collectLoadingSpinner() }
            }
        }
    }

    // Collect asScheduled state
    private suspend fun collectAsScheduled() {
        medicationViewModel.asScheduled.collect { asScheduled ->
            updateFragmentVisibility(asScheduled)
        }
    }

    // Collect current medication state
    private suspend fun collectCurrentMedication() {
        medicationViewModel.currentMedication.collect { med ->
            if (med == null) { toggleContentVisibility(HIDE) }
            else { toggleContentVisibility(SHOW) }
        }
    }

    // Collect error message state
    private suspend fun collectErrorMessage() {
        medicationViewModel.errorMessage.collect { errMsg ->
            if (errMsg != null) {
                createToast(this@EditMedicationActivity, errMsg)
                medicationViewModel.clearError()
            }
        }
    }

    // Collect loading spinner state
    private suspend fun collectLoadingSpinner() {
        medicationViewModel.isLoading.collect { isLoading ->
            if (isLoading) loadingSpinnerUtil.show() else loadingSpinnerUtil.hide()
        }
    }

    // Toggle the visibility of the layout and edit/delete buttons based on the action
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

    // Update visibility of dosage, reminder, and schedule fragments based on asScheduled value
    private fun updateFragmentVisibility(asScheduled: Boolean) {
        with (binding) {
            fragmentEditMedDosage.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedReminder.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedSchedule.visibility = if (asScheduled) View.VISIBLE else View.GONE
        }
    }

    // Update medication data and finish the activity
    private fun handleUpdateMedication(medicationId: Long) {
        lifecycleScope.launch {
            try {
                val medData = getMedicationData(MedicationAction.EDIT)
                val asScheduled = medicationViewModel.asScheduled.value
                val dosageData = if (asScheduled) getDosageData(MedicationAction.EDIT) else null
                val reminderData = if (asScheduled) medicationViewModel.getReminderData() else null
                val scheduleData = if (asScheduled) medicationViewModel.getScheduleData() else null

                // Update medication if med data is not null (dosage data can be null if it is
                // an as-needed medication)
                if (medData != null && (dosageData != null || !asScheduled)) {
                    val success = medicationViewModel.updateMedication(
                        medicationId,
                        medData,
                        dosageData,
                        reminderData,
                        scheduleData
                    )

                    if (success) {
                        createToast(this@EditMedicationActivity, MED_UPDATED)
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } catch (e: Exception) {
                medicationViewModel.setErrorMessage(e.message ?: ERR_UNEXPECTED)
            }
        }
    }

    // Fetch medication data from the ViewModel (no need to catch errors here, as they are handled
    // in the ViewModel and the error observer for this activity will handle showing the error message)
    private fun fetchMedicationData() {
        lifecycleScope.launch {
            medicationViewModel.fetchMedication(medicationId)
        }
    }

    companion object {
        private const val MED_UPDATED = "Medication updated successfully!"
    }
}