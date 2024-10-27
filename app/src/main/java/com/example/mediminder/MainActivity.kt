package com.example.mediminder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.MainDateSelectorAdapter
import com.example.mediminder.adapters.MainMedicationAdapter
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.DatabaseSeeder
import com.example.mediminder.databinding.ActivityMainBinding
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var binding: ActivityMainBinding
    private lateinit var medicationAdapter: MainMedicationAdapter
    private lateinit var dateSelectorAdapter: MainDateSelectorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        setupDatabase()
        setupUI()
    }

    private fun setupDatabase() {
        val database = AppDatabase.getDatabase(this)
        val seeder = DatabaseSeeder(database.medicationDao(), database.dosageDao(), database.remindersDao(), database.scheduleDao(), database.medicationLogDao())

        lifecycleScope.launch {
            seeder.clearDatabase()
            seeder.seedDatabase()

            viewModel.fetchMedications()
        }
    }

    private fun setupUI() {
        setupAppBar()
        setupNavigationView()
        setupRecyclerViews()
        setupFab()
        observeViewModel()
    }

    // Set up the top app bar
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

    private fun setupRecyclerViews() {
        // Medications recycler view
        medicationAdapter = MainMedicationAdapter()

        binding.medicationList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = medicationAdapter
        }

        // Date selector recycler view
        dateSelectorAdapter = MainDateSelectorAdapter(emptyList()) { date ->
//            viewModel.selectDate(date)
            // todo
        }

        binding.dateSelector.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateSelectorAdapter
        }


    }

    private val addMedicationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.fetchMedications()
        }
    }

    private fun setupFab() {
        binding.fabAddMedication.setOnClickListener {
            val intent = Intent(this, AddMedicationActivity::class.java)
            addMedicationLauncher.launch(intent)
        }
    }

    // https://developer.android.com/topic/libraries/architecture/viewmodel
    // todo put these in to work on once you know the medication is being saved correctly
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { date ->
                binding.selectedDateText.text = date.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.medications.collectLatest { medications ->
                medicationAdapter.setMedications(medications)
            }
        }

        lifecycleScope.launch {
            viewModel.dateSelectorDates.collectLatest { dates ->
                dateSelectorAdapter.updateDates(dates)
            }
        }
    }
}