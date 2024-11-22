package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.databinding.ActivityViewMedicationBinding
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.Constants.HIDE
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.Constants.SHOW
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.utils.ViewMedicationSetupUtils
import com.example.mediminder.viewmodels.ViewMedicationViewModel
import kotlinx.coroutines.launch

// This activity displays the details of a medication, including icon, name, dosage, doctor, notes,
// reminders, schedule, start date, and end date. It uses the ViewMedicationViewModel to fetch the
// medication details.
class ViewMedicationActivity(): BaseActivity() {
    private val viewModel: ViewMedicationViewModel by viewModels { ViewMedicationViewModel.Factory }
    private lateinit var binding: ActivityViewMedicationBinding
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil
    private lateinit var setupUI: ViewMedicationSetupUtils
    private var medicationId: Long = -1L

    // Initialize variables and setup bindings
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicationId = intent.getLongExtra(MED_ID, -1L)
        if (medicationId == -1L) { finish() }
        setupBindings()
        setupObservers()
    }

    // Set up listeners and observers before data is fetched
    override fun onStart() {
        super.onStart()
        setupListeners()
    }

    // Fetch/refresh medication data (observers will update UI when data is available)
    override fun onResume() {
        super.onResume()
        fetchMedicationData(medicationId)
    }

    // Set up bindings for the base class, then inflate this view into the base layout
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityViewMedicationBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
        setupUI = ViewMedicationSetupUtils(binding, resources)
    }

    private fun setupListeners() {
        binding.buttonEditMed.setOnClickListener {
            val intent = Intent(this, EditMedicationActivity::class.java)
            intent.putExtra(MED_ID, medicationId)
            startActivity(intent)
        }

        binding.buttonDeleteMed.setOnClickListener {
            val intent = Intent(this, DeleteMedicationActivity::class.java)
            intent.putExtra(MED_ID, medicationId)
            startActivity(intent)
        }
    }

    // Set up observers to update the UI when state flow changes
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMedication() }
                launch { collectErrorMessage() }
                launch { collectLoadingSpinner() }
            }
        }
    }

    // Collect medication details from the view model
    private suspend fun collectMedication() {
        viewModel.medication.collect { medicationDetails ->
            if (medicationDetails == null) { toggleContentVisibility(HIDE) }

            else {
                toggleContentVisibility(SHOW)
                setupUI.setupMedicationDetails(medicationDetails)
            }
        }
    }

    // Collect error messages from the view model and display them as toasts
    private suspend fun collectErrorMessage() {
        viewModel.errorMessage.collect { msg ->
            if (msg != null) {
                createToast(this@ViewMedicationActivity, msg)
                viewModel.clearError()
            }
        }
    }

    // Collect loading spinner state
    private suspend fun collectLoadingSpinner() {
        viewModel.isLoading.collect { isLoading ->
            if (isLoading) loadingSpinnerUtil.show() else loadingSpinnerUtil.hide()
        }
    }

    // Toggle the visibility of the layout and edit/delete buttons based on the action
    private fun toggleContentVisibility(action: String) {
        binding.layoutMedSummary.visibility = if (action == HIDE) View.GONE else View.VISIBLE
        binding.layoutEditDeleteButtons.visibility = if (action == HIDE) View.GONE else View.VISIBLE
    }

    // Fetch medication data from the ViewModel (no need to catch errors here, as they are handled
    // in the ViewModel and the error observer for this activity will handle showing the error message)
    private fun fetchMedicationData(medicationId: Long) {
        lifecycleScope.launch {
            viewModel.fetchMedication(medicationId)
        }
    }
}