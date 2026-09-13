package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallControlEngineTest {
    @Test
    fun ringingCallCanBeAnswered() {
        val target = FakeTarget(CallLifecycleState.RINGING)
        assertEquals(
            CallControlResult.Succeeded,
            CallControlEngine(target).execute(CallControlAction.AnswerAudio),
        )
        assertEquals(listOf("answer"), target.events)
    }

    @Test
    fun nonRingingCallCannotBeDeclined() {
        val target = FakeTarget(CallLifecycleState.ACTIVE)
        val result = CallControlEngine(target).execute(CallControlAction.Decline)
        assertTrue(result is CallControlResult.Rejected)
        assertTrue(target.events.isEmpty())
    }

    @Test
    fun activeCallCanBeHeldAndHoldingCallCanBeResumed() {
        val active = FakeTarget(CallLifecycleState.ACTIVE)
        assertEquals(
            CallControlResult.Succeeded,
            CallControlEngine(active).execute(CallControlAction.Hold),
        )
        assertEquals(listOf("hold"), active.events)

        val held = FakeTarget(CallLifecycleState.HOLDING)
        assertEquals(
            CallControlResult.Succeeded,
            CallControlEngine(held).execute(CallControlAction.Resume),
        )
        assertEquals(listOf("resume"), held.events)
    }

    @Test
    fun dtmfRequiresValidDigitAndConnectedState() {
        val active = FakeTarget(CallLifecycleState.ACTIVE)
        assertEquals(
            CallControlResult.Succeeded,
            CallControlEngine(active).execute(CallControlAction.StartDtmf('#')),
        )
        assertEquals(listOf("dtmf:#"), active.events)

        val invalid = FakeTarget(CallLifecycleState.ACTIVE)
        assertTrue(
            CallControlEngine(invalid).execute(CallControlAction.StartDtmf('A'))
                is CallControlResult.Rejected,
        )
        assertTrue(invalid.events.isEmpty())
    }

    @Test
    fun runtimeFailureIsReportedInsteadOfClaimingSuccess() {
        val target = FakeTarget(CallLifecycleState.ACTIVE, fail = true)
        val result = CallControlEngine(target).execute(CallControlAction.End)
        assertTrue(result is CallControlResult.Failed)
    }

    private class FakeTarget(
        override val state: CallLifecycleState,
        private val fail: Boolean = false,
    ) : CallControlTarget {
        val events = mutableListOf<String>()

        private fun record(event: String) {
            if (fail) throw IllegalStateException("platform failure")
            events += event
        }

        override fun answerAudio() = record("answer")
        override fun decline() = record("decline")
        override fun disconnect() = record("end")
        override fun hold() = record("hold")
        override fun resume() = record("resume")
        override fun startDtmf(digit: Char) = record("dtmf:$digit")
        override fun stopDtmf() = record("dtmf-stop")
    }
}
