package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityDeleteMedicationBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.DeleteMedicationViewModel
import kotlinx.coroutines.launch

class DeleteMedicationActivity : BaseActivity() {
    private val viewModel: DeleteMedicationViewModel by viewModels { DeleteMedicationViewModel.Factory }
    private lateinit var binding: ActivityDeleteMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private var medicationDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (medicationDeleted) { navigateToMain() }
                else { finish() }
            }
        })

        val medicationId = intent.getLongExtra(MED_ID, NULL_INT)
        checkMedicationId(medicationId)

        setupUI()
        setupListeners()
        setupObservers()
        lifecycleScope.launch { fetchMedication(medicationId) }
    }

    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityDeleteMedicationBinding.inflate(layoutInflater)
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

    private suspend fun fetchMedication(medicationId: Long) {
        loadingSpinnerUtil.whileLoading {
            try {
                viewModel.fetchMedication(medicationId)
            } catch (e: Exception) {
                Log.e("DeleteMedicationActivity testcat", "Error fetching medication", e)
                finish()
            }
        }
    }

    private fun setupUI() {
        val medicationName = viewModel.medicationName.value
        val deleteMessage = resources.getString(R.string.delete_medication_instructions, medicationName)
        binding.deleteMedicationMessage.text = deleteMessage
    }

    private fun setupListeners() {
        binding.buttonCancelDeleteMed.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.buttonConfirmDeleteMed.setOnClickListener {
            lifecycleScope.launch { deleteMedication() }
        }

        binding.buttonGoToMain.setOnClickListener { navigateToMain() }
        binding.buttonGoToMedications.setOnClickListener { navigateToMedications() }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.medicationName.collect { name ->
                val message = resources.getString(R.string.delete_medication_instructions, name)
                binding.deleteMedicationMessage.text = message
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToMedications() {
        startActivity(Intent(this, MedicationsActivity::class.java))
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
            }
        }
    }

    private fun showSuccessMessage() {
        binding.confirmDeleteMedContainer.visibility = View.GONE
        binding.successDeleteMedContainer.visibility = View.VISIBLE
    }
}