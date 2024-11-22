package com.example.mediminder.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.models.MedicationWithDetails
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// This view model holds state for the view medication activity. It handles fetching medication
// details from the MedicationRepository to display in the UI.
class ViewMedicationViewModel(private val repository: MedicationRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _medication = MutableStateFlow<MedicationWithDetails?>(null)
    val medication: StateFlow<MedicationWithDetails?> = _medication.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    // Fetch medication details from the repository (updates UI when data is available)
    fun fetchMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _medication.value = repository.getMedicationDetailsById(medicationId)
            }
            catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_MED, e)
                _errorMessage.value = ERR_FETCHING_MED_USER
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "ViewMedicationViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                ViewMedicationViewModel(repository)
            }
        }
    }
}