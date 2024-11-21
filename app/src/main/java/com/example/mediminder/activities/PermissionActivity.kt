package com.example.mediminder.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mediminder.AlarmPermissionManager
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityPermissionBinding
import com.example.mediminder.utils.AppUtils.setupWindowInsets

class PermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPermissionBinding
    private lateinit var permissionManager: AlarmPermissionManager

    // Handle the result of the notification permission request
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkNotificationPermission()) { showAlarmPermissionUI() }
        else { handlePermissionDenied(NOTIFICATION) }
    }

    // Handle the result of the alarm permission request
    private val alarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            || permissionManager.hasAlarmPermission()) { navigateToMainActivity() }
        else { handlePermissionDenied(ALARM) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsets(binding.root)
        permissionManager = AlarmPermissionManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkNotificationPermission()) { checkAlarmPermissionAndNavigate() }
            else { showNotificationPermissionUI() }
        }
        else { checkAlarmPermissionAndNavigate() }
    }

    // Check for notification permission
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
        else true
    }

    // Check for alarm permission and navigate to the main activity if granted
    private fun checkAlarmPermissionAndNavigate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S
            || permissionManager.hasAlarmPermission()) { navigateToMainActivity() }
        else { handlePermissionDenied(ALARM) }
    }

    // Show the UI for requesting notification permissions
    private fun showNotificationPermissionUI() {
        with (binding) {
            permissionIcon.setImageResource(R.drawable.notification)
            permissionTitle.text = getString(R.string.enable_notifications)
            permissionDescription.text = getString(R.string.msg_enable_notifications)
            permissionButton.text = getString(R.string.enable_notifications)
            permissionButton.setOnClickListener { handleNotificationPermissionClick() }
        }
    }

    // Handle the click event for the notification permission button
    private fun handleNotificationPermissionClick() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        notificationPermissionLauncher.launch(intent)
    }

    // Show the UI for requesting alarm permissions
    private fun showAlarmPermissionUI() {
        with (binding) {
            permissionIcon.setImageResource(R.drawable.alarm)
            permissionTitle.text = getString(R.string.enable_alarms)
            permissionDescription.text = getString(R.string.msg_enable_alarms)
            permissionButton.text = getString(R.string.enable_alarms)
            permissionButton.setOnClickListener { handleAlarmPermissionClick() }
        }
    }

    // Handle the click event for the alarm permission button
    private fun handleAlarmPermissionClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            alarmPermissionLauncher.launch(intent)
        }
    }

    // Handle the case where the permission is denied
    private fun handlePermissionDenied(permissionType: String) {
        val message = when (permissionType) {
            NOTIFICATION -> getString(R.string.notification_required)
            ALARM -> getString(R.string.alarm_required)
            else -> getString(R.string.permission_required)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Navigate to the main activity
    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        private const val NOTIFICATION = "notification"
        private const val ALARM = "alarm"
    }
}