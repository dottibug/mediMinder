package com.example.mediminder

import android.app.Application
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
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

//        val checkMissedMedicationsWorkRequest = PeriodicWorkRequestBuilder<CheckMissedMedicationsWorker>(
//            1, TimeUnit.HOURS
//        ).setConstraints(constraints).build()

        // NOTE: For development/testing purposes only
        val checkMissedMedicationsWorkRequest = PeriodicWorkRequestBuilder<CheckMissedMedicationsWorker>(
            15, TimeUnit.MINUTES  // Run every 15 minutes for testing
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "check_missed_medications",
            ExistingPeriodicWorkPolicy.KEEP,
            checkMissedMedicationsWorkRequest
        )
    }

    private fun setupFutureMedicationLogsWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val createFutureMedicationLogsRequest = PeriodicWorkRequestBuilder<CreateFutureMedicationLogsWorker>(
            1, TimeUnit.DAYS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "create_future_medication_logs",
            ExistingPeriodicWorkPolicy.KEEP,
            createFutureMedicationLogsRequest
        )
    }
}