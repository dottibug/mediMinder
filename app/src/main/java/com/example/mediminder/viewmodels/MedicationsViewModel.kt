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
import com.example.mediminder.utils.Constants.ERR_FETCHING_MEDS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel for the MedicationsActivity. Fetches medications from the repository for the activity to display.
class MedicationsViewModel(private val repository: MedicationRepository) : ViewModel() {

    private val _medications = MutableStateFlow<List<MedicationWithDetails>>(emptyList())
    val medications: StateFlow<List<MedicationWithDetails>> = _medications.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    // Fetch medications from the repository (updates UI when data is available)
    fun fetchMedications() {
        viewModelScope.launch {
            try {
                _medications.value = repository.getAllMedicationsDetailed()
            } catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_MEDS, e)
                _errorMessage.value = ERR_FETCHING_MEDS
            }
        }
    }

    companion object {
        const val TAG = "MedicationsViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                MedicationsViewModel(repository)
            }
        }
    }
}