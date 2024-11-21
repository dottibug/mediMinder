package com.example.mediminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mediminder.activities.BaseActivity
import com.example.mediminder.adapters.MainDateSelectorAdapter
import com.example.mediminder.adapters.MainMedicationAdapter
import com.example.mediminder.data.InitializeDatabase
import com.example.mediminder.databinding.ActivityMainBinding
import com.example.mediminder.fragments.AddAsNeededMedicationDialog
import com.example.mediminder.fragments.UpdateMedicationStatusDialogFragment
import com.example.mediminder.utils.AppUtils
import com.example.mediminder.utils.AppUtils.createToast
import com.example.mediminder.utils.AppUtils.setupWindowInsets
import com.example.mediminder.utils.Constants.MED_STATUS_CHANGED
import com.example.mediminder.utils.LoadingSpinnerUtil
import com.example.mediminder.viewmodels.MainViewModel
import kotlinx.coroutines.launch

// Main activity of the application. Displays a list of scheduled medications for the selected date.
// Users can add new medications and update the status of a scheduled medication, as well as add
// as-needed medications.
class MainActivity : BaseActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private lateinit var binding: ActivityMainBinding
    private lateinit var medicationAdapter: MainMedicationAdapter
    private lateinit var dateSelectorAdapter: MainDateSelectorAdapter
    private lateinit var loadingSpinnerUtil: LoadingSpinnerUtil

    // Broadcast receiver for medication status changes
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
        setupBaseUI(drawerLayout, navView, topAppBar)
        createNotificationChannel()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        setupListeners()
        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        initializeDatabaseAndFetchData()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(statusChangeReceiver)
    }

    // Register the status change receiver
    private fun registerStatusChangeReceiver() {
        registerReceiver(
            statusChangeReceiver,
            IntentFilter(MED_STATUS_CHANGED),
            Context.RECEIVER_EXPORTED
        )
    }

    // Setup bindings for the activity
    private fun setupBindings() {
        setupBaseLayout()
        binding = ActivityMainBinding.inflate(layoutInflater)
        baseBinding.contentContainer.addView(binding.root)
        setupWindowInsets(binding.root)
        loadingSpinnerUtil = LoadingSpinnerUtil(binding.loadingSpinner)
    }

    // Initialize the database and fetch data
    private fun initializeDatabaseAndFetchData() {
        lifecycleScope.launch {
            loadingSpinnerUtil.whileLoading {
                try {
                    InitializeDatabase(applicationContext).initDatabase()
                    viewModel.fetchMedicationsForDate(viewModel.selectedDate.value)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error initializing database: ${e.message}", e)
                }
            }
        }
    }

    // Setup listeners for UI elements
    private fun setupListeners() {
        binding.buttonAddUnscheduled.setOnClickListener {
            AddAsNeededMedicationDialog().show(supportFragmentManager, TAG_ADD_AS_NEEDED_MED)
        }
    }

    // Setup recycler views for date selector and medication list
    private fun setupRecyclerViews() {
        medicationAdapter = MainMedicationAdapter (
            // Update medication status callback
            onUpdateStatusClick = { logId ->
                UpdateMedicationStatusDialogFragment
                    .newInstance(logId)
                    .show(supportFragmentManager, TAG_UPDATE_STATUS)
            },
            // Delete medication callback
            onDeleteAsNeededClick = { logId -> viewModel.deleteAsNeededMedication(logId) }
        )

        dateSelectorAdapter = MainDateSelectorAdapter { date -> viewModel.selectDate(date) }

        setupMedicationList()
        setupDateSelector()
    }

    // Setup medication list and adapter
    private fun setupMedicationList() {
        binding.medicationList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = medicationAdapter
        }
    }

    // Setup date selector and adapter
    private fun setupDateSelector() {
        binding.dateSelector.apply {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL,
                false)
            adapter = dateSelectorAdapter
        }
    }

    // Observe view model data
    private fun setupObservers() {
        lifecycleScope.launch {
            // Only collect latest data flow when the activity is in the STARTED state to avoid UI
            // updates when the activity is not visible to the user
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { collectMedications() }         // Medication list
                launch { collectSelectedDate() }        // Selected date text
                launch { collectDateSelectorDates() }   // Date selector dates
                launch { collectErrorMessage() }         // Error message
            }
        }
    }

    // Collect medication data from the view model
    private suspend fun collectMedications() {
        viewModel.medications.collect { medications -> medicationAdapter.submitList(medications) }
    }

    // Collect selected date from the view model
    private suspend fun collectSelectedDate() {
        viewModel.selectedDate.collect { date ->
            binding.selectedDateText.text = AppUtils.formatToLongDate(date)
        }
    }

    // Collect date selector dates from the view model
    private suspend fun collectDateSelectorDates() {
        viewModel.dateSelectorDates.collect { dates ->
            dateSelectorAdapter.submitList(dates)
            dateSelectorAdapter.updateSelectedPosition()
        }
    }

    // Collect error state from the view model
    private suspend fun collectErrorMessage() {
        viewModel.errorMessage.collect { msg ->
            if (msg != null) {
                createToast(this@MainActivity, msg)
                viewModel.clearError()
            }
        }
    }

    // Create notification channel to display medication reminders
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            MED_REMINDERS_CHANNEL_ID,
            MED_REMINDERS_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = MED_REMINDERS_CHANNEL_DESCRIPTION }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG_ADD_AS_NEEDED_MED = "add_as_needed_med"
        private const val TAG_UPDATE_STATUS = "update_status"
        private const val MED_REMINDERS_CHANNEL_ID = "medication_reminders"
        private const val MED_REMINDERS_CHANNEL_NAME = "Medication Reminders"
        private const val MED_REMINDERS_CHANNEL_DESCRIPTION = "Channel for medication reminders"
    }
}