package com.example.mediminder.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import com.example.mediminder.utils.Constants.ERR_DELETING_MED
import com.example.mediminder.utils.Constants.ERR_DELETING_MED_USER
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED
import com.example.mediminder.utils.Constants.ERR_FETCHING_MED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel for the DeleteMedicationActivity
class DeleteMedicationViewModel(
    private val repository: MedicationRepository,
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _currentMedication = MutableStateFlow<Medication?>(null)
    val currentMedication: StateFlow<Medication?> = _currentMedication.asStateFlow()

    private val _medicationName = MutableStateFlow("")
    val medicationName: StateFlow<String> = _medicationName.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    // Fetch medication details from the repository
    suspend fun fetchMedication(medicationId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val medication = repository.getMedicationById(medicationId)
                _currentMedication.value = medication
                _medicationName.value = medication.name
            } catch (e: Exception) {
                Log.e(TAG, ERR_FETCHING_MED, e)
                _errorMessage.value = ERR_FETCHING_MED_USER
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete medication from the repository
    suspend fun deleteMedication() {
        viewModelScope.launch {
            try {
                _isDeleting.value = true
                _isLoading.value = true

                currentMedication.value?.let { medication ->
                    repository.deleteMedication(medication.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, ERR_DELETING_MED, e)
                _errorMessage.value = ERR_DELETING_MED_USER
            } finally {
                _isDeleting.value = false
                _isLoading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "DeleteMedicationViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val database = AppDatabase.getDatabase(application)
                val repository = createMedicationRepository(database)
                DeleteMedicationViewModel(repository)
            }
        }
    }
}