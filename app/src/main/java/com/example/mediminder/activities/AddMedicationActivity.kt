package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.ActivityAddMedicationBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import kotlinx.coroutines.launch

class AddMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
//    override var isAsNeeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If activity started with ADD_AS_NEEDED flag, hide dosage fragment
        if (intent.getBooleanExtra("ADD_AS_NEEDED", false)) {
            medicationViewModel.setAsNeeded(true)
//            isAsNeeded = true
        }

        setupBindings()
        setupInitialVisibility()
        setupListeners()
        setupObservers()
    }

    private fun setupInitialVisibility() {
        if (medicationViewModel.asNeeded.value) {
            // Hide dosage fragment, reminder fragment, and schedule fragment
            binding.fragmentAddMedDosage.visibility = View.GONE
            binding.fragmentAddMedReminder.visibility = View.GONE
            binding.fragmentAddMedSchedule.visibility = View.GONE
        } else {
            // Show dosage fragment, reminder fragment, and schedule fragment
            binding.fragmentAddMedDosage.visibility = View.VISIBLE
            binding.fragmentAddMedReminder.visibility = View.VISIBLE
            binding.fragmentAddMedSchedule.visibility = View.VISIBLE
        }
    }

    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    private fun setupListeners() {
        binding.buttonAddMed.setOnClickListener { handleAddMedication() }

        binding.buttonCancelAddMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            medicationViewModel.asNeeded.collect { asNeeded ->
//                isAsNeeded = asNeeded
                setupInitialVisibility()
            }
        }
    }

    private fun handleAddMedication() {
        lifecycleScope.launch {
            loadingSpinnerUtil.whileLoading {
                try {
                    Log.d("AddMedicationActivity testcat", "handleAddMedication called")
                    val medData = getMedicationData(MedicationAction.ADD)
                    Log.d("AddMedicationActivity testcat", "Medication data: $medData")
                    val isAsNeeded = medicationViewModel.asNeeded.value
                    Log.d("AddMedicationActivity testcat", "As-needed flag: $isAsNeeded")

                    // Get dosage data if not as-needed medication
                    val dosageData = if (!isAsNeeded) getDosageData(MedicationAction.ADD) else null
                    Log.d("AddMedicationActivity testcat", "Dosage data: $dosageData")

                    // Get reminder data if not as-needed medication
                    val reminderData = if (!isAsNeeded) medicationViewModel.getReminderData() else null
                    Log.d("AddMedicationActivity testcat", "Reminder data: $reminderData")

                    // Get schedule data if not as-needed medication
                    val scheduleData = if (!isAsNeeded) medicationViewModel.getScheduleData() else null
                    Log.d("AddMedicationActivity testcat", "Schedule data: $scheduleData")

                    if (medData != null && (dosageData != null || isAsNeeded)) {
                        medicationViewModel.addMedication(medData, dosageData, reminderData, scheduleData)
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Log.e("AddMedicationActivity testcat", "Invalid medication data")
                    }
                } catch (e: Exception) {
                    Log.e("AddMedicationActivity testcat", "Error adding medication", e)
                }
            }
        }
    }
}