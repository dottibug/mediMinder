package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.ActivityAddMedicationBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import kotlinx.coroutines.launch

// Activity to add a new medication
class AddMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()

        // Hide dosage, reminder, and schedule fragments if the activity started with ADD_AS_NEEDED flag
        if (intent.getBooleanExtra("ADD_AS_NEEDED", false)) {
            medicationViewModel.setAsScheduled(false)
        }
    }

    // Set up listeners and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupListeners()
        setupObservers()
    }

    // Set up bindings for the base class, then inflate this view into the base layout
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
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
            medicationViewModel.asScheduled.collect { asScheduled ->
                updateFragmentVisibility(asScheduled)
            }
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
            loadingSpinnerUtil.whileLoading {
                try {
                    val medData = getMedicationData(MedicationAction.ADD)
                    val isAsScheduled = medicationViewModel.asScheduled.value
                    val dosageData = if (isAsScheduled) getDosageData(MedicationAction.ADD) else null
                    val reminderData = if (isAsScheduled) medicationViewModel.getReminderData() else null
                    val scheduleData = if (isAsScheduled) medicationViewModel.getScheduleData() else null

                    // Add medication if med data is not null (dosage data can be null if it is
                    // an as-needed medication)
                    if (medData != null && (dosageData != null || !isAsScheduled)) {
                        medicationViewModel.addMedication(medData, dosageData, reminderData, scheduleData)
                        setResult(RESULT_OK)
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("AddMedicationActivity testcat", "Error adding medication", e)
                }
            }
        }
    }
}