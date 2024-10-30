package com.example.mediminder.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mediminder.AlarmPermissionManager
import com.example.mediminder.MainActivity
import com.example.mediminder.databinding.ActivityPermissionBinding
import com.example.mediminder.utils.WindowInsetsUtil

class PermissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding
    private lateinit var permissionManager: AlarmPermissionManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        checkPermissionAndNavigate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsUtil.setupWindowInsets(binding.root)

        permissionManager = AlarmPermissionManager(this)

        binding.permissionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                permissionLauncher.launch(intent)
            }
        }
    }

    private fun checkPermissionAndNavigate() {
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.S || permissionManager.hasAlarmPermission()) {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}