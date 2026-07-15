package com.eyescroll.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.eyescroll.app.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {
    private val settings: SettingsRepository by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val desired = runBlocking { settings.serviceDesired.first() }
        if (desired) {
            EyeControlForegroundService.start(context.applicationContext)
        }
    }
}
