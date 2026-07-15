package com.eyescroll.app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eyescroll.app.R

@Composable
fun CalibrationScreen(
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onBaseline: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stringResource(R.string.calib_title), style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.calib_body), style = MaterialTheme.typography.bodyLarge)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    onBaseline(1f)
                    onDone()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.btn_continue)) }
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.btn_skip))
            }
        }
    }
}
