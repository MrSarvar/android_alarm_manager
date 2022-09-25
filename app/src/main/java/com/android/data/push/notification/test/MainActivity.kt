package com.android.data.push.notification.test

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.data.push.notification.test.databinding.ActivityMainBinding
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        this::onPermissionResult
    )

    private val appSettings by lazy { AppSettings(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.checkboxUseAlarmManager.isChecked = appSettings.useAlarmManager
        binding.checkboxUseAlarmManager.setOnCheckedChangeListener { _, isChecked ->
            appSettings.useAlarmManager = isChecked
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appSettings.flowFirebaseToken.collect {
                    if (it != null) {
                        binding.tvFirebaseToken.text = it
                        binding.btnShareToken.visibility = if (!it.isNullOrBlank()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    } else {
                        getFirebaseToken()
                    }
                }
            }
        }

        binding.btnShareToken.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, appSettings.flowFirebaseToken.value)
                type = "text/plain"
            }
            val intentChooser = Intent.createChooser(intent, null)
            startActivity(intentChooser)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!appSettings.useAlarmManager) {
            checkAndRequestPermissions()
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setMessage("Please allow the display pop-up windows permission")
                .setPositiveButton("Allow") { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                }
                .setNegativeButton("Exit") { _, _ -> finish() }
                .show()
            return
        }

        if (Build.VERSION.SDK_INT >= 33 && !hasPermission("android.permission.POST_NOTIFICATIONS")) {
            requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
        }
    }

    private fun onPermissionResult(isGranted: Boolean) {
        if (!isGranted) {
            AlertDialog.Builder(this)
                .setMessage("You have not granted permissions")
                .setPositiveButton("Allow") { _, _ -> checkAndRequestPermissions() }
                .setNegativeButton("Exit") { _, _ -> finish() }
                .show()
        } else {
            checkAndRequestPermissions()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { appSettings.setFirebaseToken(it) }
            .addOnFailureListener { Toast.makeText(this, it.message, Toast.LENGTH_LONG).show() }
    }
}