package com.example.mediminder.workers

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.utils.Constants.LOG_ID
import com.example.mediminder.utils.Constants.MED_STATUS_CHANGED
import com.example.mediminder.utils.Constants.NEW_STATUS
import java.time.LocalDateTime

// https://developer.android.com/reference/androidx/work/Worker
// Notes: Workers are the modern implementation of deprecated asyncTask. An important distinction is
// that asyncTask will stop when the app is killed, while workers will not.

// This background worker checks for missed medications and marks them as missed. The worker runs at
// set intervals, depending on the user's grace period setting. The worker will run in the background
// even if the app is destroyed.
class CheckMissedMedicationsWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val medicationLogDao = database.medicationLogDao()

        val settingsPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gracePeriod = settingsPrefs.getString(GRACE_PERIOD_KEY, GRACE_PERIOD_DEFAULT)?.toDouble() ?: 1.0

        // NOTE: For development/testing purposes only, set grace period to 0.0
        // val gracePeriod = 0.0

        return try {
            val cutoffTime = getCutOffTime(gracePeriod)

            // Get logs still in pending status before the cutoff time (they will be marked as missed)
            val pendingLogs = medicationLogDao.getPendingMedicationLogs(cutoffTime)

            pendingLogs.forEach { log ->
                medicationLogDao.updateStatus(log.id, MedicationStatus.MISSED)

                // Send broadcast to application to update the UI with the new missed status
                val updateIntent = Intent(MED_STATUS_CHANGED).apply {
                    setPackage(applicationContext.packageName)
                    putExtra(LOG_ID, log.id)
                    putExtra(NEW_STATUS, MedicationStatus.MISSED.toString())
                }
                applicationContext.sendBroadcast(updateIntent)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    // Calculate cutoff time (current time minus grace period in minutes)
    private fun getCutOffTime(gracePeriod: Double): LocalDateTime {
        return LocalDateTime.now().minusMinutes((gracePeriod * 60).toLong())
    }

    companion object {
        private const val GRACE_PERIOD_KEY = "grace_period"
        private const val GRACE_PERIOD_DEFAULT = "1"
    }
}