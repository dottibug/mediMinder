package com.example.mediminder.activities

import android.content.Intent
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewbinding.ViewBinding
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityBaseBinding
import com.example.mediminder.fragments.AddMedicationDosageFragment
import com.example.mediminder.fragments.AddMedicationInfoFragment
import com.example.mediminder.fragments.EditDosageFragment
import com.example.mediminder.fragments.EditMedicationInfoFragment
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator

// Base activity for all activities in the app
// Sets up the top app bar and navigation drawer
abstract class BaseActivity : AppCompatActivity() {
    lateinit var baseBinding: ActivityBaseBinding
    protected val drawerLayout get() = baseBinding.drawerLayout
    protected val navView get() = baseBinding.navView
    protected val topAppBar get() = baseBinding.topAppBar
    protected val medicationViewModel: BaseMedicationViewModel by viewModels { BaseMedicationViewModel.Factory }
    protected lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

    // Set up the base activity bindings and inflate the child activity into the base layout
    // Call this only after inflating the child activity layout
    // Accepts an optional loading spinner to show while data is loading
    protected fun setupBaseBinding(
        binding: ViewBinding,
        loadingSpinner: CircularProgressIndicator? = null
    ) {
        setupBaseLayout()
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinner?.let { loadingSpinnerUtil = LoadingSpinnerUtil(it) }
    }

    // Set up the base layout, including the top app bar, navigation drawer, and edge-to-edge display
    protected fun setupBaseLayout() {
        enableEdgeToEdge()
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        super.setContentView(baseBinding.root)
        setupBaseUI(drawerLayout, navView, topAppBar)
    }

    protected fun setupBaseUI(drawer: DrawerLayout, navView: NavigationView, topAppBar: MaterialToolbar) {
        setupAppBar(drawer, topAppBar)
        setupNavigationView(drawer, navView)
    }

    // Set up the top app bar
    private fun setupAppBar(drawer: DrawerLayout, topAppBar: MaterialToolbar) {
        topAppBar.setNavigationOnClickListener { drawer.open() }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.quickAdd -> {
                    if (this is AddMedicationActivity) { false }
                    else {
                        startActivity(ADD)
                        true
                    }
                }
                else -> false
            }
        }
    }

    // Set up the navigation drawer
    private fun setupNavigationView(drawer: DrawerLayout, navView: NavigationView) {
        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            menuItem.isChecked = true
            drawer.close()
            true
        }
    }

    // Handle navigation item selection
    private fun handleNavigationItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> if (this !is MainActivity) startActivity(HOME)
            R.id.nav_medications -> if (this !is MedicationsActivity) startActivity(MEDS)
            R.id.nav_history -> if (this !is HistoryActivity) { startActivity(HISTORY) }
            R.id.nav_add_medication -> if (this !is AddMedicationActivity) { startActivity(ADD)}
            R.id.nav_settings -> startActivity(SETTINGS)
        }
    }

    // Start activity based on the name
    private fun startActivity(name: String) {
        when (name) {
            HOME -> startActivity(Intent(this, MainActivity::class.java))
            MEDS -> startActivity(Intent(this, MedicationsActivity::class.java))
            HISTORY -> startActivity(Intent(this, HistoryActivity::class.java))
            ADD -> startActivity(Intent(this, AddMedicationActivity::class.java))
            SETTINGS -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // Get medication data based on the action (add or edit)
    protected fun getMedicationData(action: MedicationAction): MedicationData? {
       val medFragment = when (action) {
           MedicationAction.ADD -> supportFragmentManager.findFragmentById(
               R.id.fragmentAddMedInfo) as AddMedicationInfoFragment?
           MedicationAction.EDIT -> supportFragmentManager.findFragmentById(
               R.id.fragmentEditMedInfo) as EditMedicationInfoFragment?
       }
        return medFragment?.getMedicationData()
    }

    // Get dosage data based on the action (add or edit)
    protected fun getDosageData(action: MedicationAction): DosageData? {
        // Skip dosage data for as-needed medications
        if (!medicationViewModel.asScheduled.value) return null

        val dosageFragment = when (action) {
            MedicationAction.ADD -> supportFragmentManager.findFragmentById(
                R.id.fragmentAddMedDosage) as AddMedicationDosageFragment?
            MedicationAction.EDIT -> supportFragmentManager.findFragmentById(
                R.id.fragmentEditMedDosage) as EditDosageFragment?
        }
        return dosageFragment?.getDosageData()
    }

    // Medication action enum
    protected enum class MedicationAction {
        ADD,
        EDIT
    }

    companion object {
        private const val HOME = "home"
        private const val MEDS = "medications"
        private const val HISTORY = "history"
        private const val ADD = "add"
        private const val SETTINGS = "settings"
    }
}