package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.databinding.ActivityViewMedicationBinding
import com.example.mediminder.utils.AppUtils.getMedicationId
import com.example.mediminder.utils.Constants.DELETE
import com.example.mediminder.utils.Constants.EDIT
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.Constants.SHOW
import com.example.mediminder.utils.ViewMedicationSetupUtils
import kotlinx.coroutines.launch

/**
 * Activity to view a summary of a specific medication. The user can also edit or delete
 * the medication.
 */
class ViewMedicationActivity(): BaseActivity() {
    private lateinit var binding: ActivityViewMedicationBinding
    private lateinit var setupUI: ViewMedicationSetupUtils
    private var medicationId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = getMedicationId(this) ?: return
        setupActivity()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        fetchMedicationData(medicationId)
    }

    /**
     * Set up activity bindings and BaseActivity components
     * (top app bar, navigation drawer, error observer)
     */
    private fun setupActivity() {
        binding = ActivityViewMedicationBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
        setupUI = ViewMedicationSetupUtils(binding, resources)
    }

    private fun setupListeners() {
        binding.buttonEditMed.setOnClickListener { navigateToActivity(EDIT, medicationId) }
        binding.buttonDeleteMed.setOnClickListener { navigateToActivity(DELETE, medicationId) }
    }

    // Helper function to navigate to specified activity
    private fun navigateToActivity(action: String, medicationId: Long) {
        val activityClass = when (action) {
            EDIT -> EditMedicationActivity::class.java
            DELETE -> DeleteMedicationActivity::class.java
            else -> return
        }
        val intent = Intent(this, activityClass)
        intent.putExtra(MED_ID, medicationId)
        startActivity(intent)
    }

    /**
     * Set up state flow observers to update UI when medication details change
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { collectMedication() }
        }
    }

    /**
     * Collect medication details from the view model and update the UI accordingly.
     */
    private suspend fun collectMedication() {
        appViewModel.medication.current.collect { medicationDetails ->
            if (medicationDetails == null) { toggleContentVisibility(HIDE) }
            else {
                toggleContentVisibility(SHOW)
                setupUI.setupMedicationDetails(medicationDetails)
            }
        }
    }

    /**
     * Toggle the visibility of the medication summary and edit/delete buttons
     * @param action The action to perform (SHOW, HIDE)
     */
    private fun toggleContentVisibility(action: String) {
        binding.layoutMedSummary.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        binding.layoutEditDeleteButtons.visibility = if (action == HIDE) View.GONE else View.VISIBLE
    }

    /**
     * Fetch medication details from AppViewModel to display in the UI
     */
    private fun fetchMedicationData(medicationId: Long) {
        lifecycleScope.launch { appViewModel.fetchMedicationDetails(medicationId) }
    }
}