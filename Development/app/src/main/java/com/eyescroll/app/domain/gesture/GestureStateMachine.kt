package com.eyescroll.app.domain.gesture

import com.eyescroll.app.domain.model.FaceSignals
import com.eyescroll.app.domain.model.GestureCommand
import com.eyescroll.app.domain.model.GestureThresholds
import com.eyescroll.app.domain.model.GestureToggles
import com.eyescroll.app.domain.model.GestureType

/**
 * Pure Kotlin gesture recognizer. Thread-safe only if called from one worker thread.
 */
class GestureStateMachine(
    private var thresholds: GestureThresholds = GestureThresholds(),
    private var toggles: GestureToggles = GestureToggles(),
    private var noseBaseline: Float = 1f
) {
    private var lastLeftClosed = false
    private var lastRightClosed = false
    private var leftWinkTimes = ArrayDeque<Long>()
    private var rightWinkTimes = ArrayDeque<Long>()
    private var bothBlinkTimes = ArrayDeque<Long>()

    private var bothClosedSince: Long? = null
    private var mouthOpenSince: Long? = null
    private var sleepClosedSince: Long? = null
    private var lastFireAt = mutableMapOf<GestureType, Long>()
    private var volumeArmed = false

    fun updateConfig(
        thresholds: GestureThresholds = this.thresholds,
        toggles: GestureToggles = this.toggles,
        noseBaseline: Float = this.noseBaseline
    ) {
        this.thresholds = thresholds
        this.toggles = toggles
        this.noseBaseline = noseBaseline.coerceAtLeast(0.01f)
    }

    fun process(signals: FaceSignals): GestureCommand? {
        if (!signals.facePresent) {
            resetTransient()
            return null
        }

        val t = signals.timestampMs
        val leftClosed = signals.eyeBlinkLeft >= thresholds.blinkClosed
        val rightClosed = signals.eyeBlinkRight >= thresholds.blinkClosed
        val leftOpen = signals.eyeBlinkLeft <= thresholds.blinkOpen
        val rightOpen = signals.eyeBlinkRight <= thresholds.blinkOpen
        val bothClosed = leftClosed && rightClosed

        // Rising edge: open -> closed counts as wink/blink pulse end when releasing
        if (lastLeftClosed && leftOpen && rightOpen) {
            push(leftWinkTimes, t)
        }
        if (lastRightClosed && rightOpen && leftOpen) {
            push(rightWinkTimes, t)
        }
        if (lastLeftClosed && lastRightClosed && leftOpen && rightOpen) {
            push(bothBlinkTimes, t)
        }

        // Volume arming: both closed long enough
        if (bothClosed) {
            if (bothClosedSince == null) bothClosedSince = t
            if ((t - (bothClosedSince ?: t)) >= thresholds.volumeBothClosedMs) {
                volumeArmed = true
            }
            if (sleepClosedSince == null) sleepClosedSince = t
        } else {
            bothClosedSince = null
            // Volume: both were closed, now asymmetric open
            if (volumeArmed && toggles.volume) {
                if (rightOpen && leftClosed) {
                    volumeArmed = false
                    fire(GestureType.VOLUME_DOWN, t)?.let {
                        lastLeftClosed = leftClosed
                        lastRightClosed = rightClosed
                        return it
                    }
                } else if (leftOpen && rightClosed) {
                    volumeArmed = false
                    fire(GestureType.VOLUME_UP, t)?.let {
                        lastLeftClosed = leftClosed
                        lastRightClosed = rightClosed
                        return it
                    }
                } else if (leftOpen && rightOpen) {
                    volumeArmed = false
                }
            }
            sleepClosedSince = null
        }

        lastLeftClosed = leftClosed
        lastRightClosed = rightClosed

        prune(leftWinkTimes, t)
        prune(rightWinkTimes, t)
        prune(bothBlinkTimes, t)

        if (toggles.doubleWinkRight && rightWinkTimes.size >= 2 && leftWinkTimes.size < 2) {
            fire(GestureType.DOUBLE_WINK_RIGHT, t)?.let {
                rightWinkTimes.clear()
                return it
            }
        }
        if (toggles.doubleWinkLeft && leftWinkTimes.size >= 2 && rightWinkTimes.size < 2) {
            fire(GestureType.DOUBLE_WINK_LEFT, t)?.let {
                leftWinkTimes.clear()
                return it
            }
        }
        if (toggles.doubleBlinkBoth && bothBlinkTimes.size >= 2) {
            fire(GestureType.DOUBLE_BLINK_BOTH, t)?.let {
                bothBlinkTimes.clear()
                leftWinkTimes.clear()
                rightWinkTimes.clear()
                return it
            }
        }

        if (toggles.noseExpand) {
            val delta = (signals.noseWidthRatio / noseBaseline) - 1f
            val sneer = (signals.noseSneerLeft + signals.noseSneerRight) / 2f
            if (delta >= thresholds.noseExpandDelta || sneer >= 0.4f) {
                fire(GestureType.NOSE_EXPAND, t)?.let { return it }
            }
        }

        if (toggles.browRaise && signals.browInnerUp >= thresholds.browRaise && leftOpen && rightOpen) {
            fire(GestureType.BROW_RAISE, t)?.let { return it }
        }

        if (toggles.mouthOpen) {
            if (signals.jawOpen >= thresholds.mouthOpen) {
                if (mouthOpenSince == null) mouthOpenSince = t
                if ((t - (mouthOpenSince ?: t)) >= thresholds.mouthHoldMs) {
                    mouthOpenSince = null
                    fire(GestureType.MOUTH_OPEN, t)?.let { return it }
                }
            } else {
                mouthOpenSince = null
            }
        }

        if (toggles.headTilt) {
            when {
                signals.headYaw <= -thresholds.headTiltAbs ->
                    fire(GestureType.HEAD_TILT_LEFT, t)?.let { return it }
                signals.headYaw >= thresholds.headTiltAbs ->
                    fire(GestureType.HEAD_TILT_RIGHT, t)?.let { return it }
            }
        }

        if (toggles.bothEyesSleep && bothClosed) {
            val since = sleepClosedSince
            if (since != null && (t - since) >= thresholds.sleepHoldMs) {
                sleepClosedSince = t + thresholds.cooldownMs
                fire(GestureType.BOTH_EYES_SLEEP, t)?.let { return it }
            }
        }

        return null
    }

    private fun fire(type: GestureType, t: Long): GestureCommand? {
        val last = lastFireAt[type] ?: 0L
        if (t - last < thresholds.cooldownMs) return null
        // Global short cooldown to avoid stacked commands
        val anyLast = lastFireAt.values.maxOrNull() ?: 0L
        if (t - anyLast < thresholds.cooldownMs / 2) return null
        lastFireAt[type] = t
        return GestureCommand(type, t)
    }

    private fun push(q: ArrayDeque<Long>, t: Long) {
        q.addLast(t)
        prune(q, t)
    }

    private fun prune(q: ArrayDeque<Long>, t: Long) {
        while (q.isNotEmpty() && t - q.first() > thresholds.doubleWinkWindowMs) {
            q.removeFirst()
        }
    }

    private fun resetTransient() {
        bothClosedSince = null
        mouthOpenSince = null
        sleepClosedSince = null
        volumeArmed = false
        lastLeftClosed = false
        lastRightClosed = false
    }
}
