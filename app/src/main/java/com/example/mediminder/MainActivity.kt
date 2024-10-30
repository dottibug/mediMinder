package com.example.mediminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.mediminder.adapters.MainDateSelectorAdapter
import com.example.mediminder.adapters.MainMedicationAdapter
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.DatabaseSeeder
import com.example.mediminder.databinding.ActivityMainBinding
import com.example.mediminder.fragments.UpdateMedicationStatusDialogFragment
import com.example.mediminder.receivers.MedicationReminderReceiver
import com.example.mediminder.utils.WindowInsetsUtil
import com.example.mediminder.viewmodels.MainViewModel
import com.example.mediminder.workers.CheckMissedMedicationsWorker
import com.example.mediminder.workers.MedicationRemindersWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.concurrent.TimeUnit

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


        lifecycleScope.launch {
            setupDatabase()
            createNotificationChannel()
            setupWorkers()
            setupUI()
        }
    }

    private fun setupDatabase() {
        val database = AppDatabase.getDatabase(this)
        val seeder = DatabaseSeeder(
            database.medicationDao(),
            database.dosageDao(),
            database.remindersDao(),
            database.scheduleDao(),
            database.medicationLogDao()
        )

        lifecycleScope.launch {
            seeder.clearDatabase()
            seeder.seedDatabase()
            viewModel.fetchMedicationsForDate(LocalDate.now())
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
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
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
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }

            menuItem.isChecked = true
            binding.drawerLayout.close()
            true
        }
    }

    private fun setupRecyclerViews() {
        // Medications recycler view
        medicationAdapter = MainMedicationAdapter { medId ->
            UpdateMedicationStatusDialogFragment.newInstance(
                medId = medId
            ) { newStatus ->
                viewModel.updateMedicationStatus(medId, newStatus)
            }.show(supportFragmentManager, "update_medication_status_dialog")
        }

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

    // https://developer.android.com/topic/libraries/architecture/viewmodel
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { date ->
                binding.selectedDateText.text = date.toString()
                viewModel.fetchMedicationsForDate(date)
            }
        }

        lifecycleScope.launch {
            viewModel.medications.collectLatest { medications ->
                medicationAdapter.submitList(medications)
            }
        }

        lifecycleScope.launch {
            viewModel.dateSelectorDates.collectLatest { dates ->
                dateSelectorAdapter.updateDates(dates)
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "Medication Reminder"
        val descriptionText = "Channel for medication reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            MedicationReminderReceiver.CHANNEL_ID,
            name,
            importance
        ).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun setupWorkers() {
        // Check for exact alarm permission
        // // https://developer.android.com/reference/android/app/AlarmManager#canScheduleExactAlarms()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Open system settings to allow exact alarms
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        // https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#work-constraints
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        // Schedule CheckMissedMedicationWorker
        // https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#schedule_periodic_work
        // Performs the work every 1 hour
        val checkMissedMedicationsWorkRequest = PeriodicWorkRequestBuilder<CheckMissedMedicationsWorker>(
            1, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        // TEST: Performs the work every 15 minutes for development purposes
//        val checkMissedMedicationsWorkRequest = PeriodicWorkRequestBuilder<CheckMissedMedicationsWorker>(
//            15, TimeUnit.MINUTES
//        ).setConstraints(constraints).build()

        // Schedule MedicationRemindersWorker
        val medicationRemindersWorkRequest = PeriodicWorkRequestBuilder<MedicationRemindersWorker>(
            30, TimeUnit.MINUTES
        )
            .setInitialDelay(0, TimeUnit.MILLISECONDS)  // Run immediately on first schedule
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("medication_reminders_worker")
            .setConstraints(constraints)
            .build()

        // https://developer.android.com/reference/androidx/work/WorkManager#enqueueUniquePeriodicWork(kotlin.String,androidx.work.ExistingPeriodicWorkPolicy,androidx.work.PeriodicWorkRequest)
        WorkManager.getInstance(applicationContext).apply {
            enqueueUniquePeriodicWork(
                "check_missed_medications",
                ExistingPeriodicWorkPolicy.KEEP,
                checkMissedMedicationsWorkRequest
            )

            cancelAllWorkByTag("medication_reminders_worker")

            enqueueUniquePeriodicWork(
                "medication_reminders",
                ExistingPeriodicWorkPolicy.UPDATE,
                medicationRemindersWorkRequest
            )
        }
    }


}