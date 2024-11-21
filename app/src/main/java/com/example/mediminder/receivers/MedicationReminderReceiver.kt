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
import com.example.mediminder.utils.Constants.DOSAGE
import com.example.mediminder.utils.Constants.DOSAGE_PRIVACY
import com.example.mediminder.utils.Constants.EMPTY_STRING
import com.example.mediminder.utils.Constants.LOG_ID
import com.example.mediminder.utils.Constants.MED_NAME
import com.example.mediminder.utils.Constants.MED_NAME_DEFAULT
import com.example.mediminder.utils.Constants.MED_PRIVACY
import com.example.mediminder.utils.Constants.MED_REMINDER_CHANNEL_DESC
import com.example.mediminder.utils.Constants.MED_REMINDER_CHANNEL_ID
import com.example.mediminder.utils.Constants.MED_REMINDER_CHANNEL_NAME
import com.example.mediminder.utils.Constants.SKIP_MEDICATION
import com.example.mediminder.utils.Constants.TAKE_MEDICATION

// https://developer.android.com/develop/ui/views/notifications
// https://developer.android.com/develop/ui/views/notifications/build-notification
// Broadcast receiver for medication reminders
class MedicationReminderReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra(LOG_ID, -1L)
        val medicationName = intent.getStringExtra(MED_NAME) ?: MED_NAME_DEFAULT
        val dosage = intent.getStringExtra(DOSAGE) ?: EMPTY_STRING

        createNotificationChannel(context)

        // Open main activity
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Notifications must be wrapped in pending intents
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // User action: Take medication
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

        // User action: Skip medication
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


        // Build notification
        val contentText = getMessage(context, medicationName, dosage)
        val notification = NotificationCompat.Builder(context, MED_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_small)
            .setContentTitle(context.getString(R.string.medication_reminder))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.taken, context.getString(R.string.take), takePendingIntent)
            .addAction(R.drawable.skip, context.getString(R.string.skip), skipPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(logId.toInt(), notification)
    }

    // Get notification message based on user settings (show or hide medication name and dosage)
    private fun getMessage(context: Context, name: String, dosage: String): String {
        val settingsPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val showName = settingsPrefs.getBoolean(MED_PRIVACY, true)
        val showDosage = settingsPrefs.getBoolean(DOSAGE_PRIVACY, true)

        return when {
            showName && showDosage -> context.getString(R.string.notification_show_med_and_dose, name, dosage)
            showName && !showDosage -> context.getString(R.string.notification_show_med_hide_dose, name)
            !showName && showDosage -> context.getString(R.string.notification_hide_med_show_dose, dosage)
            else -> context.getString(R.string.notification_hide_med_and_dose)
        }
    }

    // Create notification channel
    private fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH // high importance allows peeking
        val channel = NotificationChannel(MED_REMINDER_CHANNEL_ID, MED_REMINDER_CHANNEL_NAME, importance).apply {
            description = MED_REMINDER_CHANNEL_DESC
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val SKIP_OFFSET = 1000
    }
}