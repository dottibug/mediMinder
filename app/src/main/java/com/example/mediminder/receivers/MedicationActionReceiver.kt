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
        val logId = intent.getLongExtra("logId", -1)
        if (logId == -1L) { return }

        val action = intent.action

        if (action == "TAKE_MEDICATION" || action == "SKIP_MEDICATION") {
            val newStatus = when (action) {
                "TAKE_MEDICATION" -> MedicationStatus.TAKEN
                "SKIP_MEDICATION" -> MedicationStatus.SKIPPED
                else -> return
            }

// Update medication status in database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                database.medicationLogDao().updateStatus(logId, newStatus)

                // Send broadcast to update UI
                withContext(Dispatchers.Main) {
                    val updateIntent = Intent("com.example.mediminder.MEDICATION_STATUS_CHANGED").apply {
                        setPackage(context.packageName)
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

        else { return }
    }
}