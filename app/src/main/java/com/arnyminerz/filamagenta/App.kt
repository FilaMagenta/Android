package com.arnyminerz.filamagenta

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.arnyminerz.filamagenta.data.ANALYTICS_COLLECTION
import com.arnyminerz.filamagenta.data.ERROR_COLLECTION
import com.arnyminerz.filamagenta.firebase.ALERTS_NOTIFICATION_CHANNEL
import com.arnyminerz.filamagenta.utils.doAsync
import com.arnyminerz.filamagenta.utils.getBooleanPreferences
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import kotlinx.coroutines.Job
import timber.log.Timber

class App : Application() {
    private var errorCollectionJob: Job? = null
    private var analyticsCollectionJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        else
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    Firebase.crashlytics.log(message)
                    t?.let { Firebase.crashlytics.recordException(it) }
                }
            })

        errorCollectionJob = doAsync {
            getBooleanPreferences(ERROR_COLLECTION, true).collect { enabled ->
                Firebase.crashlytics.setCrashlyticsCollectionEnabled(enabled)
            }
        }
        analyticsCollectionJob = doAsync {
            getBooleanPreferences(ANALYTICS_COLLECTION, true).collect { enabled ->
                Firebase.performance.isPerformanceCollectionEnabled = enabled
                Firebase.analytics.setAnalyticsCollectionEnabled(enabled)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        errorCollectionJob?.cancel()
        analyticsCollectionJob?.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val name = getString(R.string.notification_channel_alerts_name)
        val desc = getString(R.string.notification_channel_alerts_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(ALERTS_NOTIFICATION_CHANNEL, name, importance).apply {
                description = desc
            }
        )
    }
}
