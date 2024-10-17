package com.example.mediminder

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.databinding.ActivityMainBinding
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        setupUI()
        observeViewModel()
//        setupBackNavigation()
    }

    private fun setupUI() {
        setupAppBar()
//        setupToolbar()
        setupNavigationView()
    }


    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    // todo: navigate to settings
                    Log.i("testcat", "Navigate to settings")
                    true
                }
                else -> false
            }
        }
    }



    // Set up toolbar (replace back button with menu button that opens navigation drawer)
//    private fun setupToolbar() {
//        supportActionBar?.setDisplayHomeAsUpEnabled(false)
//        supportActionBar?.title = getString(R.string.app_name)
//    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_menu -> {
//                if (binding.main.isDrawerOpen(GravityCompat.END)) {
//                    binding.main.closeDrawer(GravityCompat.END)
//                } else {
//                    binding.main.openDrawer(GravityCompat.END)
//                }
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    // Set up navigation drawer menu items
    private fun setupNavigationView() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // todo: navigate to home screen
                    Log.i("testcat", "Navigate to home")
                }
                R.id.nav_profile -> {
                    // todo: navigate to profile screen
                    Log.i("testcat", "Navigate to profile")
                }
                R.id.nav_medications -> {
                    // todo: navigate to medications screen
                    Log.i("testcat", "Navigate to medications")
                }
                R.id.nav_schedule -> {
                    // todo: navigate to schedule screen
                    Log.i("testcat", "Navigate to schedule")
                }
                R.id.nav_tracking -> {
                    // todo: navigate to tracking screen
                    Log.i("testcat", "Navigate to tracking")
                }
                R.id.nav_refills -> {
                    // todo: navigate to refills screen
                    Log.i("testcat", "Navigate to refills")
                }
                R.id.nav_settings -> {
                    // todo: navigate to settings screen
                    Log.i("testcat", "Navigate to settings")
                }
            }

            menuItem.isChecked = true
            binding.drawerLayout.close()
            true
        }
    }

    // Handle back button press: Close navigation drawer if open, otherwise go back to previous screen
//    private fun setupBackNavigation() {
//        onBackPressedDispatcher.addCallback(this) {
//            if (binding.main.isDrawerOpen(GravityCompat.END)) {
//                binding.main.closeDrawer(GravityCompat.END)
//            } else {
//                isEnabled = false
//                onBackPressedDispatcher.onBackPressed()
//            }
//        }
//    }

    // https://developer.android.com/topic/libraries/architecture/viewmodel
    private fun observeViewModel() {
        // Collect date changes from the view model
        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { date ->
                // todo: update ui with the selected date
            }
        }

        // Collect medication changes from the view model
        lifecycleScope.launch {
            viewModel.medications.collectLatest { medications ->
                // todo: update ui with the medications for the selected date
            }
        }
    }
}