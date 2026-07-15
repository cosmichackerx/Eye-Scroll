package com.eyescroll.app.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eyescroll.app.domain.model.GestureThresholds
import com.eyescroll.app.domain.model.GestureToggles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("eye_scroll_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val onboardingDone = booleanPreferencesKey("onboarding_done")
        val serviceDesired = booleanPreferencesKey("service_desired")
        val detectionPaused = booleanPreferencesKey("detection_paused")
        val noseBaseline = floatPreferencesKey("nose_baseline")
        val sensitivity = stringPreferencesKey("sensitivity")

        val twRight = booleanPreferencesKey("g_wink_right")
        val twLeft = booleanPreferencesKey("g_wink_left")
        val twBoth = booleanPreferencesKey("g_blink_both")
        val tNose = booleanPreferencesKey("g_nose")
        val tVol = booleanPreferencesKey("g_vol")
        val tBrow = booleanPreferencesKey("g_brow")
        val tMouth = booleanPreferencesKey("g_mouth")
        val tTilt = booleanPreferencesKey("g_tilt")
        val tSleep = booleanPreferencesKey("g_sleep")
    }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.onboardingDone] ?: false }

    val serviceDesired: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.serviceDesired] ?: false }

    val detectionPaused: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.detectionPaused] ?: false }

    val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        UserSettings(
            onboardingDone = p[Keys.onboardingDone] ?: false,
            serviceDesired = p[Keys.serviceDesired] ?: false,
            detectionPaused = p[Keys.detectionPaused] ?: false,
            noseBaseline = p[Keys.noseBaseline] ?: 1f,
            sensitivity = p[Keys.sensitivity] ?: "normal",
            toggles = GestureToggles(
                doubleWinkRight = p[Keys.twRight] ?: true,
                doubleWinkLeft = p[Keys.twLeft] ?: true,
                doubleBlinkBoth = p[Keys.twBoth] ?: true,
                noseExpand = p[Keys.tNose] ?: true,
                volume = p[Keys.tVol] ?: true,
                browRaise = p[Keys.tBrow] ?: true,
                mouthOpen = p[Keys.tMouth] ?: true,
                headTilt = p[Keys.tTilt] ?: true,
                bothEyesSleep = p[Keys.tSleep] ?: false
            )
        )
    }

    suspend fun setOnboardingDone(done: Boolean = true) {
        context.dataStore.edit { it[Keys.onboardingDone] = done }
    }

    suspend fun setServiceDesired(desired: Boolean) {
        context.dataStore.edit { it[Keys.serviceDesired] = desired }
    }

    suspend fun setDetectionPaused(paused: Boolean) {
        context.dataStore.edit { it[Keys.detectionPaused] = paused }
    }

    suspend fun setNoseBaseline(value: Float) {
        context.dataStore.edit { it[Keys.noseBaseline] = value }
    }

    suspend fun setSensitivity(value: String) {
        context.dataStore.edit { it[Keys.sensitivity] = value }
    }

    suspend fun setToggles(toggles: GestureToggles) {
        context.dataStore.edit { p ->
            p.writeToggles(toggles)
        }
    }

    fun thresholdsFor(sensitivity: String): GestureThresholds {
        return when (sensitivity) {
            "easy" -> GestureThresholds(
                blinkClosed = 0.45f,
                blinkOpen = 0.3f,
                cooldownMs = 450L,
                noseExpandDelta = 0.06f
            )
            "precise" -> GestureThresholds(
                blinkClosed = 0.62f,
                blinkOpen = 0.2f,
                cooldownMs = 700L,
                noseExpandDelta = 0.11f,
                doubleWinkWindowMs = 650L
            )
            else -> GestureThresholds()
        }
    }

    private fun MutablePreferences.writeToggles(t: GestureToggles) {
        this[Keys.twRight] = t.doubleWinkRight
        this[Keys.twLeft] = t.doubleWinkLeft
        this[Keys.twBoth] = t.doubleBlinkBoth
        this[Keys.tNose] = t.noseExpand
        this[Keys.tVol] = t.volume
        this[Keys.tBrow] = t.browRaise
        this[Keys.tMouth] = t.mouthOpen
        this[Keys.tTilt] = t.headTilt
        this[Keys.tSleep] = t.bothEyesSleep
    }
}

data class UserSettings(
    val onboardingDone: Boolean,
    val serviceDesired: Boolean,
    val detectionPaused: Boolean,
    val noseBaseline: Float,
    val sensitivity: String,
    val toggles: GestureToggles
)
