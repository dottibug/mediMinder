package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.MedicationsAdapter
import com.example.mediminder.databinding.ActivityMedicationsBinding
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.viewmodels.MedicationsViewModel
import kotlinx.coroutines.launch

// This activity displays a list of medications. It uses the MedicationsViewModel to fetch the list.
class MedicationsActivity : BaseActivity() {
    private val viewModel: MedicationsViewModel by viewModels { MedicationsViewModel.Factory }
    private lateinit var binding: ActivityMedicationsBinding
    private lateinit var adapter: MedicationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivity()
        setupObservers()
    }

    // Set up recycler view and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupRecyclerView()
    }

    // Fetch/refresh medication data (observers will update UI when data is available)
    override fun onResume() {
        super.onResume()
        fetchMedications()
    }

    // Set up bindings for this activity
    private fun setupActivity() {
        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        setupBaseBinding(binding, binding.loadingSpinner)
    }

    // Sets up observers for the view model
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMedications() }
            }
        }
    }

    // Collect medications from the view model
    private suspend fun collectMedications() {
        viewModel.medications.collect { medications ->
            adapter.submitList(medications)
        }
    }

    // Sets up the recycler view to display fetched medications
    private fun setupRecyclerView() {
        adapter = MedicationsAdapter(
            onViewClick = { medicationId ->
                val intent = Intent(this, ViewMedicationActivity::class.java)
                intent.putExtra(MED_ID, medicationId)
                startActivity(intent)
            },
            onEditClick = { medicationId ->
                val intent = Intent(this, EditMedicationActivity::class.java)
                intent.putExtra(MED_ID, medicationId)
                startActivity(intent)
            },
            onDeleteClick = { medicationId ->
                val intent = Intent(this, DeleteMedicationActivity::class.java)
                intent.putExtra(MED_ID, medicationId)
                startActivity(intent)
            }
        )

        binding.medicationsList.layoutManager = LinearLayoutManager(this)
        binding.medicationsList.adapter = adapter
    }

    // Fetch medications from the ViewModel (no need to catch errors here, as they are handled
    // in the ViewModel and the error observer for this activity will handle showing the error message)
    private fun fetchMedications() {
        lifecycleScope.launch {
            viewModel.fetchMedications()
        }
    }
}