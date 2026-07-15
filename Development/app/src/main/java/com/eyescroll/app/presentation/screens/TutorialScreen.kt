package com.eyescroll.app.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eyescroll.app.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(onContinue: () -> Unit) {
    val pages = listOf(
        R.string.gesture_double_wink_right,
        R.string.gesture_double_wink_left,
        R.string.gesture_double_both,
        R.string.gesture_nose,
        R.string.gesture_vol_right,
        R.string.gesture_vol_left,
        R.string.gesture_brow,
        R.string.gesture_mouth,
        R.string.gesture_tilt,
        R.string.gesture_sleep
    )
    val pager = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(stringResource(R.string.tutorial_title), style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        HorizontalPager(
            state = pager,
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.weight(1f)
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${page + 1} / ${pages.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(pages[page]),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(stringResource(R.string.btn_continue))
        }
    }
}
