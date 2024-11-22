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
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.ERR_UPDATING_STATUS
import com.example.mediminder.utils.Constants.LOG_ID
import com.example.mediminder.utils.Constants.MED_STATUS_CHANGED
import com.example.mediminder.utils.Constants.NEW_STATUS
import com.example.mediminder.utils.Constants.SKIP_MEDICATION
import com.example.mediminder.utils.Constants.TAKE_MEDICATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Broadcast receiver for actions (take or skip) in response to medication notifications
// Updates medication status depending on the user action.
// Cancels the notification after an action is taken or the notification is dismissed by the user.
class MedicationActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(LOG_ID, -1L)
        if (logId == -1L) { return }

        val action = intent.action

        if (action == TAKE_MEDICATION || action == SKIP_MEDICATION) {
            val newStatus = getNewStatus(action) ?: return

            // Update medication status in database
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateMedStatus(context, logId, newStatus)
                    sendBroadcast(context, logId, newStatus, action)
                } catch (e: Exception) {
                    Log.e(TAG, ERR_UPDATING_STATUS, e)
                }
            }
            cancelNotification(context, logId)
        }
    }

    // Get new medication status based on user action
    private fun getNewStatus(action: String): MedicationStatus? {
        return when (action) {
            TAKE_MEDICATION -> MedicationStatus.TAKEN
            SKIP_MEDICATION -> MedicationStatus.SKIPPED
            else -> return null // Does not respond to other broadcast actions
        }
    }

    // Update medication status of the log in the database
    private suspend fun updateMedStatus(context: Context, logId: Long, newStatus: MedicationStatus) {
        AppDatabase.getDatabase(context).medicationLogDao().updateStatus(logId, newStatus)
    }

    // Send broadcast to update UI with new medication status
    private suspend fun sendBroadcast(context: Context, logId: Long, newStatus: MedicationStatus, action: String) {
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
    }

    // Get message based on user action
    private fun getMessage(action: String, context: Context): String {
        return when (action) {
            TAKE_MEDICATION -> context.getString(R.string.take_action_response)
            SKIP_MEDICATION -> context.getString(R.string.skip_action_response)
            else -> EMPTY_STRING
        }
    }

    // Cancel notification
    private fun cancelNotification(context: Context, logId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(logId.toInt())
    }

    companion object {
        private const val TAG = "MedActionReceiver"
    }
}