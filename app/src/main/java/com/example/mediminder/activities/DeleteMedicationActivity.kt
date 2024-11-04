package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityDeleteMedicationBinding
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.DeleteMedicationViewModel
import kotlinx.coroutines.launch

class DeleteMedicationActivity : BaseActivity() {
    private val viewModel: DeleteMedicationViewModel by viewModels { DeleteMedicationViewModel.Factory }
    private lateinit var binding: ActivityDeleteMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private var medicationDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupBaseLayout()
        binding = ActivityDeleteMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (medicationDeleted) { navigateToMain() }
                else { finish() }
            }
        })

        // Get medicationId from intent extras
        val medicationId = intent.getLongExtra("medicationId", -1)
        if (medicationId == -1L) {
            finish()
            return
        }

        lifecycleScope.launch {
            fetchMedication(medicationId)
        }

        setupUI()
        setupObservers()
    }

    private suspend fun fetchMedication(medicationId: Long) {
        loadingSpinnerUtil.whileLoading {
            try {
                viewModel.fetchMedication(medicationId)
            } catch (e: Exception) {
                Log.e("DeleteMedicationActivity", "Error fetching medication", e)
                finish()
            }
        }
    }

    private fun setupUI() {
        val medicationName = viewModel.medicationName.value
        binding.deleteMedicationMessage.text = resources.getString(R.string.delete_medication_instructions, medicationName)

        binding.buttonCancelDeleteMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.buttonConfirmDeleteMed.setOnClickListener {
            lifecycleScope.launch {
                deleteMedication()
            }
        }

        binding.buttonGoToMain.setOnClickListener {
            navigateToMain()
        }

        binding.buttonGoToMedications.setOnClickListener {
            navigateToMedications()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.medicationName.collect { name ->
                binding.deleteMedicationMessage.text =
                    resources.getString(R.string.delete_medication_instructions, name)
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMedications() {
        val intent = Intent(this, MedicationsActivity::class.java)
        startActivity(intent)
        finish()
    }


    private suspend fun deleteMedication() {
        loadingSpinnerUtil.whileLoading {
            try {
                viewModel.deleteMedication()
                medicationDeleted = true
                showSuccessMessage()
            } catch (e: Exception) {
                Log.e("DeleteMedicationActivity", "Error deleting medication", e)
                // Show error message
            }
        }
    }

    private fun showSuccessMessage() {
        binding.confirmDeleteMedContainer.visibility = View.GONE
        binding.successDeleteMedContainer.visibility = View.VISIBLE
    }
}