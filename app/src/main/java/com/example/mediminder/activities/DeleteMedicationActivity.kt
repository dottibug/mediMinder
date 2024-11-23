package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityDeleteMedicationBinding
import com.example.mediminder.utils.AppUtils.getMedicationId
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.SHOW
import com.example.mediminder.viewmodels.DeleteMedicationViewModel
import kotlinx.coroutines.launch

// Activity to delete a medication from the database
class DeleteMedicationActivity : BaseActivity() {
    private val viewModel: DeleteMedicationViewModel by viewModels { DeleteMedicationViewModel.Factory }
    private lateinit var binding: ActivityDeleteMedicationBinding
    private var medicationId: Long = -1L
    private var medicationDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = getMedicationId(this) ?: return
        setupActivity()
        setupObservers()

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
    }

    // Fetch medication data when the activity is resumed
    override fun onResume() {
        super.onResume()
        fetchMedicationData()
    }

    // Set up bindings for this activity
    private fun setupActivity() {
        binding = ActivityDeleteMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
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

    // Set up observers to update the UI when state flow changes
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMedicationName() }
                launch { collectCurrentMedication() }
                launch { collectIsDeleting() }
            }
        }
    }

    // Collect medication name from the view model
    private suspend fun collectMedicationName() {
        viewModel.medicationName.collect { name ->
            val message = resources.getString(R.string.msg_delete_medication, name)
            binding.deleteMedicationMessage.text = message
        }
    }

    // Collect current medication state
    private suspend fun collectCurrentMedication() {
        viewModel.currentMedication.collect { med ->
            if (med == null) { toggleConfirmContentVisibility(HIDE) }
            else { toggleConfirmContentVisibility(SHOW) }
        }
    }

    // Collect isDeleting state
    private suspend fun collectIsDeleting() {
        viewModel.isDeleting.collect { isDeleting ->
            if (isDeleting) toggleSuccessContentVisibility(HIDE)
            else toggleSuccessContentVisibility(SHOW)
        }
    }

    // Toggle the visibility of the confirmation layout and edit/delete buttons based on the action
    private fun toggleConfirmContentVisibility(action: String) {
        with (binding) {
            deleteMedicationMessage.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonConfirmDeleteMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonCancelDeleteMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        }
    }

    // Toggle the visibility of the success layout based on the action
    private fun toggleSuccessContentVisibility(action: String) {
        with (binding) {
            successDeleteMedMessage.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonGoToMain.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonGoToMedications.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        }
    }

    // Fetch medication data (no need to catch errors here, as they are handled in the view model
    // and the error observer for this activity will handle showing the error message)
    private fun fetchMedicationData() {
        lifecycleScope.launch {
            viewModel.fetchMedication(medicationId)
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
            viewModel.deleteMedication()
            medicationDeleted = true
            showSuccessMessage()
        }
    }

    private fun showSuccessMessage() {
        binding.confirmDeleteMedContainer.visibility = View.GONE
        binding.successDeleteMedContainer.visibility = View.VISIBLE
    }
}