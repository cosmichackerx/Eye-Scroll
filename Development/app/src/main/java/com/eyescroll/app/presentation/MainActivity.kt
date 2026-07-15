package com.eyescroll.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.eyescroll.app.presentation.nav.EyeScrollNav
import com.eyescroll.app.presentation.theme.EyeScrollTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EyeScrollTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EyeScrollNav()
                }
            }
        }
    }
}
