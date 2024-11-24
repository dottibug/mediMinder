package com.example.mediminder.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityBaseBinding
import com.example.mediminder.fragments.DosageFragment
import com.example.mediminder.fragments.MedicationInfoFragment
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationAction
import com.example.mediminder.models.MedicationData
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.NavigationHandler
import com.example.mediminder.viewmodels.AppViewModel
import kotlinx.coroutines.launch

/**
 * Base activity for all activities in the app. Sets up the top app bar, navigation drawer, and
 * handles navigation between activities. This activity also observes error messages from the
 * BaseViewModel and displays them to the user as a toast.
 */
abstract class BaseActivity : AppCompatActivity() {
    protected val appViewModel: AppViewModel by viewModels { AppViewModel.Factory }
    private lateinit var baseBinding: ActivityBaseBinding
    private lateinit var navHandler: NavigationHandler
    private val drawer get() = baseBinding.drawerLayout
    private val navView get() = baseBinding.navView
    private val topAppBar get() = baseBinding.topAppBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        navHandler = NavigationHandler(this)
    }

    override fun onResume() {
        super.onResume()
        navHandler.setNavigationSelection(navView)  // Update selected navigation item
    }

    /**
     * Sets up layout bindings, top app bar, navigation drawer, and error observer.
     * Call this function in child activities AFTER inflating the child activity layout
     * (the child is added to the base layout).
     * @param viewBinding The view binding for the child activity
     */
    protected fun setupBaseBinding(viewBinding: ViewBinding) {
        Log.d("ErrorFlow testcat", "setupBaseBinding called in BaseActivity")
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)  // Inflate base activity layout
        super.setContentView(baseBinding.root)
        baseBinding.contentContainer.addView(viewBinding.root)  // Add child layout
        setupWindowInsets(viewBinding.root)
        setupBaseObservers()
        setupAppBar()
        setupNavigationView()
    }

    /**
     * Sets up window insets for the given view.
     * @param rootView The view to set insets for
     */
    private fun setupWindowInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Observes error messages from AppViewModel to display as a toast
     */
    private fun setupBaseObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appViewModel.errorMessage.collect { error ->
                    if (error != null) {
                        createToast(this@BaseActivity, error)
                        appViewModel.clearError()
                    }
                }
            }
        }
    }

    /**
     * Sets up the top app bar that is displayed in all activities
     */
    private fun setupAppBar() {
        topAppBar.setNavigationOnClickListener { drawer.open() }
        topAppBar.setOnMenuItemClickListener { menuItem -> startAddMedicationActivity(menuItem) }
    }

    /**
     * Start AddMedicationActivity if it is not the current activity
     * @param menuItem The top bar menu item that was clicked
     */
    private fun startAddMedicationActivity(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.quickAdd -> {
                if (this is AddMedicationActivity) { return false }
                startActivity(Intent(this, AddMedicationActivity::class.java))
                return true
            }
            else -> return false
        }
    }

    /**
     * Sets up the navigation drawer for navigation between activities
     */
    private fun setupNavigationView() {
        navView.setNavigationItemSelectedListener { menuItem ->
            navHandler.handleNavigationItemSelected(menuItem)
            menuItem.isChecked = true
            drawer.close()
            true
        }
    }

    /**
     * Get medication data from the relevant fragment based on the action (add or edit).
     * @param action The action that triggered this function (add or edit)
     */
    protected fun getMedicationData(action: MedicationAction): MedicationData? {
       val medFragment = when (action) {
           MedicationAction.ADD -> supportFragmentManager.findFragmentById(
               R.id.fragmentAddMedInfo) as MedicationInfoFragment?
           MedicationAction.EDIT -> supportFragmentManager.findFragmentById(
               R.id.fragmentEditMedInfo) as MedicationInfoFragment?
       }
        return medFragment?.getMedicationData()
    }

    /**
     * Get dosage data from the relevant fragment based on the action (add or edit).
     * @param action The action that triggered this function (add or edit)
     */
    protected fun getDosageData(action: MedicationAction): DosageData? {
        // Skip dosage data for as-needed medications
        if (!appViewModel.medication.asScheduled.value) return null

        val dosageFragment = when (action) {
            MedicationAction.ADD -> supportFragmentManager.findFragmentById(
                R.id.fragmentDosage) as DosageFragment?
            MedicationAction.EDIT -> supportFragmentManager.findFragmentById(
                R.id.fragmentEditMedDosage) as DosageFragment?
        }
        return dosageFragment?.getDosageData()
    }
}