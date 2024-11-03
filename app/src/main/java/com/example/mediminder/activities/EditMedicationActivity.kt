package com.example.mediminder.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.example.mediminder.databinding.ActivityEditMedicationBinding
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.utils.WindowInsetsUtil

class EditMedicationActivity : BaseActivity() {
    private lateinit var binding: ActivityEditMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

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

    }
}