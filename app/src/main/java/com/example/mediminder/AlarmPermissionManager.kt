package com.example.mediminder

import android.app.AlarmManager
import android.content.Context
import android.os.Build

/**
 * This class is used to check if the user has granted the exact alarm permission.
 */
class AlarmPermissionManager(private val context: Context) {
    fun hasAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        else { return true }
    }
}