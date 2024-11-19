package com.example.mediminder

import android.app.Application
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mediminder.schedulers.MidnightMedicationScheduler
import com.example.mediminder.workers.CheckMissedMedicationsWorker
import com.example.mediminder.workers.CreateFutureMedicationLogsWorker
import java.util.concurrent.TimeUnit

// Application class for the app.
// Initializes the midnight scheduler (creates alarms for the next day)
// Initializes a worker to check for and mark missed medications at set intervals
// Initializes a worker to create future medication logs
class MediminderApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initializeSchedulers()
    }

    private fun initializeSchedulers() {
        MidnightMedicationScheduler(this).scheduleMidnightAlarm()
        setupMissedMedicationsWorker()
        setupFutureMedicationLogsWorker()
    }

    // Sets up worker to check for and mark missed medications at set intervals
    // The interval is based on the user's grace period setting
    private fun setupMissedMedicationsWorker() {
        // NOTE: For development/testing purposes only, run every 15 minutes
        // val testRepeatInterval = 15L

        val repeatInterval = getGracePeriod()
        val workRequest = PeriodicWorkRequestBuilder<CheckMissedMedicationsWorker>(
            repeatInterval,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            MISSED_MED_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    // Returns the grace period in minutes based on the user's settings
    private fun getGracePeriod(): Long {
        val settingsPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gracePeriod = settingsPrefs.getString(GRACE_PERIOD_KEY, GRACE_PERIOD_DEFAULT)
        return when (gracePeriod) {
            "0.5" -> 30L
            "1" -> 60L
            else -> 120L // Default to 2 hours if period is longer than 1 hour
        }
    }

    // Sets up worker to create future medication logs once per day
    private fun setupFutureMedicationLogsWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<CreateFutureMedicationLogsWorker>(
            1,
            TimeUnit.DAYS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            FUTURE_MED_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        private const val MISSED_MED_WORKER_NAME = "check_missed_medications"
        private const val FUTURE_MED_WORKER_NAME = "create_future_medication_logs"
        private const val GRACE_PERIOD_KEY = "grace_period"
        private const val GRACE_PERIOD_DEFAULT = "1"
    }
}