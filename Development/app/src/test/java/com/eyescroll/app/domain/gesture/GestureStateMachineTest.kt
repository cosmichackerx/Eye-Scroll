package com.eyescroll.app.domain.gesture

import com.eyescroll.app.domain.model.FaceSignals
import com.eyescroll.app.domain.model.GestureToggles
import com.eyescroll.app.domain.model.GestureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GestureStateMachineTest {

    private fun signals(
        t: Long,
        left: Float,
        right: Float
    ) = FaceSignals(
        eyeBlinkLeft = left,
        eyeBlinkRight = right,
        browInnerUp = 0f,
        jawOpen = 0f,
        noseSneerLeft = 0f,
        noseSneerRight = 0f,
        noseWidthRatio = 1f,
        headYaw = 0f,
        headPitch = 0f,
        facePresent = true,
        timestampMs = t
    )

    private fun machineCoreOnly(): GestureStateMachine {
        val m = GestureStateMachine()
        m.updateConfig(
            toggles = GestureToggles(
                doubleWinkRight = true,
                doubleWinkLeft = true,
                doubleBlinkBoth = true,
                noseExpand = false,
                volume = false,
                browRaise = false,
                mouthOpen = false,
                headTilt = false,
                bothEyesSleep = false
            )
        )
        return m
    }

    @Test
    fun doubleWinkRight_firesNext() {
        val m = machineCoreOnly()
        // eyes open
        assertNull(m.process(signals(0, 0.05f, 0.05f)))
        // wink 1: right closes (left stays open), then opens
        assertNull(m.process(signals(100, 0.05f, 0.80f)))
        assertNull(m.process(signals(150, 0.05f, 0.80f)))
        assertNull(m.process(signals(200, 0.05f, 0.05f)))
        // wink 2
        assertNull(m.process(signals(350, 0.05f, 0.80f)))
        assertNull(m.process(signals(400, 0.05f, 0.80f)))
        val cmd = m.process(signals(450, 0.05f, 0.05f))
        assertNotNull("expected DOUBLE_WINK_RIGHT, got null", cmd)
        assertEquals(GestureType.DOUBLE_WINK_RIGHT, cmd!!.type)
    }

    @Test
    fun doubleBlinkBoth_firesHome() {
        val m = machineCoreOnly()
        assertNull(m.process(signals(0, 0.05f, 0.05f)))
        // blink 1 both
        assertNull(m.process(signals(100, 0.80f, 0.80f)))
        assertNull(m.process(signals(150, 0.80f, 0.80f)))
        assertNull(m.process(signals(200, 0.05f, 0.05f)))
        // blink 2 both
        assertNull(m.process(signals(350, 0.80f, 0.80f)))
        assertNull(m.process(signals(400, 0.80f, 0.80f)))
        val cmd = m.process(signals(450, 0.05f, 0.05f))
        assertNotNull("expected DOUBLE_BLINK_BOTH, got null", cmd)
        assertEquals(GestureType.DOUBLE_BLINK_BOTH, cmd!!.type)
    }
}
