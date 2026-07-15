package com.eyescroll.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.eyescroll.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class EyeScrollApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startKoin {
            androidContext(this@EyeScrollApp)
            modules(appModule)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "eye_control"
    }
}
