package com.example.mediminder.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.MedicationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicationActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedActionReceiver testcat", "Receiver triggered")

        val logId = intent.getLongExtra("logId", -1)

        Log.d("MedActionReceiver testcat", "Received action with logId: $logId")

        if (logId == -1L) {
            Log.e("MedActionReceiver testcat", "Invalid logId received")
            return
        }

        val action = intent.action
        Log.d("MedActionReceiver testcat", "Action received: $action")

        val newStatus = when (action) {
            "TAKE_MEDICATION" -> {
                Log.d("MedActionReceiver testcat", "Setting status to TAKEN")
                MedicationStatus.TAKEN
            }
            "SKIP_MEDICATION" -> {
                Log.d("MedActionReceiver testcat", "Setting status to SKIPPED")
                MedicationStatus.SKIPPED
            }
            else -> {
                Log.e("MedActionReceiver testcat", "Invalid action received: $action")
                return
            }
        }

// Update medication status in database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                database.medicationLogDao().updateStatus(logId, newStatus)

                // Send broadcast to update UI
                withContext(Dispatchers.Main) {
                    val updateIntent = Intent("com.example.mediminder.MEDICATION_STATUS_CHANGED").apply {
                        putExtra("logId", logId)
                        putExtra("newStatus", newStatus.toString())
                    }
                    context.sendBroadcast(updateIntent)

                    val message = if (action == "TAKE_MEDICATION")
                        "Medication marked as taken"
                    else "Medication marked as skipped"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MedActionReceiver testcat", "Error updating status: ${e.message}")
            }
        }

        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(logId.toInt())
    }
}