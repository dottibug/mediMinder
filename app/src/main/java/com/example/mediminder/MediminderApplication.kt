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

class MediminderApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize midnight scheduler
        val midnightScheduler = MidnightMedicationScheduler(this)
        midnightScheduler.scheduleMidnightAlarm()
        setupMissedMedicationsWorker()
        setupFutureMedicationLogsWorker()
    }


    private fun setupMissedMedicationsWorker() {
        val settingsPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gracePeriod = settingsPrefs.getString(GRACE_PERIOD_KEY, GRACE_PERIOD_DEFAULT)

        val repeatInterval = when (gracePeriod) {
            "0.5" -> 30L
            "1" -> 60L
            else -> 120L // Default to 2 hours if period is longer than 1 hour
        }

        // NOTE: For development/testing purposes only, run every 15 minutes
        val testRepeatInterval = 15L

        val missedMedsWorkReq = PeriodicWorkRequestBuilder<CheckMissedMedicationsWorker>(
//            testRepeatInterval,
            repeatInterval,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            MISSED_MED_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            missedMedsWorkReq
        )
    }

    private fun setupFutureMedicationLogsWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val createFutureMedicationLogsRequest = PeriodicWorkRequestBuilder<CreateFutureMedicationLogsWorker>(
            1,
            TimeUnit.DAYS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            FUTURE_MED_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            createFutureMedicationLogsRequest
        )
    }

    companion object {
        private const val MISSED_MED_WORKER_NAME = "check_missed_medications"
        private const val FUTURE_MED_WORKER_NAME = "create_future_medication_logs"
        private const val GRACE_PERIOD_KEY = "grace_period"
        private const val GRACE_PERIOD_DEFAULT = "1"
    }
}