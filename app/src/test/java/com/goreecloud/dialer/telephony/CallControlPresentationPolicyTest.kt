package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallControlPresentationPolicyTest {
    @Test
    fun ringingCallShowsOnlyAnswerAndDecline() {
        assertEquals(
            listOf(CallControlAction.AnswerAudio, CallControlAction.Decline),
            CallControlPresentationPolicy.actionsFor(CallLifecycleState.RINGING),
        )
    }

    @Test
    fun activeCallShowsHoldAndEnd() {
        assertEquals(
            listOf(CallControlAction.Hold, CallControlAction.End),
            CallControlPresentationPolicy.actionsFor(CallLifecycleState.ACTIVE),
        )
    }

    @Test
    fun heldCallShowsResumeAndEnd() {
        assertEquals(
            listOf(CallControlAction.Resume, CallControlAction.End),
            CallControlPresentationPolicy.actionsFor(CallLifecycleState.HOLDING),
        )
    }

    @Test
    fun terminalOrUnknownStateShowsNoControl() {
        assertTrue(CallControlPresentationPolicy.actionsFor(CallLifecycleState.DISCONNECTED).isEmpty())
        assertTrue(CallControlPresentationPolicy.actionsFor(CallLifecycleState.UNKNOWN).isEmpty())
    }
}
