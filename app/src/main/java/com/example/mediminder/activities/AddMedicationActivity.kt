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
import com.example.mediminder.utils.Constants.ADD_AS_NEEDED
import com.example.mediminder.utils.Constants.ERR_UNEXPECTED
import kotlinx.coroutines.launch

// Activity to add a new medication
class AddMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityAddMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
        getAsNeededIntent()
        setupObservers()
    }

    override fun onStart() {
        Log.d("ErrorFlow testcat", "AddMedicationActivity - onStart")

        super.onStart()
        setupListeners()
    }

    // TEST START
    override fun onStop() {
        Log.d("ErrorFlow testcat", "AddMedicationActivity - onStop")
        super.onStop()
    }

    override fun onPause() {
        Log.d("ErrorFlow testcat", "AddMedicationActivity - onPause")
        super.onPause()
    }

    override fun onResume() {
        Log.d("ErrorFlow testcat", "AddMedicationActivity - onResume")
        super.onResume()
    }

    // TEST END

    // Set up bindings for this activity
    private fun setupActivity() {
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
    }

    // Observe changes in state flow that are used to update UI
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { collectAsScheduled() }
        }
    }

    // Collect asScheduled state to show/hide dosage, reminder, and schedule fragments
    private suspend fun collectAsScheduled() {
        medicationViewModel.asScheduled.collect { scheduled -> setFragmentVisibility(scheduled) }
    }

    // Update visibility of dosage, reminder, and schedule fragments based on asScheduled value
    private fun setFragmentVisibility(scheduled: Boolean) {
        with (binding) {
            fragmentAddMedDosage.visibility = if (scheduled) View.VISIBLE else View.GONE
            fragmentAddMedReminder.visibility = if (scheduled) View.VISIBLE else View.GONE
            fragmentAddMedSchedule.visibility = if (scheduled) View.VISIBLE else View.GONE
        }
    }

    // Set asScheduled in the view model based on the value of the ADD_AS_NEEDED flag
    private fun getAsNeededIntent() {
        if (intent.getBooleanExtra(ADD_AS_NEEDED, false)) {
            medicationViewModel.setAsScheduled(false)
        }
    }

    // Click listeners
    private fun setupListeners() {
        binding.buttonAddMed.setOnClickListener { handleAddMedication() }
        binding.buttonCancelAddMed.setOnClickListener { cancelActivity(this) }
    }

    // Add medication to the database
    private fun handleAddMedication() {
        Log.d("ErrorFlow testcat", "AddMedicationActivity - handleAddMedication started")

        lifecycleScope.launch {
            try {
                Log.d("ErrorFlow testcat", "AddMedicationActivity - handleAddMedication started")

                val medData = getMedicationData(MedicationAction.ADD)
                val asScheduled = medicationViewModel.asScheduled.value
                val dosageData = if (asScheduled) getDosageData(MedicationAction.ADD) else null
                val reminderData = if (asScheduled) medicationViewModel.getReminderData() else null
                val scheduleData = if (asScheduled) medicationViewModel.getScheduleData() else null

                // Add medication if med data is not null (dosage data can be null if it is
                // an as-needed medication)
                if (medData != null && (dosageData != null || !asScheduled)) {
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
                Log.d("ErrorFlow testcat", "AddMedicationActivity - Exception caught in handleAddMedication")

                Log.e(TAG, ERR_UNEXPECTED, e)
                baseViewModel.setErrorMessage(e.message ?: ERR_UNEXPECTED)
            }
        }
    }

    companion object {
        private const val TAG = "AddMedicationActivity"
        private const val MED_ADDED = "Medication added successfully!"
    }
}