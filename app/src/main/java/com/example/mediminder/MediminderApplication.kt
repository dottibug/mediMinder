package com.example.mediminder

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mediminder.schedulers.MidnightMedicationScheduler
import com.example.mediminder.workers.CheckMissedMedicationsWorker
import java.util.concurrent.TimeUnit

class MediminderApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // TESt start
        // Check for exact alarm permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Open system settings to allow exact alarms
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                return
            }
        }
        // TEST end

        // Initialize midnight scheduler
        val midnightScheduler = MidnightMedicationScheduler(this)
        midnightScheduler.scheduleMidnightAlarm()
        setupMissedMedicationsWorker()
    }

    private fun setupMissedMedicationsWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val checkMissedMedicationsWorkRequest = PeriodicWorkRequestBuilder<CheckMissedMedicationsWorker>(
            1, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "check_missed_medications",
            ExistingPeriodicWorkPolicy.KEEP,
            checkMissedMedicationsWorkRequest
        )
    }
}