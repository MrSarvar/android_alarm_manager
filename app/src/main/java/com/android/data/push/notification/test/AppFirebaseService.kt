package com.android.data.push.notification.test

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseService : FirebaseMessagingService() {

    private val alarmManager by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }
    private val appSettings by lazy { AppSettings(this) }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        appSettings.setFirebaseToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        try {
            if (appSettings.useAlarmManager) {
                val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    100,
                    Intent(this, AlarmActivity::class.java),
                    flag
                )

                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(System.currentTimeMillis() + 1000, pendingIntent),
                    pendingIntent
                )
                Log.e("AlarmApp", "Alarm Set")
            } else {
                startActivity(
                    Intent(this, AlarmActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                Log.e("AlarmApp", "Activity Launched")
            }
        } catch (e: Exception) {
            Log.e("AlarmApp", e.message.toString())
            e.printStackTrace()
        }
    }
}