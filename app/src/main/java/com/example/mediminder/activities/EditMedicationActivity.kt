package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.ActivityEditMedicationBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import kotlinx.coroutines.launch

class EditMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityEditMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private val medicationViewModel: BaseMedicationViewModel by viewModels { BaseMedicationViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()

        val medicationId = intent.getLongExtra(MED_ID, NULL_INT)
        checkMedicationId(medicationId)

        setupListeners(medicationId)

        lifecycleScope.launch { fetchMedication(medicationId) }
    }

    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityEditMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
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

    private fun handleUpdateMedication(medicationId: Long) {
        lifecycleScope.launch {
            val medData = getMedicationData(MedicationAction.EDIT)
            val dosageData = getDosageData(MedicationAction.EDIT)
            val reminderData = medicationViewModel.getReminderData()
            val scheduleData = medicationViewModel.getScheduleData()

            if (medData != null && dosageData != null) {
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
                Log.e("EditMedicationActivity testcat", "Medication or dosage data is null")
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