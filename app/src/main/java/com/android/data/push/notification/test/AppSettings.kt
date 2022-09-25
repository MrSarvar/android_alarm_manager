package com.android.data.push.notification.test

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppSettings(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("app.settings", Context.MODE_PRIVATE)

    private val _flowFirebaseToken = MutableStateFlow<String?>(null)
    val flowFirebaseToken = _flowFirebaseToken.asStateFlow()

    var useAlarmManager: Boolean
        get() = prefs.getBoolean(Keys.USE_ALARM_MANAGER, false)
        set(value) = prefs.edit { putBoolean(Keys.USE_ALARM_MANAGER, value) }

    init {
        _flowFirebaseToken.value = prefs.getString(Keys.FIREBASE_TOKEN, null)
    }

    fun setFirebaseToken(token: String?) {
        prefs.edit {
            if (token != null) {
                putString(Keys.FIREBASE_TOKEN, token)
            } else {
                remove(Keys.FIREBASE_TOKEN)
            }
            _flowFirebaseToken.value = token
        }
    }

    private object Keys {
        const val FIREBASE_TOKEN = "firebase_token"
        const val USE_ALARM_MANAGER = "use_alarm_manager"
    }
}