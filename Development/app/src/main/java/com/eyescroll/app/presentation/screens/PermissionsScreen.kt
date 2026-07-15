package com.eyescroll.app.presentation.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eyescroll.app.R
import com.eyescroll.app.presentation.util.OemSettings

@Composable
fun PermissionsScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.permissions_title), style = MaterialTheme.typography.displayLarge)
        Text(stringResource(R.string.permissions_body), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { cameraLauncher.launch(Manifest.permission.CAMERA) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Allow camera") }

        if (Build.VERSION.SDK_INT >= 33) {
            Button(
                onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Allow notifications") }
        }

        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.btn_open_accessibility)) }

        OutlinedButton(
            onClick = {
                val i = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(i)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.btn_battery)) }

        if (OemSettings.isXiaomiFamily()) {
            OutlinedButton(
                onClick = { OemSettings.openAutostart(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_autostart)) }
        }

        Text(stringResource(R.string.privacy_note), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.btn_continue))
        }
    }
}
