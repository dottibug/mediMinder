package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.MedicationsAdapter
import com.example.mediminder.databinding.ActivityMedicationsBinding
import com.example.mediminder.utils.Constants.DELETE
import com.example.mediminder.utils.Constants.EDIT
import com.example.mediminder.utils.Constants.ERR_FETCHING_MEDS
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.Constants.VIEW
import com.example.mediminder.viewmodels.MedicationsViewModel
import com.example.mediminder.viewmodels.MedicationsViewModel.Companion.TAG
import kotlinx.coroutines.launch

/**
 * Activity to display a list of all medications in the database. Users can view, edit, and delete
 * a medication from this list.
 */
class MedicationsActivity : BaseActivity() {
    private val medsViewModel: MedicationsViewModel by viewModels { MedicationsViewModel.Factory }
    private lateinit var binding: ActivityMedicationsBinding
    private lateinit var adapter: MedicationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        fetchMedications()
    }

    /**
     * Set up activity bindings and BaseActivity components
     * (top app bar, navigation drawer, error observer)
     */
    private fun setupActivity() {
        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        setupBaseBinding(binding)
    }

    /**
     * Set up state flow observers to update UI when medication list changes
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                medsViewModel.medications.collect { medications -> adapter.submitList(medications) }
            }
        }
    }

    /**
     * Sets up the recycler view to display fetched medications
     * - View: Opens ViewMedicationActivity
     * - Edit: Opens EditMedicationActivity
     * - Delete: Opens DeleteMedicationActivity
     */
    private fun setupRecyclerView() {
        adapter = MedicationsAdapter(
            onViewClick = { medicationId -> navigateToActivity(VIEW, medicationId) },
            onEditClick = { medicationId -> navigateToActivity(EDIT, medicationId) },
            onDeleteClick = { medicationId -> navigateToActivity(DELETE, medicationId) }
        )
        binding.medicationsList.layoutManager = LinearLayoutManager(this)
        binding.medicationsList.adapter = adapter
    }

    /**
     * Helper function to navigate to specified activity
     * @param action The action to perform (VIEW, EDIT, DELETE)
     * @param medicationId The ID of the medication to view, edit, or delete
     */
    private fun navigateToActivity(action: String, medicationId: Long) {
        val activityClass = when (action) {
            VIEW -> ViewMedicationActivity::class.java
            EDIT -> EditMedicationActivity::class.java
            DELETE -> DeleteMedicationActivity::class.java
            else -> return
        }
        val intent = Intent(this, activityClass)
        intent.putExtra(MED_ID, medicationId)
        startActivity(intent)
    }

    /**
     * Fetch medication from MedicationsViewModel to list in the recycler view
     */
    private fun fetchMedications() {
        lifecycleScope.launch {
            try {
                medsViewModel.fetchMedications()
            } catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_MEDS, e)
                appViewModel.setErrorMessage(ERR_FETCHING_MEDS)
            }
        }
    }
}