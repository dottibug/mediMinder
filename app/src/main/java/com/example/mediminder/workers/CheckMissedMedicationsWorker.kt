package com.example.mediminder.workers

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.MedicationStatus
import java.time.LocalDateTime


// https://developer.android.com/reference/androidx/work/Worker
// Notes: Workers are used to run code in the background (modern implementation of deprecated asyncTask)
// Workers were chosen instead of the asyncTask methods learned in the course, because I did not want
// the creation of medication logs and notifications to be killed when the app is killed.

class CheckMissedMedicationsWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val medicationLogDao = database.medicationLogDao()

        val settingsPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gracePeriod = settingsPrefs.getString("grace_period", "1")?.toDouble() ?: 1.0

        // NOTE: For development/testing purposes only, set grace period to 0.0 to see a
        //  medication get marked as missed
//        val gracePeriod = 0.0

        return try {
            // Calculate cutoff time (current time minus grace period in minutes)
            val cutoffTime = LocalDateTime.now().minusMinutes((gracePeriod * 60).toLong())

            // Get all logs that are still in pending status before the cutoff time
            // These logs have not been marked as taken or skipped, so they are missed
            val pendingLogs = medicationLogDao.getPendingMedicationLogs(cutoffTime)

            pendingLogs.forEach { log ->
                medicationLogDao.updateStatus(log.id, MedicationStatus.MISSED)

                // Send broadcast for each missed medication to update UI
                val updateIntent = Intent("com.example.mediminder.MEDICATION_STATUS_CHANGED").apply {
                    setPackage(applicationContext.packageName)
                    putExtra("logId", log.id)
                    putExtra("newStatus", MedicationStatus.MISSED.toString())
                }
                applicationContext.sendBroadcast(updateIntent)
            }

            Result.success()

        } catch (e: Exception) {
            Result.retry()
        }
    }
}