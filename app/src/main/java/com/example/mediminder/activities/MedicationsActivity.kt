package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.MedicationsAdapter
import com.example.mediminder.databinding.ActivityMedicationsBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.MedicationsViewModel
import kotlinx.coroutines.launch

// This activity displays a list of medications. It uses the MedicationsViewModel to fetch the list.
class MedicationsActivity : BaseActivity() {
    private val viewModel: MedicationsViewModel by viewModels { MedicationsViewModel.Factory }
    private lateinit var binding: ActivityMedicationsBinding
    private lateinit var adapter: MedicationsAdapter
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()
    }

    // Set up recycler view and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupUI()
    }

    // Fetch/refresh medication data (observers will update UI when data is available)
    override fun onResume() {
        super.onResume()
        fetchMedications()
    }

    // Sets up bindings for the base class, then inflates this view into the base layout.
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    private fun setupUI() {
        setupRecyclerView()
        observeViewModel()
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

    // Updates the UI after medications are fetched
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.medications.collect { medications -> adapter.submitList(medications) }
        }
    }


    private fun fetchMedications() {
        lifecycleScope.launch {
            loadingSpinnerUtil.whileLoading {
                try { viewModel.fetchMedications() }
                catch (e: Exception) {
                    Log.e("MedicationsActivity testcat", "Error fetching medications", e)
                }
            }
        }
    }
}