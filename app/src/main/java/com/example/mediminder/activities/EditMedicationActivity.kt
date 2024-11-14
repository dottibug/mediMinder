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

    // BUG The initial visibility isn't working because asNeeded is false on all meds it seems.
    //  How do we figure out if it's asNeeded or not? ... the base view model should be setting it
    //  for us after we fetch the medication. Add a bunch of loggin.
    private fun setupInitialVisibility() {
        Log.d("EditMedicationActivity testcat", "setupInitialVisibility called")

        val isAsNeeded = medicationViewModel.asNeeded.value
        Log.d("EditMedicationActivity testcat", "isAsNeeded: $isAsNeeded")

        if (medicationViewModel.asNeeded.value) {
            // Hide dosage fragment, reminder fragment, and schedule fragment
            binding.fragmentEditMedDosage.visibility = View.GONE
            binding.fragmentEditMedReminder.visibility = View.GONE
            binding.fragmentEditMedSchedule.visibility = View.GONE
        } else {
            // Show dosage fragment, reminder fragment, and schedule fragment
            binding.fragmentEditMedDosage.visibility = View.VISIBLE
            binding.fragmentEditMedReminder.visibility = View.VISIBLE
            binding.fragmentEditMedSchedule.visibility = View.VISIBLE
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
            medicationViewModel.asNeeded.collect { isAsNeeded ->
                Log.d("EditMedicationActivity testcat", "asNeeded: $isAsNeeded")
                updateFragmentVisibility(isAsNeeded)
            }
        }
    }

    private fun updateFragmentVisibility(isAsNeeded: Boolean) {
        Log.d("EditMedicationActivity", "Updating fragments visibility. isAsNeeded: $isAsNeeded")

        binding.fragmentEditMedDosage.visibility = if (isAsNeeded) View.GONE else View.VISIBLE
        binding.fragmentEditMedReminder.visibility = if (isAsNeeded) View.GONE else View.VISIBLE
        binding.fragmentEditMedSchedule.visibility = if (isAsNeeded) View.GONE else View.VISIBLE
    }

    private fun handleUpdateMedication(medicationId: Long) {
        lifecycleScope.launch {
            val medData = getMedicationData(MedicationAction.EDIT)
            val isAsNeeded = medicationViewModel.asNeeded.value

            // Get dosage data if not as-needed medication
            val dosageData = if (!isAsNeeded) getDosageData(MedicationAction.EDIT) else null

            // Get reminder data if not as-needed medication
            val reminderData = if (!isAsNeeded) medicationViewModel.getReminderData() else null

            // Get schedule data if not as-needed medication
            val scheduleData = if (!isAsNeeded) medicationViewModel.getScheduleData() else null

            if (medData != null && (dosageData != null || isAsNeeded)) {
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