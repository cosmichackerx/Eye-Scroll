package com.eyescroll.app.domain.gesture

import com.eyescroll.app.domain.model.FaceSignals
import com.eyescroll.app.domain.model.GestureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GestureStateMachineTest {

    private fun signals(
        t: Long,
        left: Float,
        right: Float,
        nose: Float = 1f,
        brow: Float = 0f,
        jaw: Float = 0f
    ) = FaceSignals(
        eyeBlinkLeft = left,
        eyeBlinkRight = right,
        browInnerUp = brow,
        jawOpen = jaw,
        noseSneerLeft = 0f,
        noseSneerRight = 0f,
        noseWidthRatio = nose,
        headYaw = 0f,
        headPitch = 0f,
        facePresent = true,
        timestampMs = t
    )

    @Test
    fun doubleWinkRight_firesNext() {
        val m = GestureStateMachine()
        // open
        assertNull(m.process(signals(0, 0.1f, 0.1f)))
        // right close then open
        m.process(signals(100, 0.1f, 0.7f))
        m.process(signals(200, 0.1f, 0.1f))
        m.process(signals(350, 0.1f, 0.7f))
        val cmd = m.process(signals(450, 0.1f, 0.1f))
        assertNotNull(cmd)
        assertEquals(GestureType.DOUBLE_WINK_RIGHT, cmd!!.type)
    }

    @Test
    fun doubleBlinkBoth_firesHome() {
        val m = GestureStateMachine()
        m.process(signals(0, 0.1f, 0.1f))
        m.process(signals(100, 0.7f, 0.7f))
        m.process(signals(200, 0.1f, 0.1f))
        m.process(signals(350, 0.7f, 0.7f))
        val cmd = m.process(signals(450, 0.1f, 0.1f))
        assertNotNull(cmd)
        assertEquals(GestureType.DOUBLE_BLINK_BOTH, cmd!!.type)
    }
}
