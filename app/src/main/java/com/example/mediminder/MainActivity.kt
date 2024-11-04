package com.example.mediminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.mediminder.activities.AddMedicationActivity
import com.example.mediminder.activities.BaseActivity
import com.example.mediminder.adapters.MainDateSelectorAdapter
import com.example.mediminder.adapters.MainMedicationAdapter
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.DatabaseSeeder
import com.example.mediminder.databinding.ActivityMainBinding
import com.example.mediminder.fragments.UpdateMedicationStatusDialogFragment
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.MainViewModel
import com.example.mediminder.workers.CreateFutureMedicationLogsWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var binding: ActivityMainBinding
    private lateinit var medicationAdapter: MainMedicationAdapter
    private lateinit var dateSelectorAdapter: MainDateSelectorAdapter
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

    private val statusChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("MainActivity testcat", "Received status change broadcast")

            if (intent?.action == "com.example.mediminder.MEDICATION_STATUS_CHANGED") {
                lifecycleScope.launch {
                    viewModel.fetchMedicationsForDate(viewModel.selectedDate.value)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupBaseLayout()
        binding = ActivityMainBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)

        // Register broadcast receiver
        registerReceiver(
            statusChangeReceiver,
            IntentFilter("com.example.mediminder.MEDICATION_STATUS_CHANGED"),
            Context.RECEIVER_EXPORTED
        )

        setupUI()
        createNotificationChannel()

        lifecycleScope.launch {
            initializeDatabaseAndFetchData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(statusChangeReceiver)
    }

    // Coroutine off the main thread to avoid blocking the UI
    private suspend fun initializeDatabaseAndFetchData() {
        loadingSpinnerUtil.whileLoading {
            try {
                setupDatabase()
                forceFutureLogsWorker()
                viewModel.fetchMedicationsForDate(LocalDate.now())
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing database: ${e.message}", e)
            }
        }
    }

    private fun setupUI() {
        setupBaseUI(drawerLayout, navView, topAppBar)
        setupRecyclerViews()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        medicationAdapter = MainMedicationAdapter { logId ->
            UpdateMedicationStatusDialogFragment.newInstance(logId)
                .show(supportFragmentManager, "update_status")
        }

        binding.medicationList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = medicationAdapter
        }

        dateSelectorAdapter = MainDateSelectorAdapter(emptyList()) { date ->
            viewModel.selectDate(date)
        }

        binding.dateSelector.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateSelectorAdapter
        }
    }

    private val addMedicationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.fetchMedicationsForDate(viewModel.selectedDate.value)
        }
    }

    private fun setupFab() {
        binding.fabAddMedication.setOnClickListener {
            val intent = Intent(this, AddMedicationActivity::class.java)
            addMedicationLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.medications.collectLatest { medications ->
                medicationAdapter.submitList(medications)
            }
        }

        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest {
                date -> binding.selectedDateText.text = date.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.dateSelectorDates.collectLatest { dates ->
                dateSelectorAdapter.updateDates(dates)
            }
        }

        lifecycleScope.launch {
            viewModel.errorState.collectLatest { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "medication_reminders",
            "Medication Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for medication reminders"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun setupDatabase() {
        val database = AppDatabase.getDatabase(this)
        val medicationDao = database.medicationDao()

        // Check if database is empty
        val medicationCount = medicationDao.getCount()

        if (medicationCount == 0) {
            val seeder = DatabaseSeeder(
                applicationContext,
                medicationDao,
                database.dosageDao(),
                database.remindersDao(),
                database.scheduleDao(),
                database.medicationLogDao()
            )

            try {
                seeder.clearDatabase()
                seeder.seedDatabase()
            } catch (e: Exception) {
                Log.e("MainActivity testcat", "Error in setupDatabase: ${e.message}", e)
            }
        } else {
            Log.d("MainActivity testcat", "Database already contains data, skipping seed")
        }
    }

    // Coroutine off the main thread to avoid blocking the UI
//    private suspend fun setupDatabase() {
//        val database = AppDatabase.getDatabase(this)
//
//        val seeder = DatabaseSeeder(
//            applicationContext,
//            database.medicationDao(),
//            database.dosageDao(),
//            database.remindersDao(),
//            database.scheduleDao(),
//            database.medicationLogDao()
//        )
//
//        try {
//            seeder.clearDatabase()
//            seeder.seedDatabase()
//
//            // Verify seeded data
////            val medicationDao = database.medicationDao()
////            val medications = medicationDao.getAllWithRemindersEnabled()
////            Log.d("MainActivity testcat", "Found ${medications.size} medications with reminders enabled")
//        } catch (e: Exception) {
//            Log.e("MainActivity testcat", "Error in setupDatabase: ${e.message}", e)
//        }
//    }

    // NOTE: Development purposes only
    private fun forceFutureLogsWorker() {
        val workRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>().build()

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(workRequest.id)
            .observe(this) { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    viewModel.fetchMedicationsForDate(LocalDate.now())
                }
            }
    }
}