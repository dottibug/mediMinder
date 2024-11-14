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
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.mediminder.activities.BaseActivity
import com.example.mediminder.adapters.MainDateSelectorAdapter
import com.example.mediminder.adapters.MainMedicationAdapter
import com.example.mediminder.data.InitializeDatabase
import com.example.mediminder.databinding.ActivityMainBinding
import com.example.mediminder.fragments.AddAsNeededMedicationDialog
import com.example.mediminder.fragments.UpdateMedicationStatusDialogFragment
import com.example.mediminder.utils.AppUtils
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.MainViewModel
import com.example.mediminder.workers.CreateFutureMedicationLogsWorker
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
            if (intent?.action == MED_STATUS_CHANGED) {
                lifecycleScope.launch {
                    viewModel.fetchMedicationsForDate(viewModel.selectedDate.value)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBindings()
        registerStatusChangeReceiver()
        setupUI()
        createNotificationChannel()
        lifecycleScope.launch { initializeDatabaseAndFetchData() }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(statusChangeReceiver)
    }

    private fun registerStatusChangeReceiver() {
        registerReceiver(
            statusChangeReceiver,
            IntentFilter(MED_STATUS_CHANGED),
            Context.RECEIVER_EXPORTED
        )
    }

    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityMainBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    // Coroutine off the main thread to avoid blocking the UI
    private suspend fun initializeDatabaseAndFetchData() {
        loadingSpinnerUtil.whileLoading {
            try {
                InitializeDatabase(applicationContext).initDatabase()
                forceFutureLogsWorker()
                viewModel.fetchMedicationsForDate(LocalDate.now())
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing database: ${e.message}", e)
            }
        }
    }

    private fun setupUI() {
        setupBaseUI(drawerLayout, navView, topAppBar)
        setupListeners()
        setupRecyclerViews()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.buttonAddUnscheduled.setOnClickListener {
            AddAsNeededMedicationDialog().show(supportFragmentManager, "add_as_needed_med")
        }
    }

    private fun setupRecyclerViews() {
        medicationAdapter = MainMedicationAdapter (
            // Update medication status callback
            onUpdateStatusClick = { logId ->
                UpdateMedicationStatusDialogFragment
                    .newInstance(logId)
                    .show(supportFragmentManager, "update_status")
            },
            // Delete medication callback
            onDeleteAsNeededClick = { logId -> viewModel.deleteAsNeededMedication(logId) }
        )

        dateSelectorAdapter = MainDateSelectorAdapter { date -> viewModel.selectDate(date) }

        setupMedicationList()
        setupDateSelector()
    }

    private fun setupMedicationList() {
        binding.medicationList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = medicationAdapter
        }
    }

    private fun setupDateSelector() {
        binding.dateSelector.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false)
            adapter = dateSelectorAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Only collect latest data flow when the activity is in the STARTED state
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMedications() }         // Medication list
                launch { collectSelectedDate() }        // Selected date text
                launch { collectDateSelectorDates() }   // Date selector dates
                launch { collectErrorState() }          // Error state
            }
        }
    }

    private suspend fun collectMedications() {
        viewModel.medications.collect { medications -> medicationAdapter.submitList(medications) }
    }

    private suspend fun collectSelectedDate() {
        viewModel.selectedDate.collect { date ->
            binding.selectedDateText.text = AppUtils.formatToLongDate(date)
        }
    }

    private suspend fun collectDateSelectorDates() {
        viewModel.dateSelectorDates.collect { dates ->
            dateSelectorAdapter.submitList(dates)
            dateSelectorAdapter.updateSelectedPosition()
//            dateSelectorAdapter.updateDates(dates)
        }
    }

    private suspend fun collectErrorState() {
        viewModel.errorState.collect { error ->
            error?.let {
                Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            MED_REMINDERS_CHANNEL_ID,
            MED_REMINDERS_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = MED_REMINDERS_CHANNEL_DESCRIPTION }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

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

    companion object {
        private const val MED_STATUS_CHANGED = "com.example.mediminder.MEDICATION_STATUS_CHANGED"
        private const val MED_REMINDERS_CHANNEL_ID = "medication_reminders"
        private const val MED_REMINDERS_CHANNEL_NAME = "Medication Reminders"
        private const val MED_REMINDERS_CHANNEL_DESCRIPTION = "Channel for medication reminders"
    }
}