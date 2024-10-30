package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
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
import com.example.mediminder.viewmodels.AddMedicationViewModel
import com.example.mediminder.viewmodels.ReminderData
import kotlinx.coroutines.launch

class AddMedicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicationBinding
    private val windowUtils = WindowInsetsUtil
    private val addMedViewModel: AddMedicationViewModel by viewModels { AddMedicationViewModel.Factory }
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

                val reminderData = addMedViewModel.getReminderData()
                val scheduleData = addMedViewModel.getScheduleData()

                if (medicationData != null && dosageData != null) {
                    addMedViewModel.saveMedication(
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

        Log.i("testcat", "Medication status on a new medication: $status")

        return status
    }
}