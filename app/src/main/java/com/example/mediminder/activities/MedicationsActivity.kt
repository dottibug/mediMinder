package com.example.mediminder.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.MedicationsAdapter
import com.example.mediminder.databinding.ActivityMedicationsBinding
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.MedicationsViewModel
import kotlinx.coroutines.launch

class MedicationsActivity : BaseActivity() {
    private val viewModel: MedicationsViewModel by viewModels { MedicationsViewModel.Factory }
    private lateinit var binding: ActivityMedicationsBinding
    private lateinit var adapter: MedicationsAdapter
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupBaseLayout()
        binding = ActivityMedicationsBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)

        setupUI()

        lifecycleScope.launch {
            fetchMedications()
        }
    }

    private fun setupUI() {
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = MedicationsAdapter(
            onViewClick = { medicationId ->
                // TODO
                // Handle view medication click: View medication by id
            },
            onEditClick = { medicationId ->
                // TODO
                // Handle edit medication click: Edit medication by id
            },
            onDeleteClick = { medicationId ->
                // TODO
                // Handle delete medication click: Delete medication by id
            }
        )

        binding.medicationsList.layoutManager = LinearLayoutManager(this)
        binding.medicationsList.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.medications.collect { medications ->
                adapter.submitList(medications)
            }
        }
    }


    private suspend fun fetchMedications() {
        loadingSpinnerUtil.whileLoading {
            try {
                viewModel.fetchMedications()
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

}