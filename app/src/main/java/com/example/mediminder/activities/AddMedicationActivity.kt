package com.example.mediminder.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.databinding.ActivityAddMedicationBinding
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.Constants.ADD_AS_NEEDED
import com.example.mediminder.utils.Constants.ERR_UNEXPECTED
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.SHOW
import kotlinx.coroutines.launch

// Activity to add a new medication
class AddMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityAddMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
        setupObservers()
        checkAsNeededIntent()
    }

    // Set up listeners and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    // Set up bindings for this activity
    private fun setupActivity() {
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding, binding.loadingSpinner)
    }

    // Hide dosage, reminder, and schedule fragments if the activity is started with ADD_AS_NEEDED flag
    private fun checkAsNeededIntent() {
        if (intent.getBooleanExtra(ADD_AS_NEEDED, false)) {
            medicationViewModel.setAsScheduled(false)
        }
    }

    // Click listeners
    private fun setupListeners() {
        binding.buttonAddMed.setOnClickListener { handleAddMedication() }

        binding.buttonCancelAddMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    // Update UI when asScheduled changes
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectAsScheduled() }
                launch { collectErrorMessage() }
                launch { collectLoadingSpinner() }
                launch { collectIsAdding() }
            }
        }
    }

    // Collect asScheduled state
    private suspend fun collectAsScheduled() {
        medicationViewModel.asScheduled.collect { asScheduled ->
            updateFragmentVisibility(asScheduled)
        }
    }

    // Collect error message state
    private suspend fun collectErrorMessage() {
        medicationViewModel.errorMessage.collect { errMsg ->
            if (errMsg != null) {
                createToast(this@AddMedicationActivity, errMsg)
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

    // Collect isAdding state
    private suspend fun collectIsAdding() {
        medicationViewModel.isAdding.collect { isAdding ->
            if (isAdding) { toggleContentVisibility(HIDE) }
            else { toggleContentVisibility(SHOW) }
        }
    }

    // Toggle the visibility of the layout based on the action
    private fun toggleContentVisibility(action: String) {
        with (binding) {
            fragmentAddMedInfo.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            fragmentAddMedDosage.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            fragmentAddMedSchedule.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            fragmentAddMedReminder.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonAddMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonCancelAddMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        }
    }

    // Update visibility of dosage, reminder, and schedule fragments based on asScheduled value
    private fun updateFragmentVisibility(asScheduled: Boolean) {
        with (binding) {
            fragmentAddMedDosage.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentAddMedReminder.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentAddMedSchedule.visibility = if (asScheduled) View.VISIBLE else View.GONE
        }
    }

    // Add medication to the database
    private fun handleAddMedication() {
        lifecycleScope.launch {
            try {
                val medData = getMedicationData(MedicationAction.ADD)
                val isAsScheduled = medicationViewModel.asScheduled.value
                val dosageData = if (isAsScheduled) getDosageData(MedicationAction.ADD) else null
                val reminderData = if (isAsScheduled) medicationViewModel.getReminderData() else null
                val scheduleData = if (isAsScheduled) medicationViewModel.getScheduleData() else null

                // Add medication if med data is not null (dosage data can be null if it is
                // an as-needed medication)
                if (medData != null && (dosageData != null || !isAsScheduled)) {
                    val success = medicationViewModel.addMedication(
                        medData,
                        dosageData,
                        reminderData,
                        scheduleData
                    )

                    if (success) {
                        createToast(this@AddMedicationActivity, MED_ADDED)
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            } catch (e: Exception) {
                medicationViewModel.setErrorMessage(e.message ?: ERR_UNEXPECTED)
            }
        }
    }

    companion object {
        private const val MED_ADDED = "Medication added successfully!"
    }
}