package com.example.mediminder.utils

import android.view.View
import com.google.android.material.progressindicator.CircularProgressIndicator

// Helper class to show and hide a loading spinner while fetching data
// Activities that use this utility must include a circular progress indicator in their layout
class LoadingSpinnerUtil (private val spinner: CircularProgressIndicator) {
    fun showSpinner() {
        spinner.visibility = View.VISIBLE
    }

    fun hideSpinner() {
        spinner.visibility = View.GONE
    }

    suspend fun <T> whileLoading(dataFetchFunction: suspend () -> T): T {
        try {
            showSpinner()
            return dataFetchFunction()
        } finally {
            hideSpinner()
        }
    }
}