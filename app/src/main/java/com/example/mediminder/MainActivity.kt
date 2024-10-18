package com.example.mediminder

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.adapters.MainDateSelectorAdapter
import com.example.mediminder.adapters.MainMedicationAdapter
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.DatabaseSeeder
import com.example.mediminder.data.repositories.MedicationWithDosage
import com.example.mediminder.databinding.ActivityMainBinding
import com.example.mediminder.fragments.AddAsNeededMedicationDialog
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
        observeViewModel()
    }

    private fun setupDatabase() {
        val database = AppDatabase.getDatabase(this)
        val seeder = DatabaseSeeder(database.medicationDao(), database.dosageDao(), database.scheduleDao(), database.medicationLogDao())

        lifecycleScope.launch {
            seeder.clearDatabase()
            seeder.seedDatabase()
        }
    }

    private fun setupUI() {
        setupAppBar()
        setupNavigationView()
        setupRecyclerViews()
        setupFab()
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
            viewModel.selectDate(date)
        }

        binding.dateSelector.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateSelectorAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddMedication.setOnClickListener {
            val addAsNeededMedicationDialog = AddAsNeededMedicationDialog()
            addAsNeededMedicationDialog.show(supportFragmentManager, AddAsNeededMedicationDialog.TAG)
        }
    }

    private fun showAddMedicationDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add As-Needed Medication")
            .setItems(arrayOf("Choose Existing", "Add New")) { _, option ->
                when (option) {
                    0 -> showExistingMedicationsDialog()
                    1 -> showNewMedicationDialog()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun showExistingMedicationsDialog() {
        viewModel.viewModelScope.launch {
            val asNeededMedications = viewModel.asNeededMedications.value
            if (asNeededMedications.isEmpty()) {
                Toast.makeText(this@MainActivity, "No as-needed medications available", Toast.LENGTH_SHORT).show()
            } else {
                val medicationNames = asNeededMedications.map { it.medication.name }.toTypedArray()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Choose Medication")
                    .setItems(medicationNames) { _, med ->
                        val selectedMedication = asNeededMedications[med]
                        addMedicationLog(selectedMedication)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun showNewMedicationDialog() {
        // Implement this method to add a new as-needed medication
        // This will involve creating a custom dialog or navigating to a new activity/fragment
        Toast.makeText(this, "Add new medication feature not implemented yet", Toast.LENGTH_SHORT).show()
    }

    private fun addMedicationLog(medicationWithDosage: MedicationWithDosage) {
        viewModel.viewModelScope.launch {
            // Implement this method to add a new medication log for the selected as-needed medication
            // This should update the database and refresh the medication list
            Toast.makeText(this@MainActivity, "Added ${medicationWithDosage.medication.name}", Toast.LENGTH_SHORT).show()
            // TODO: Update database and refresh medication list
        }
    }

    // https://developer.android.com/topic/libraries/architecture/viewmodel
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { date ->
                binding.selectedDateText.text = date.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.scheduledMedications.collectLatest { medications ->
                medicationAdapter.updateMedications(medications)
            }
        }

        lifecycleScope.launch {
            viewModel.dateSelectorDates.collectLatest { dates ->
                dateSelectorAdapter.updateDates(dates)
            }
        }
    }
}