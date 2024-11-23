package com.example.mediminder.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Base view model for all view models in the app
// Centralizes error handling
open class BaseViewModel(): ViewModel() {

    // Test start
    private val instanceId = nextId()  // Add this

    init {
        Log.d("ErrorFlow testcat", "BaseViewModel created with instance ID: $instanceId")
    }

    // test end



    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun setErrorMessage(msg: String) {
        Log.d("ErrorFlow testcat", "Setting error message in ViewModel: $msg")

        _errorMessage.value = msg

        Log.d("ErrorFlow testcat", "Current error value: ${_errorMessage.value}")

    }
    fun clearError() { _errorMessage.value = null }

    companion object {

        // test start
        private var idCounter = 0
        private fun nextId() = ++idCounter
        // test end

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                BaseViewModel()
            }
        }
    }
}