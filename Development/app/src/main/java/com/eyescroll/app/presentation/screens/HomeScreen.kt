package com.eyescroll.app.presentation.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eyescroll.app.R
import com.eyescroll.app.presentation.HomeViewModel
import com.eyescroll.app.presentation.util.OemSettings
import com.eyescroll.app.service.CommandBus
import com.eyescroll.app.service.EyeControlForegroundService

@Composable
fun HomeScreen(vm: HomeViewModel) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val a11yOk = CommandBus.accessibilityConnected

    LaunchedEffect(state.serviceDesired) {
        if (state.serviceDesired) {
            EyeControlForegroundService.start(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.displayLarge)

        val status = when {
            !a11yOk -> stringResource(R.string.home_status_need_a11y)
            !state.serviceDesired -> stringResource(R.string.home_status_stopped)
            state.detectionPaused -> stringResource(R.string.home_status_paused)
            else -> stringResource(R.string.home_status_running)
        }
        Text(status, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)

        if (!a11yOk) {
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_open_accessibility)) }
        }

        if (state.serviceDesired) {
            Button(
                onClick = {
                    vm.setServiceDesired(false)
                    EyeControlForegroundService.stop(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_stop_service)) }
            OutlinedButton(
                onClick = { vm.setPaused(!state.detectionPaused) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.detectionPaused) stringResource(R.string.action_resume)
                    else stringResource(R.string.action_pause)
                )
            }
        } else {
            Button(
                onClick = {
                    vm.setServiceDesired(true)
                    EyeControlForegroundService.start(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_start_service)) }
        }

        Text(stringResource(R.string.settings_sensitivity), style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("easy", "normal", "precise").forEach { level ->
                FilterChip(
                    selected = state.sensitivity == level,
                    onClick = { vm.setSensitivity(level) },
                    label = { Text(level) }
                )
            }
        }

        Text(stringResource(R.string.settings_gestures), style = MaterialTheme.typography.titleLarge)
        GestureSwitch("Double wink right", state.toggles.doubleWinkRight) {
            vm.setToggles(state.toggles.copy(doubleWinkRight = it))
        }
        GestureSwitch("Double wink left", state.toggles.doubleWinkLeft) {
            vm.setToggles(state.toggles.copy(doubleWinkLeft = it))
        }
        GestureSwitch("Double blink both → Home", state.toggles.doubleBlinkBoth) {
            vm.setToggles(state.toggles.copy(doubleBlinkBoth = it))
        }
        GestureSwitch("Nose expand → tap", state.toggles.noseExpand) {
            vm.setToggles(state.toggles.copy(noseExpand = it))
        }
        GestureSwitch("Volume sequence", state.toggles.volume) {
            vm.setToggles(state.toggles.copy(volume = it))
        }
        GestureSwitch("Brow raise → like", state.toggles.browRaise) {
            vm.setToggles(state.toggles.copy(browRaise = it))
        }
        GestureSwitch("Mouth open → Recents", state.toggles.mouthOpen) {
            vm.setToggles(state.toggles.copy(mouthOpen = it))
        }
        GestureSwitch("Head tilt seek", state.toggles.headTilt) {
            vm.setToggles(state.toggles.copy(headTilt = it))
        }
        GestureSwitch("Long both-eyes → lock", state.toggles.bothEyesSleep) {
            vm.setToggles(state.toggles.copy(bothEyesSleep = it))
        }

        if (OemSettings.isXiaomiFamily()) {
            Text(stringResource(R.string.miui_checklist_title), style = MaterialTheme.typography.titleLarge)
            OutlinedButton(
                onClick = { OemSettings.openAutostart(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_autostart)) }
        }

        Text(stringResource(R.string.privacy_note), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GestureSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
