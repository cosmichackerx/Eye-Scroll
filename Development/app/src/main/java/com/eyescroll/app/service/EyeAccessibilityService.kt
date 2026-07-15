package com.eyescroll.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.eyescroll.app.domain.model.GestureCommand
import com.eyescroll.app.domain.model.GestureType

class EyeAccessibilityService : AccessibilityService() {

    private val listener: (GestureCommand) -> Unit = { cmd -> dispatch(cmd) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        CommandBus.accessibilityConnected = true
        CommandBus.addListener(listener)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        CommandBus.removeListener(listener)
        CommandBus.accessibilityConnected = false
        instance = null
        super.onDestroy()
    }

    private fun dispatch(command: GestureCommand) {
        when (command.type) {
            GestureType.DOUBLE_WINK_RIGHT -> swipeVertical(up = true)
            GestureType.DOUBLE_WINK_LEFT -> swipeVertical(up = false)
            GestureType.DOUBLE_BLINK_BOTH -> performGlobalAction(GLOBAL_ACTION_HOME)
            GestureType.NOSE_EXPAND -> tapCenter()
            GestureType.VOLUME_UP -> {
                // Volume handled in foreground service via AudioManager; noop here
            }
            GestureType.VOLUME_DOWN -> Unit
            GestureType.BROW_RAISE -> doubleTapCenter()
            GestureType.MOUTH_OPEN -> performGlobalAction(GLOBAL_ACTION_RECENTS)
            GestureType.HEAD_TILT_LEFT -> swipeHorizontal(left = true)
            GestureType.HEAD_TILT_RIGHT -> swipeHorizontal(left = false)
            GestureType.BOTH_EYES_SLEEP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
            }
        }
    }

    private fun swipeVertical(up: Boolean) {
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val startY = if (up) dm.heightPixels * 0.72f else dm.heightPixels * 0.28f
        val endY = if (up) dm.heightPixels * 0.28f else dm.heightPixels * 0.72f
        stroke(x, startY, x, endY, durationMs = 280L)
    }

    private fun swipeHorizontal(left: Boolean) {
        val dm = resources.displayMetrics
        val y = dm.heightPixels / 2f
        val startX = if (left) dm.widthPixels * 0.75f else dm.widthPixels * 0.25f
        val endX = if (left) dm.widthPixels * 0.25f else dm.widthPixels * 0.75f
        stroke(startX, y, endX, y, durationMs = 260L)
    }

    private fun tapCenter() {
        val dm = resources.displayMetrics
        val x = dm.widthPixels / 2f
        val y = dm.heightPixels / 2f
        stroke(x, y, x, y, durationMs = 50L)
    }

    private fun doubleTapCenter() {
        tapCenter()
        mainExecutor.execute {
            android.os.Handler(mainLooper).postDelayed({ tapCenter() }, 90L)
        }
    }

    private fun stroke(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Long) {
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    companion object {
        @Volatile
        var instance: EyeAccessibilityService? = null
    }
}
