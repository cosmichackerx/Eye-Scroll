package com.eyescroll.app.presentation.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object OemSettings {
    fun isXiaomiFamily(): Boolean {
        val m = Build.MANUFACTURER.lowercase()
        val b = Build.BRAND.lowercase()
        return listOf("xiaomi", "redmi", "poco", "blackshark").any { m.contains(it) || b.contains(it) }
    }

    fun openAutostart(context: Context) {
        val candidates = listOf(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            ),
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
        )
        for (cn in candidates) {
            val i = Intent().setComponent(cn)
            if (i.resolveActivity(context.packageManager) != null) {
                runCatching { context.startActivity(i) }
                return
            }
        }
        val fallback = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(fallback)
    }
}
