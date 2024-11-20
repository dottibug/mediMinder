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
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.DeleteMedicationViewModel
import kotlinx.coroutines.launch

// Activity to delete a medication from the database
class DeleteMedicationActivity : BaseActivity() {
    private val viewModel: DeleteMedicationViewModel by viewModels { DeleteMedicationViewModel.Factory }
    private lateinit var binding: ActivityDeleteMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private var medicationId: Long = -1L
    private var medicationDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = intent.getLongExtra(MED_ID, -1L)
        if (medicationId == -1L) { finish() }
        setupBindings()

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (medicationDeleted) { navigateToMain() }
                else { finish() }
            }
        })
    }

    // Set up listeners and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupUI()
        setupListeners()
        setupObservers()
    }

    // Fetch medication data when the activity is resumed
    override fun onResume() {
        super.onResume()
        fetchMedicationData()
    }

    // Set up bindings for the base class, then inflate this view into the base layout
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityDeleteMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    private fun setupUI() {
        val medicationName = viewModel.medicationName.value
        binding.deleteMedicationMessage.text = resources.getString(R.string.msg_delete_medication, medicationName)
    }

    // Click listeners
    private fun setupListeners() {
        with (binding) {
            buttonConfirmDeleteMed.setOnClickListener { deleteMedication() }
            buttonCancelDeleteMed.setOnClickListener { cancelActivity() }
            buttonGoToMain.setOnClickListener { navigateToMain() }
            buttonGoToMedications.setOnClickListener { navigateToMedications() }
        }
    }

    // Cancel the activity
    private fun cancelActivity() {
        setResult(RESULT_CANCELED)
        finish()
    }

    // Update the UI when medication name is fetched
    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.medicationName.collect { name ->
                val message = resources.getString(R.string.msg_delete_medication, name)
                binding.deleteMedicationMessage.text = message
            }
        }
    }

    // Fetch medication details from the database
    private fun fetchMedicationData() {
        lifecycleScope.launch {
            loadingSpinnerUtil.whileLoading {
                try { viewModel.fetchMedication(medicationId) }
                catch (e: Exception) {
                    Log.e("DeleteMedicationActivity testcat", "Error fetching medication", e)
                }
            }
        }
    }

    // Navigate to MainActivity
    // This will not launch a new instance; all other activities will be closed and the old instance
    // will be resumed with a new intent
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    // Navigate to MedicationsActivity
    // This will not launch a new instance; all other activities will be closed and the old instance
    // of MedicationsActivity will be resumed with a new intent
    private fun navigateToMedications() {
        val intent = Intent(this, MedicationsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    // Delete medication from the database (cascades to all related entities)
    private fun deleteMedication() {
        lifecycleScope.launch {
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
    }

    private fun showSuccessMessage() {
        binding.confirmDeleteMedContainer.visibility = View.GONE
        binding.successDeleteMedContainer.visibility = View.VISIBLE
    }
}