package com.example.mediminder.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.repositories.MedicationRepository
import com.example.mediminder.utils.AppUtils.createMedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel for the DeleteMedicationActivity
class DeleteMedicationViewModel(
    private val repository: MedicationRepository,
) : ViewModel() {
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    // Delete medication from the repository
    suspend fun deleteMedication(medId: Long) {
        viewModelScope.launch {
            try {
                _isDeleting.value = true
                repository.deleteMedication(medId)
            } catch (e: Exception) {
                throw e
            } finally {
                _isDeleting.value = false
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