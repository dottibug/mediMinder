package com.example.mediminder.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.mediminder.data.local.AppDatabase
import com.example.mediminder.data.local.classes.MedicationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra("medicationId", -1)
        if (medicationId == -1L) return

        val action = intent.action
        val newStatus = when (action) {
            "TAKE_MEDICATION" -> MedicationStatus.TAKEN
            "SKIP_MEDICATION" -> MedicationStatus.SKIPPED
            else -> return
        }

        // Update medication status in database
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            database.medicationLogDao().updateStatus(medicationId, newStatus)
        }

        // Show toast
        val message = if (action == "TAKE_MEDICATION") "Medication marked as taken" else "Medication marked as skipped"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(medicationId.toInt())
    }
}