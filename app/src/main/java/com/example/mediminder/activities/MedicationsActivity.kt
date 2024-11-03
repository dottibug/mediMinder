package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
                val intent = Intent(this, ViewMedicationActivity::class.java)
                intent.putExtra("medicationId", medicationId)
                startActivity(intent)
            },
            onEditClick = { medicationId ->
                val intent = Intent(this, EditMedicationActivity::class.java)
                intent.putExtra("medicationId", medicationId)
                startActivity(intent)
            },
            onDeleteClick = { medicationId ->
                val intent = Intent(this, DeleteMedicationActivity::class.java)
                intent.putExtra("medicationId", medicationId)
                startActivity(intent)
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
                Log.e("MedicationsActivity testcat", "Error fetching medications", e)
                // Handle error
                e.printStackTrace()
            }
        }
    }

}