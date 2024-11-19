package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.ActivityEditMedicationBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import kotlinx.coroutines.launch

class EditMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityEditMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
//    private var isAsNeeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val medicationId = intent.getLongExtra(MED_ID, NULL_INT)
        checkMedicationId(medicationId)

        setupBindings()
//        setupInitialVisibility()
        setupListeners(medicationId)
        setupObservers()

//        Log.d("EditMedicationActivity testcat", "isAsNeeded: $isAsNeeded")

//        setupObservers()
        lifecycleScope.launch {
            fetchMedication(medicationId)
//            setupInitialVisibility()
        }
    }

    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityEditMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    private fun setupInitialVisibility() {
        val isAsScheduled = medicationViewModel.asScheduled.value

        if (isAsScheduled) {
            // Show dosage fragment, reminder fragment, and schedule fragment
            binding.fragmentEditMedDosage.visibility = View.VISIBLE
            binding.fragmentEditMedReminder.visibility = View.VISIBLE
            binding.fragmentEditMedSchedule.visibility = View.VISIBLE
        } else {
            // Hide dosage fragment, reminder fragment, and schedule fragment
            binding.fragmentEditMedDosage.visibility = View.GONE
            binding.fragmentEditMedReminder.visibility = View.GONE
            binding.fragmentEditMedSchedule.visibility = View.GONE
        }
    }

    private fun checkMedicationId(medId: Long) {
        if (medId == NULL_INT) {
            finish()
            return
        }
    }

    private fun setupListeners(medicationId: Long) {
        binding.buttonUpdateMed.setOnClickListener {
            handleUpdateMedication(medicationId)
        }

        binding.buttonCancelUpdateMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            medicationViewModel.asScheduled.collect { asScheduled ->
                updateFragmentVisibility(asScheduled)
            }
        }
    }

    private fun updateFragmentVisibility(asScheduled: Boolean) {
        with (binding) {
            fragmentEditMedDosage.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedReminder.visibility = if (asScheduled) View.VISIBLE else View.GONE
            fragmentEditMedSchedule.visibility = if (asScheduled) View.VISIBLE else View.GONE
        }
    }

    private fun handleUpdateMedication(medicationId: Long) {
        lifecycleScope.launch {
            val medData = getMedicationData(MedicationAction.EDIT)
            val asScheduled = medicationViewModel.asScheduled.value

            // Get dosage data if not as-needed medication
            val dosageData = if (asScheduled) getDosageData(MedicationAction.EDIT) else null

            // Get reminder data if not as-needed medication
            val reminderData = if (asScheduled) medicationViewModel.getReminderData() else null

            // Get schedule data if not as-needed medication
            val scheduleData = if (asScheduled) medicationViewModel.getScheduleData() else null

            // Update medication if med data is not null
            // Dosage data can be null only if medication is as-needed
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
            } else {
                Log.e("EditMedicationActivity testcat", "Required data is missing")
            }
        }
    }

    private suspend fun fetchMedication(medicationId: Long) {
        loadingSpinnerUtil.whileLoading {
            try {
                medicationViewModel.fetchMedication(medicationId)
            } catch (e: Exception) {
                Log.e("EditMedicationActivity", "Error fetching medication", e)
            }
        }
    }
}