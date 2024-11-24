package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityDeleteMedicationBinding
import com.example.mediminder.utils.AppUtils.cancelActivity
import com.example.mediminder.utils.AppUtils.getMedicationId
import com.example.mediminder.utils.Constants.ERR_DELETING_MED
import com.example.mediminder.utils.Constants.ERR_DELETING_MED_USER
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.SHOW
import com.example.mediminder.viewmodels.DeleteMedicationViewModel
import kotlinx.coroutines.launch

/**
 * Activity to delete a medication from the database. Shows a confirmation dialog before deleting,
 * deletes medication, and gives navigation option after deletion (MainActivity or MedicationsActivity).
 */
class DeleteMedicationActivity : BaseActivity() {
    private val deleteViewModel: DeleteMedicationViewModel by viewModels { DeleteMedicationViewModel.Factory }
    private lateinit var binding: ActivityDeleteMedicationBinding
    private var medicationId: Long = -1L
    private var medicationDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = getMedicationId(this) ?: return
        setupActivity()
        setupObservers()
        setupBackNavigation()
    }

    override fun onStart() {
        super.onStart()
        setupUI()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchMedicationData()
    }

    /**
     * Setup activity binding and BaseActivity components
     * (top app bar, navigation drawer, error observer)
     */
    private fun setupActivity() {
        binding = ActivityDeleteMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
    }

    private fun setupUI() {
        val medicationName = appViewModel.medication.name
        binding.deleteMedicationMessage.text = resources.getString(R.string.msg_delete_medication, medicationName)
    }

    private fun setupListeners() {
        with (binding) {
            buttonConfirmDeleteMed.setOnClickListener { deleteMedication() }
            buttonCancelDeleteMed.setOnClickListener { cancelActivity(this@DeleteMedicationActivity) }
            buttonGoToMain.setOnClickListener { navigateToMain() }
            buttonGoToMedications.setOnClickListener { navigateToMedications() }
        }
    }

    /**
     * Set up state flow observers to update UI
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMedicationName() }
                launch { collectCurrentMedication() }
                launch { collectIsDeleting() }
            }
        }
    }

    /**
     * Update deletion confirmation message when medication name is collected
     */
    private suspend fun collectMedicationName() {
        appViewModel.medication.name.collect { name ->
            val message = resources.getString(R.string.msg_delete_medication, name)
            binding.deleteMedicationMessage.text = message
        }
    }

    /**
     * Show/hide the confirmation UI based on the current medication state
     */
    private suspend fun collectCurrentMedication() {
        appViewModel.medication.current.collect { med ->
            if (med == null) { toggleConfirmContentVisibility(HIDE) }
            else { toggleConfirmContentVisibility(SHOW) }
        }
    }

    /**
     * Show/hide the success UI based on the isDeleting state
     */
    private suspend fun collectIsDeleting() {
        deleteViewModel.isDeleting.collect { isDeleting ->
            if (isDeleting) toggleSuccessContentVisibility(HIDE)
            else toggleSuccessContentVisibility(SHOW)
        }
    }

    /**
     * Helper functions to toggle the visibility of the confirmation and success layouts
     * @param action The action to perform (SHOW or HIDE)
     */
    private fun toggleConfirmContentVisibility(action: String) {
        with (binding) {
            deleteMedicationMessage.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonConfirmDeleteMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonCancelDeleteMed.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        }
    }

    private fun toggleSuccessContentVisibility(action: String) {
        with (binding) {
            successDeleteMedMessage.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonGoToMain.visibility = if (action == HIDE) View.GONE else View.VISIBLE
            buttonGoToMedications.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        }
    }

    /**
     * Sets up navigation behaviour based on medicationDelete state. If medication was deleted,
     * navigate to MainActivity on back press. Otherwise, finish the activity.
     */
    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (medicationDeleted) { navigateToMain() } else { finish() }
            }
        })
    }

     /**
     * Fetch medication data from the database (errors are handled in AppViewModel)
     */
    private fun fetchMedicationData() {
        lifecycleScope.launch { appViewModel.fetchMedicationDetails(medicationId) }
    }

    /**
     * Navigation helper functions to navigate to MainActivity or MedicationsActivity
     * Flags are set to prevent multiple instances of the same activity in the back stack
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToMedications() {
        val intent = Intent(this, MedicationsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    /**
     * Delete medication from the database. Handles both scheduled and as-needed medications.
     * - Gets medication ID
     * - Deletes medication from the database
     * - Updates UI on success
     * - Shows errors via the error observer in BaseActivity
     */
    private fun deleteMedication() {
        lifecycleScope.launch {
            try {
                val medId = appViewModel.medication.getId()
                deleteViewModel.deleteMedication(medId)
                medicationDeleted = true
                showSuccessMessage()
            } catch (e: Exception) {
                Log.e(TAG, ERR_DELETING_MED, e)
                appViewModel.setErrorMessage(ERR_DELETING_MED_USER)
            }
        }
    }

    private fun showSuccessMessage() {
        binding.confirmDeleteMedContainer.visibility = View.GONE
        binding.successDeleteMedContainer.visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "DeleteMedicationActivity"
    }
}