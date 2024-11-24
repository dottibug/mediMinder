package com.example.mediminder.workers

import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mediminder.receivers.MedicationSchedulerReceiver
import com.example.mediminder.utils.Constants.MED_ID
import com.example.mediminder.utils.Constants.MED_STATUS_CHANGED
import com.example.mediminder.utils.Constants.SCHEDULE_NEW_MEDICATION
import java.util.UUID

/**
 * Manager class for background workers. Creates and observes workers for future medication logs
 * and missed medication checks.
 */
class WorkerManager(private val context: Context) {
    // Create workers to check for missed medications and create future medication logs
    fun createWorkers(workerPrefix: String, medicationId: Long) {
        val workManager = WorkManager.getInstance(context)
        val (createFutureLogsRequest, checkMissedRequest) = createWorkRequests(medicationId)

        // Enqueue the work requests (create future logs then check for missed medications)
        workManager.beginUniqueWork(
            "${workerPrefix}_${medicationId}_${System.currentTimeMillis()}",
            ExistingWorkPolicy.REPLACE,
            createFutureLogsRequest
        ).then(checkMissedRequest).enqueue()

        val futureLogsReqId = createFutureLogsRequest.id
        observeWorkCompletion(workManager, futureLogsReqId)
    }

    // Build work requests for creating future medication logs and checking for missed medications
    private fun createWorkRequests(medId: Long): Pair<OneTimeWorkRequest, OneTimeWorkRequest> {
        val createFutureLogsRequest = OneTimeWorkRequestBuilder<CreateFutureMedicationLogsWorker>()
            .setInputData(workDataOf(MED_ID to medId))
            .build()

        val checkMissedRequest = OneTimeWorkRequestBuilder<CheckMissedMedicationsWorker>().build()
        return Pair(createFutureLogsRequest, checkMissedRequest)
    }

    // Observe work completion of creating future medication logs
    // Send broadcasts to update UI and reschedule notifications upon worker completion
    private fun observeWorkCompletion(workManager: WorkManager, futureLogsReqId: UUID) {
        workManager.getWorkInfoByIdLiveData(futureLogsReqId)
            .observeForever { workInfo ->
                if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
                    // Send broadcast to update UI
                    val updateIntent = Intent(MED_STATUS_CHANGED).apply {
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(updateIntent)

                    // Reschedule notifications
                    val schedulerIntent = Intent(context, MedicationSchedulerReceiver::class.java).apply {
                        action = SCHEDULE_NEW_MEDICATION
                    }
                    context.sendBroadcast(schedulerIntent)
                }
            }
    }
}