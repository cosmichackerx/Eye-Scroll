package com.eyescroll.app.domain.model

data class FaceSignals(
    val eyeBlinkLeft: Float,
    val eyeBlinkRight: Float,
    val browInnerUp: Float,
    val jawOpen: Float,
    val noseSneerLeft: Float,
    val noseSneerRight: Float,
    val noseWidthRatio: Float,
    val headYaw: Float,
    val headPitch: Float,
    val facePresent: Boolean,
    val timestampMs: Long
)

enum class GestureType {
    DOUBLE_WINK_RIGHT,
    DOUBLE_WINK_LEFT,
    DOUBLE_BLINK_BOTH,
    NOSE_EXPAND,
    VOLUME_UP,
    VOLUME_DOWN,
    BROW_RAISE,
    MOUTH_OPEN,
    HEAD_TILT_LEFT,
    HEAD_TILT_RIGHT,
    BOTH_EYES_SLEEP
}

data class GestureCommand(
    val type: GestureType,
    val timestampMs: Long
)

data class GestureThresholds(
    val blinkClosed: Float = 0.55f,
    val blinkOpen: Float = 0.25f,
    val doubleWinkWindowMs: Long = 750L,
    val cooldownMs: Long = 550L,
    val noseExpandDelta: Float = 0.08f,
    val browRaise: Float = 0.45f,
    val mouthOpen: Float = 0.45f,
    val mouthHoldMs: Long = 600L,
    val headTiltAbs: Float = 0.28f,
    val sleepHoldMs: Long = 2500L,
    val volumeBothClosedMs: Long = 280L
)

data class GestureToggles(
    val doubleWinkRight: Boolean = true,
    val doubleWinkLeft: Boolean = true,
    val doubleBlinkBoth: Boolean = true,
    val noseExpand: Boolean = true,
    val volume: Boolean = true,
    val browRaise: Boolean = true,
    val mouthOpen: Boolean = true,
    val headTilt: Boolean = true,
    val bothEyesSleep: Boolean = false
)
