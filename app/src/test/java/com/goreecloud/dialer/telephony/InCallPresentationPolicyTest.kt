package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class InCallPresentationPolicyTest {
    @Test
    fun ringingActiveAndHoldingSessionsSortAheadOfTransitionalCalls() {
        val calls = listOf(
            call(5, CallLifecycleState.DIALING),
            call(4, CallLifecycleState.HOLDING),
            call(3, CallLifecycleState.ACTIVE),
            call(2, CallLifecycleState.RINGING),
            call(1, CallLifecycleState.ACTIVE),
        )

        assertEquals(
            listOf(2L, 1L, 3L, 4L, 5L),
            InCallPresentationPolicy.orderedCalls(calls).map { it.sessionId },
        )
    }

    @Test
    fun callSetLabelUsesOnlyAggregateTelecomEvidence() {
        assertEquals(
            "2 live calls • add-call capacity available",
            InCallPresentationPolicy.callSetLabel(
                InCallRuntimeSnapshot(
                    trackedCallCount = 2,
                    canAddCall = true,
                ),
            ),
        )
        assertEquals(
            "1 live call • add-call capacity awaiting Telecom",
            InCallPresentationPolicy.callSetLabel(
                InCallRuntimeSnapshot(
                    trackedCallCount = 1,
                    canAddCall = null,
                ),
            ),
        )
    }

    private fun call(sessionId: Long, state: CallLifecycleState) = CallRuntimeSummary(
        sessionId = sessionId,
        state = state,
        direction = CallDirection.UNKNOWN,
        terminalOutcome = null,
        connectedAtElapsedRealtimeMillis = null,
        holdSupported = false,
        holdCurrentlyAvailable = false,
        muteSupported = false,
        manageConferenceSupported = false,
        mergeConferenceAvailable = false,
        swapConferenceAvailable = false,
        separateFromConferenceAvailable = false,
        conferenceableSessionIds = emptyList(),
        parentSessionId = null,
        childSessionIds = emptyList(),
        postDialWaitPending = false,
        postDialRemainingCharacterCount = 0,
    )

    @Test
    fun callSetLabelSummarizesOnlyAggregateLifecycleCounts() {
        val snapshot = InCallRuntimeSnapshot(
            trackedCallCount = 4,
            stateCounts = mapOf(
                CallLifecycleState.RINGING to 1,
                CallLifecycleState.ACTIVE to 2,
                CallLifecycleState.HOLDING to 1,
            ),
            canAddCall = false,
        )

        assertEquals(
            listOf("1 ringing", "2 active", "1 held"),
            InCallPresentationPolicy.stateBreakdown(snapshot),
        )
        assertEquals(
            "4 live calls • 1 ringing • 2 active • 1 held • add-call capacity unavailable",
            InCallPresentationPolicy.callSetLabel(snapshot),
        )
    }


    @Test
    fun conferenceBreakdownUsesOnlySessionTopologyCounts() {
        val parent = call(1, CallLifecycleState.ACTIVE).copy(
            childSessionIds = listOf(2L, 3L),
        )
        val childA = call(2, CallLifecycleState.ACTIVE).copy(parentSessionId = 1L)
        val childB = call(3, CallLifecycleState.HOLDING).copy(parentSessionId = 1L)
        val snapshot = InCallRuntimeSnapshot(
            trackedCallCount = 3,
            calls = listOf(parent, childA, childB),
            stateCounts = mapOf(
                CallLifecycleState.ACTIVE to 2,
                CallLifecycleState.HOLDING to 1,
            ),
            canAddCall = false,
        )

        assertEquals(
            listOf("1 conference", "2 conference legs"),
            InCallPresentationPolicy.conferenceBreakdown(snapshot),
        )
        assertEquals(
            "3 live calls • 2 active • 1 held • 1 conference • 2 conference legs • add-call capacity unavailable",
            InCallPresentationPolicy.callSetLabel(snapshot),
        )
    }

}
