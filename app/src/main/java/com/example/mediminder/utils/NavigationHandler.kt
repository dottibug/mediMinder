package com.example.mediminder.utils

import android.content.Intent
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.activities.AddMedicationActivity
import com.example.mediminder.activities.HistoryActivity
import com.example.mediminder.activities.MedicationsActivity
import com.example.mediminder.activities.SettingsActivity
import com.google.android.material.navigation.NavigationView

/**
 * Handles navigation between activities
 * @param activity The current activity
 */
class NavigationHandler(
    private val activity: AppCompatActivity
) {

    /**
     * Handles navigation item selection (only starts the activity if it is not the current activity)
     * @param menuItem The selected navigation item
     */
    fun handleNavigationItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> if (activity is MainActivity) return else startMenuItemActivity(HOME)
            R.id.nav_medications -> if (activity is MedicationsActivity) return else startMenuItemActivity(MEDS)
            R.id.nav_history -> if (activity is HistoryActivity) return else startMenuItemActivity(HISTORY)
            R.id.nav_add_medication -> if (activity is AddMedicationActivity) return else startMenuItemActivity(ADD)
            R.id.nav_settings -> startMenuItemActivity(SETTINGS)
        }
        menuItem.isChecked = true
    }

    /**
     * Starts the activity corresponding to the selected navigation item
     * @param menuItem The selected navigation item
     */
    private fun startMenuItemActivity(menuItem: String) {
        val activityClass = when (menuItem) {
            HOME -> MainActivity::class.java
            MEDS -> MedicationsActivity::class.java
            HISTORY -> HistoryActivity::class.java
            ADD -> AddMedicationActivity::class.java
            SETTINGS -> SettingsActivity::class.java
            else -> return
        }
        activity.startActivity(Intent(activity, activityClass))
    }

    /**
     * Update the current navigation selection when the activity changes
     * @param navView The navigation view to update
     */
    fun setNavigationSelection(navView: NavigationView) {
        val menuItem = when (activity) {
            is MainActivity -> navView.menu.findItem(R.id.nav_home)
            is MedicationsActivity -> navView.menu.findItem(R.id.nav_medications)
            is HistoryActivity -> navView.menu.findItem(R.id.nav_history)
            is AddMedicationActivity -> navView.menu.findItem(R.id.nav_add_medication)
            is SettingsActivity -> navView.menu.findItem(R.id.nav_settings)
            else -> null
        }
        menuItem?.isChecked = true
    }

    companion object {
        private const val HOME = "home"
        private const val MEDS = "medications"
        private const val HISTORY = "history"
        private const val ADD = "add"
        private const val SETTINGS = "settings"
    }
}