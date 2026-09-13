package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallControlPresentationPolicyTest {
    @Test
    fun ringingStatesShowOnlyAnswerAndDecline() {
        val expected = listOf(CallControlAction.AnswerAudio, CallControlAction.Decline)
        assertEquals(expected, CallControlPresentationPolicy.actionsFor(CallLifecycleState.RINGING))
        assertEquals(
            expected,
            CallControlPresentationPolicy.actionsFor(CallLifecycleState.SIMULATED_RINGING),
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
    fun specialAndTerminalStatesFailClosed() {
        listOf(
            CallLifecycleState.NEW,
            CallLifecycleState.SELECTING_PHONE_ACCOUNT,
            CallLifecycleState.DISCONNECTING,
            CallLifecycleState.PULLING_CALL,
            CallLifecycleState.AUDIO_PROCESSING,
            CallLifecycleState.DISCONNECTED,
            CallLifecycleState.UNKNOWN,
        ).forEach { state ->
            assertTrue(CallControlPresentationPolicy.actionsFor(state).isEmpty())
        }
    }
}
