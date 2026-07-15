package com.eyescroll.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eyescroll.app.data.SettingsRepository
import com.eyescroll.app.data.UserSettings
import com.eyescroll.app.domain.model.GestureToggles
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val settings: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<UserSettings> = settings.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserSettings(
            onboardingDone = false,
            serviceDesired = false,
            detectionPaused = false,
            noseBaseline = 1f,
            sensitivity = "normal",
            toggles = GestureToggles()
        )
    )

    fun completeOnboarding() = viewModelScope.launch {
        settings.setOnboardingDone(true)
        settings.setServiceDesired(true)
    }

    fun setServiceDesired(desired: Boolean) = viewModelScope.launch {
        settings.setServiceDesired(desired)
    }

    fun setPaused(paused: Boolean) = viewModelScope.launch {
        settings.setDetectionPaused(paused)
    }

    fun setSensitivity(value: String) = viewModelScope.launch {
        settings.setSensitivity(value)
    }

    fun setToggles(toggles: GestureToggles) = viewModelScope.launch {
        settings.setToggles(toggles)
    }

    fun setNoseBaseline(v: Float) = viewModelScope.launch {
        settings.setNoseBaseline(v)
    }
}
