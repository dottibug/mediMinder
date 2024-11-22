package com.example.mediminder.utils

import android.view.View
import com.google.android.material.progressindicator.CircularProgressIndicator

// Helper class to show and hide a loading spinner while fetching data
// Activities that use this utility must include a circular progress indicator in their layout
class LoadingSpinnerUtil (private val spinner: CircularProgressIndicator) {
    fun show() { spinner.visibility = View.VISIBLE }
    fun hide() { spinner.visibility = View.GONE }
}