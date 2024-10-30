package com.example.mediminder.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mediminder.MainActivity
import com.example.mediminder.R

// https://developer.android.com/develop/ui/views/notifications
// https://developer.android.com/develop/ui/views/notifications/build-notification
class MedicationReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmDebug", "MedicationReminderReceiver triggered")

        val medicationId = intent.getLongExtra("medicationId", -1)
        val medicationName = intent.getStringExtra("medicationName") ?: "Medication"

        Log.d("AlarmDebug", "Received alarm for medication: $medicationName (ID: $medicationId)")

        // todo: do we want to include dosage in notification?
        val dosage = intent.getStringExtra("dosage") ?: ""

        // Create notification channel
        createNotificationChannel(context)

        // -- NOTIFICATION TAP ACTIONS --
        // Open main activity
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Pending intent must wrap the intent for notifications
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // User action is to take the medication
        val takeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = "TAKE_MEDICATION"
            putExtra("medicationId", medicationId)
        }

        val takePendingIntent = PendingIntent.getBroadcast(
            context, 1, takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // User action is to skip the medication
        val skipIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = "SKIP_MEDICATION"
            putExtra("medicationId", medicationId)
        }

        val skipPendingIntent = PendingIntent.getBroadcast(
            context, 2, skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (showMedicationName(context)) {
            "Time to take $medicationName"
        } else { "Time to take medication" }

        // Build notification
        // todo settings option that allows users to show/hide name of medication in notifications
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_small)
            .setContentTitle("Medication Reminder")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.taken, "Take", takePendingIntent)
            .addAction(R.drawable.skipped, "Skip", skipPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medicationId.toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Medication Reminder"
        val descriptionText = "Channel for medication reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH // high importance allows peeking
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showMedicationName(context: Context): Boolean {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean("show_medication_name", true) // default to true
    }

    companion object {
        const val CHANNEL_ID = "medication_reminders"
    }
}