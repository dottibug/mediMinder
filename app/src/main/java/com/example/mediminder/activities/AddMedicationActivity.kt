package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.ActivityAddMedicationBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import kotlinx.coroutines.launch

class AddMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private val medicationViewModel: BaseMedicationViewModel by viewModels { BaseMedicationViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()
        setupListeners()
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

    private fun handleAddMedication() {
        lifecycleScope.launch {
            val medData = getMedicationData(MedicationAction.ADD)
            val dosageData = getDosageData(MedicationAction.ADD)
            val reminderData = medicationViewModel.getReminderData()
            val scheduleData = medicationViewModel.getScheduleData()

            if (medData != null && dosageData != null) {
                medicationViewModel.addMedication(medData, dosageData, reminderData, scheduleData)
                setResult(RESULT_OK)
                finish()
            } else {
                Log.e("AddMedicationActivity testcat", "Medication or dosage data is null")
            }
        }
    }
}