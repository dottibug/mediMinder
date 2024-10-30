package com.example.mediminder

import android.app.AlarmManager
import android.content.Context
import android.os.Build

class AlarmPermissionManager(private val context: Context) {
    // Check if the user has granted exact alarm permission
    fun hasAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}