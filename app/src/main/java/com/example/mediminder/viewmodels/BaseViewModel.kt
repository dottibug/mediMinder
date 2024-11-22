package com.example.mediminder.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Base view model for all view models in the app
// Centralizes error handling and loading spinner logic
abstract class BaseViewModel: ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun setErrorMessage(msg: String) { _errorMessage.value = msg }
    fun clearError() { _errorMessage.value = null }

    fun startLoading() { _isLoading.value = true }
    fun stopLoading() { _isLoading.value = false }
}