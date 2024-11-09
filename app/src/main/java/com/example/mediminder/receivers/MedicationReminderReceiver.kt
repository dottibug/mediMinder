package com.example.mediminder.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.mediminder.MainActivity
import com.example.mediminder.R

// https://developer.android.com/develop/ui/views/notifications
// https://developer.android.com/develop/ui/views/notifications/build-notification
class MedicationReminderReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(LOG_ID, NULL_INT)
        val medicationId = intent.getLongExtra(MED_ID, NULL_INT)
        val medicationName = intent.getStringExtra(MED_NAME) ?: MED_NAME_DEFAULT

        // todo: do we want to include dosage in notification?
        val dosage = intent.getStringExtra(DOSAGE) ?: EMPTY_STRING

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
            action = TAKE_MEDICATION
            putExtra(LOG_ID, logId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            logId.toInt(),
            takeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // User action is to skip the medication
        val skipIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = SKIP_MEDICATION
            putExtra(LOG_ID, logId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val skipPendingIntent = PendingIntent.getBroadcast(
            context,
            (logId + SKIP_OFFSET).toInt(), // Offset to avoid conflicts with "take" actions
            skipIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = getMessage(context, medicationName)

        // Build notification
        // todo settings option that allows users to show/hide name of medication in notifications
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_small)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.taken, context.getString(R.string.take), takePendingIntent)
            .addAction(R.drawable.skipped, context.getString(R.string.skip), skipPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medicationId.toInt(), notification)
    }

    private fun getMessage(context: Context, medicationName: String): String {
        return if (showMedicationName(context)) { context.getString(R.string.time_to_take, medicationName) }
        else { context.getString(R.string.time_to_take_private) }
    }

    private fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH // high importance allows peeking
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showMedicationName(context: Context): Boolean {
        val settingsPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        return settingsPrefs.getBoolean(MED_PRIVACY_KEY, true) // default to true
    }

    companion object {
        private const val LOG_ID = "logId"
        private const val MED_ID = "medicationId"
        private const val MED_NAME = "medicationName"
        private const val DOSAGE = "dosage"
        private const val NULL_INT = -1L
        private const val MED_NAME_DEFAULT = "Medication"
        private const val EMPTY_STRING = ""
        private const val SKIP_OFFSET = 1000
        const val CHANNEL_ID = "medication_reminders"
        private const val CHANNEL_NAME = "Medication Reminders"
        private const val CHANNEL_DESCRIPTION = "Channel for medication reminders"
        private const val TAKE_MEDICATION = "take_medication"
        private const val SKIP_MEDICATION = "skip_medication"
        private const val MED_PRIVACY_KEY = "show_medication_name"
    }
}