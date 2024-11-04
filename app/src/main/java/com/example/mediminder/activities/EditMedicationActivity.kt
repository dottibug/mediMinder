package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityEditMedicationBinding
import com.example.mediminder.fragments.EditDosageFragment
import com.example.mediminder.fragments.EditMedicationInfoFragment
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import kotlinx.coroutines.launch

class EditMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityEditMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private val medicationViewModel: BaseMedicationViewModel by viewModels { BaseMedicationViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupBaseLayout()
        binding = ActivityEditMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)

        // Get medicationId from intent extras
        val medicationId = intent.getLongExtra("medicationId", -1)
        if (medicationId == -1L) {
            finish()
            return
        }

        Log.d("EditMedicationActivity testcat", "Medication ID: $medicationId")

        setupUI(medicationId)

        lifecycleScope.launch {
            fetchMedication(medicationId)
        }
    }

    private fun setupUI(medicationId: Long) {
        binding.buttonUpdateMed.setOnClickListener {
            lifecycleScope.launch {
                val medicationFragment = supportFragmentManager.findFragmentById(
                    R.id.fragmentEditMedInfo
                ) as EditMedicationInfoFragment?
                val medicationData = medicationFragment?.getMedicationData()

                val dosageFragment = supportFragmentManager.findFragmentById(
                    R.id.fragmentEditMedDosage
                ) as EditDosageFragment?
                val dosageData = dosageFragment?.getDosageData()

                val reminderData = medicationViewModel.getReminderData()
                val scheduleData = medicationViewModel.getScheduleData()

                if (medicationData != null && dosageData != null) {
                    medicationViewModel.updateMedication(
                        medicationId,
                        medicationData,
                        dosageData,
                        reminderData,
                        scheduleData
                    )

                    setResult(RESULT_OK)
                    finish()
                } else {
                    // Show error message
                }
            }
        }

        binding.buttonCancelUpdateMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private suspend fun fetchMedication(medicationId: Long) {
        loadingSpinnerUtil.whileLoading {
            try {
                medicationViewModel.fetchMedication(medicationId)
            } catch (e: Exception) {
                Log.e("EditMedicationActivity", "Error fetching medication", e)
                // Show error message
            }
        }
    }
}