package com.example.mediminder.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.mediminder.R
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.models.MedicationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicationActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(LOG_ID, NULL_INT)
        if (logId == NULL_INT) { return }

        val action = intent.action

        if (action == TAKE_MEDICATION || action == SKIP_MEDICATION) {
            val newStatus = when (action) {
                TAKE_MEDICATION -> MedicationStatus.TAKEN
                SKIP_MEDICATION -> MedicationStatus.SKIPPED
                else -> return
            }

        // Update medication status in database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                database.medicationLogDao().updateStatus(logId, newStatus)

                // Send broadcast to update UI
                withContext(Dispatchers.Main) {
                    val updateIntent = Intent(MED_STATUS_CHANGED).apply {
                        setPackage(context.packageName)
                        putExtra(LOG_ID, logId)
                        putExtra(NEW_STATUS, newStatus.toString())
                    }
                    context.sendBroadcast(updateIntent)

                    val message = getMessage(action, context)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MedActionReceiver testcat", "Error updating status: ${e.message}")
            }
        }

        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(logId.toInt())

        } else { return }
    }

    private fun getMessage(action: String, context: Context): String {
        return when (action) {
            TAKE_MEDICATION -> context.getString(R.string.take_action_response)
            SKIP_MEDICATION -> context.getString(R.string.skip_action_response)
            else -> ""
        }
    }

    companion object {
        private const val LOG_ID = "logId"
        private const val NULL_INT = -1L
        private const val MED_STATUS_CHANGED = "com.example.mediminder.MEDICATION_STATUS_CHANGED"
        private const val NEW_STATUS = "newStatus"
        private const val TAKE_MEDICATION = "take_medication"
        private const val SKIP_MEDICATION = "skip_medication"
    }
}