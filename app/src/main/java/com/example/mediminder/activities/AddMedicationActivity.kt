package com.example.mediminder.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.databinding.ActivityAddMedicationBinding
import com.example.mediminder.fragments.AddMedicationDosageFragment
import com.example.mediminder.fragments.AddMedicationInfoFragment
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.example.mediminder.viewmodels.ReminderData
import kotlinx.coroutines.launch

class AddMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private val windowUtils = WindowInsetsUtil
    private val medicationViewModel: BaseMedicationViewModel by viewModels { BaseMedicationViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowUtils.setupWindowInsets(binding.root)

        binding.buttonAddMed.setOnClickListener {
            lifecycleScope.launch {
                val medicationFragment = supportFragmentManager.findFragmentById(
                    R.id.fragmentAddMedInfo
                ) as AddMedicationInfoFragment?
                val medicationData = medicationFragment?.getMedicationData()

                val dosageFragment = supportFragmentManager.findFragmentById(
                    R.id.fragmentAddMedDosage
                ) as AddMedicationDosageFragment?
                val dosageData = dosageFragment?.getDosageData()

                val reminderData = medicationViewModel.getReminderData()
                val scheduleData = medicationViewModel.getScheduleData()

                if (medicationData != null && dosageData != null) {
                    medicationViewModel.addMedication(
                        medicationData, dosageData, reminderData, scheduleData
                    )

                    setResult(RESULT_OK)
                    // TODO: show a toast message?
                    finish()
                } else {
                    // error message
                }
            }
        }

        binding.buttonCancelAddMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun getMedicationStatus(reminderData: ReminderData): MedicationStatus {
        // Medication status for a new medication
        // if no reminder is set, then UNSCHEDULED
        // if reminder is set, then SCHEDULED
        val status = if (reminderData.reminderEnabled) {
                MedicationStatus.PENDING
        } else {
            MedicationStatus.UNSCHEDULED
        }
        return status
    }
}