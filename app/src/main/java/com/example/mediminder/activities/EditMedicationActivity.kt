package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.ActivityEditMedicationBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.LoadingSpinnerUtil
import kotlinx.coroutines.launch

// This activity allows the user to edit an existing medication. It uses the MedicationViewModel to
// fetch and update medication data.
class EditMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityEditMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private var medicationId: Long = -1L

    // Initialize the ViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()
        medicationId = intent.getLongExtra(MED_ID, -1L)
        validateMedicationId(medicationId)
    }

    // Set up listeners and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupListeners()
        setupObservers()
    }

    // Fetch medication data when the activity is resumed
    override fun onResume() {
        super.onResume()
        fetchMedicationData()
    }

    // Set up bindings for the base class, then inflate this view into the base layout.
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityEditMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    // Check if the medication ID is valid. If not, finish the activity.
    private fun validateMedicationId(medId: Long) {
        if (medId == -1L) {
            finish()
            return
        }
    }

    // Click listeners for buttons
    private fun setupListeners() {
        binding.buttonUpdateMed.setOnClickListener {
            handleUpdateMedication(medicationId)
        }

        binding.buttonCancelUpdateMed.setOnClickListener {
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
            fragmentEditMedDosage.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedReminder.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedSchedule.visibility = if (asScheduled) View.VISIBLE else View.GONE
        }
    }

    // Update medication data and finish the activity
    private fun handleUpdateMedication(medicationId: Long) {
        lifecycleScope.launch {
            loadingSpinnerUtil.whileLoading {
                try {
                    val medData = getMedicationData(MedicationAction.EDIT)
                    val asScheduled = medicationViewModel.asScheduled.value
                    val dosageData = if (asScheduled) getDosageData(MedicationAction.EDIT) else null
                    val reminderData = if (asScheduled) medicationViewModel.getReminderData() else null
                    val scheduleData = if (asScheduled) medicationViewModel.getScheduleData() else null

                    // Update medication if med data is not null (dosage data can be null if it is
                    // an as-needed medication)
                    if (medData != null && (dosageData != null || !asScheduled)) {
                        medicationViewModel.updateMedication(
                            medicationId,
                            medData,
                            dosageData,
                            reminderData,
                            scheduleData
                        )
                        setResult(RESULT_OK)
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("EditMedicationActivity testcat", "Error updating medication", e)
                }
            }
        }
    }

    // Fetch medication data from the ViewModel
    private fun fetchMedicationData() {
        lifecycleScope.launch {
            loadingSpinnerUtil.whileLoading {
                try {
                    medicationViewModel.fetchMedication(medicationId)
                } catch (e: Exception) {
                    Log.e("EditMedicationActivity", "Error fetching medication", e)
                }
            }
        }
    }
}