package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class CallAudioControlEngineTest {
    @Test
    fun muteSubmitsMutedStateRequest() {
        val target = FakeTarget()
        assertEquals(
            CallAudioControlResult.Submitted,
            CallAudioControlEngine(target).execute(CallAudioControlAction.Mute),
        )
        assertEquals(listOf(true), target.states)
    }

    @Test
    fun unmuteSubmitsUnmutedStateRequest() {
        val target = FakeTarget()
        assertEquals(
            CallAudioControlResult.Submitted,
            CallAudioControlEngine(target).execute(CallAudioControlAction.Unmute),
        )
        assertEquals(listOf(false), target.states)
    }

    @Test
    fun runtimeFailureIsReported() {
        val result = CallAudioControlEngine(
            object : CallAudioControlTarget {
                override fun setMuted(isMuted: Boolean) {
                    throw IllegalStateException("audio unavailable")
                }
            },
        ).execute(CallAudioControlAction.Mute)

        assertEquals(
            CallAudioControlResult.Failed("audio unavailable"),
            result,
        )
    }

    private class FakeTarget : CallAudioControlTarget {
        val states = mutableListOf<Boolean>()
        override fun setMuted(isMuted: Boolean) {
            states += isMuted
        }
    }
}
