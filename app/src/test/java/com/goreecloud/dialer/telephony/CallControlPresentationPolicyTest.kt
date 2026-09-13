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
    fun activeCallShowsHoldAndEndWhenHoldIsCurrentlyAvailable() {
        assertEquals(
            listOf(CallControlAction.Hold, CallControlAction.End),
            CallControlPresentationPolicy.actionsFor(
                state = CallLifecycleState.ACTIVE,
                holdCurrentlyAvailable = true,
            ),
        )
    }

    @Test
    fun activeCallHidesHoldWhenTelecomDoesNotCurrentlyAllowIt() {
        assertEquals(
            listOf(CallControlAction.End),
            CallControlPresentationPolicy.actionsFor(
                state = CallLifecycleState.ACTIVE,
                holdCurrentlyAvailable = false,
            ),
        )
    }

    @Test
    fun heldCallShowsResumeAndEndWhenUnholdIsCurrentlyAvailable() {
        assertEquals(
            listOf(CallControlAction.Resume, CallControlAction.End),
            CallControlPresentationPolicy.actionsFor(
                state = CallLifecycleState.HOLDING,
                holdCurrentlyAvailable = true,
            ),
        )
    }

    @Test
    fun heldCallHidesResumeWhenTelecomDoesNotCurrentlyAllowIt() {
        assertEquals(
            listOf(CallControlAction.End),
            CallControlPresentationPolicy.actionsFor(
                state = CallLifecycleState.HOLDING,
                holdCurrentlyAvailable = false,
            ),
        )
    }

    @Test
    fun terminalOrUnknownStateShowsNoControl() {
        assertTrue(CallControlPresentationPolicy.actionsFor(CallLifecycleState.DISCONNECTED).isEmpty())
        assertTrue(CallControlPresentationPolicy.actionsFor(CallLifecycleState.UNKNOWN).isEmpty())
    }
}
