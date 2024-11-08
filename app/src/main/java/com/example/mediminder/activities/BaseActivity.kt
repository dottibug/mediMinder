package com.example.mediminder.activities

import android.content.Intent
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityBaseBinding
import com.example.mediminder.fragments.AddMedicationDosageFragment
import com.example.mediminder.fragments.AddMedicationInfoFragment
import com.example.mediminder.fragments.EditDosageFragment
import com.example.mediminder.fragments.EditMedicationInfoFragment
import com.example.mediminder.models.DosageData
import com.example.mediminder.models.MedicationData
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

// Base activity for all activities in the app
// Sets up the top app bar and navigation drawer
abstract class BaseActivity : AppCompatActivity() {
    lateinit var baseBinding: ActivityBaseBinding
    protected val drawerLayout get() = baseBinding.drawerLayout
    protected val navView get() = baseBinding.navView
    protected val topAppBar get() = baseBinding.topAppBar

    protected fun setupBaseLayout() {
        enableEdgeToEdge()
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        super.setContentView(baseBinding.root)
        setupBaseUI(baseBinding.drawerLayout, baseBinding.navView, baseBinding.topAppBar)
    }

    protected fun setupBaseUI(drawer: DrawerLayout, navView: NavigationView, topAppBar: MaterialToolbar) {
        setupAppBar(drawer, topAppBar)
        setupNavigationView(drawer, navView)
    }

    private fun setupAppBar(drawer: DrawerLayout, topAppBar: MaterialToolbar) {
        topAppBar.setNavigationOnClickListener { drawer.open() }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNavigationView(drawer: DrawerLayout, navView: NavigationView) {
        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            menuItem.isChecked = true
            drawer.close()
            true
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                if (this !is MainActivity) {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }

            R.id.nav_medications -> {
                if (this !is MedicationsActivity) {
                    startActivity(Intent(this, MedicationsActivity::class.java))
                }
            }

            R.id.nav_history -> {
                if (this !is HistoryActivity) {
                    startActivity(Intent(this, HistoryActivity::class.java))
                }
            }

            R.id.nav_add_medication -> {
                if (this !is AddMedicationActivity) {
                    startActivity(Intent(this, AddMedicationActivity::class.java))
                }
            }

            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    protected fun getMedicationData(action: MedicationAction): MedicationData? {
       val medFragment = when (action) {
           MedicationAction.ADD -> supportFragmentManager.findFragmentById(
               R.id.fragmentAddMedInfo) as AddMedicationInfoFragment?
           MedicationAction.EDIT -> supportFragmentManager.findFragmentById(
               R.id.fragmentEditMedInfo) as EditMedicationInfoFragment?
       }
        return medFragment?.getMedicationData()
    }

    protected fun getDosageData(action: MedicationAction): DosageData? {
        val dosageFragment = when (action) {
            MedicationAction.ADD -> supportFragmentManager.findFragmentById(
                R.id.fragmentAddMedDosage) as AddMedicationDosageFragment?
            MedicationAction.EDIT -> supportFragmentManager.findFragmentById(
                R.id.fragmentEditMedDosage) as EditDosageFragment?
        }
        return dosageFragment?.getDosageData()
    }

    protected enum class MedicationAction {
        ADD,
        EDIT
    }
}