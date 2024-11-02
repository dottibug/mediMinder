package com.example.mediminder.activities

import android.content.Intent
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityBaseBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

// Base activity for all activities in the app
// Sets up the top app bar and navigation drawer
abstract class BaseActivity : AppCompatActivity() {
    lateinit var baseBinding: ActivityBaseBinding

    // Protected properties to allow child activities to access base UI elements
    protected val drawerLayout get() = baseBinding.drawerLayout
    protected val navView get() = baseBinding.navView
    protected val topAppBar get() = baseBinding.topAppBar
    protected val contentContainer get() = baseBinding.contentContainer

    protected fun setupBaseLayout() {
        baseBinding = ActivityBaseBinding.inflate(layoutInflater)
        super.setContentView(baseBinding.root)
        setupBaseUI(baseBinding.drawerLayout, baseBinding.navView, baseBinding.topAppBar)
    }

    protected fun setupBaseUI(
        drawer: DrawerLayout,
        navView: NavigationView,
        topAppBar: MaterialToolbar
    ) {
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
                    if (this !is MainActivity) { startActivity(Intent(this, MainActivity::class.java)) }
                }

                R.id.nav_medications -> {
                    if (this !is MedicationsActivity) { startActivity(Intent(this, MedicationsActivity::class.java)) }
                }

                R.id.nav_profile -> Log.i("Navigation", "Navigate to profile")
                R.id.nav_schedule -> Log.i("Navigation", "Navigate to schedule")
                R.id.nav_tracking -> Log.i("Navigation", "Navigate to tracking")
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
    }
}