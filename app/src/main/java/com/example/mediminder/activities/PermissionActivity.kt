package com.example.mediminder.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mediminder.AlarmPermissionManager
import com.example.mediminder.MainActivity
import com.example.mediminder.R
import com.example.mediminder.databinding.ActivityPermissionBinding
import com.example.mediminder.receivers.MedicationReminderReceiver
import com.example.mediminder.utils.WindowInsetsUtil

class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding
    private lateinit var permissionManager: AlarmPermissionManager

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (checkNotificationPermission()) { showAlarmPermissionUI() }
    }

    private val alarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        checkAlarmPermissionAndNavigate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        permissionManager = AlarmPermissionManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkNotificationPermission()) { checkAlarmPermissionAndNavigate() }
            else { showNotificationPermissionUI() }
        }
        else { checkAlarmPermissionAndNavigate() }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else { true }
    }

    private fun showNotificationPermissionUI() {
        binding.permissionIcon.setImageResource(R.drawable.notification)
        binding.permissionTitle.text = getString(R.string.notification_permission_title)
        binding.permissionDescription.text = getString(R.string.notification_permission_description)
        binding.permissionButton.text = getString(R.string.button_grant_notification_permission)

        binding.permissionButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, MedicationReminderReceiver.CHANNEL_ID)
            }
            notificationPermissionLauncher.launch(intent)
        }
    }

    private fun showAlarmPermissionUI() {
        binding.permissionIcon.setImageResource(R.drawable.alarm)
        binding.permissionTitle.text = getString(R.string.alarm_permission_title)
        binding.permissionDescription.text = getString(R.string.alarm_permission_description)
        binding.permissionButton.text = getString(R.string.button_grant_alarm_permission)

        binding.permissionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                alarmPermissionLauncher.launch(intent)
            }
        }
    }

    private fun checkAlarmPermissionAndNavigate() {
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.S || permissionManager.hasAlarmPermission()) {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}