package com.example.mediminder.workers

import android.content.Context
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

        return try {
            // Calculate cutoff time (current time minus grace period)
            val cutoffTime = LocalDateTime.now().minusHours(GRACE_PERIOD_HOURS)

            // Get all logs that are still in pending status before the cutoff time
            // These logs have not been marked as taken or skipped, so they are missed
            val pendingLogs = medicationLogDao.getPendingMedicationLogs(cutoffTime)

            pendingLogs.forEach { log ->
                medicationLogDao.updateStatus(log.id, MedicationStatus.MISSED)
            }

            Result.success()

        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private val TAG = "CheckMissedMedicationsWorker"
        // todo: do not hardcode the grace period; default to 2L, but allow user to change it in settings
        private const val GRACE_PERIOD_HOURS = 2L

        // TEST: Grace period for development purposes
//        private const val GRACE_PERIOD_HOURS = 0L
    }

}