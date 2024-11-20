package com.example.mediminder

import android.app.AlarmManager
import android.content.Context
import android.os.Build

// Helper class to check if exact alarm permission has been granted
class AlarmPermissionManager(private val context: Context) {
    fun hasAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        else { return true }
    }
}