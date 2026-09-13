package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConferenceControlPolicyTest {
    private val baseFacts = ConferenceControlFacts(
        sourceSessionId = 1L,
        state = CallLifecycleState.ACTIVE,
        conferenceableSessionIds = setOf(2L),
        hasParent = true,
        mergeConferenceAvailable = true,
        swapConferenceAvailable = true,
        separateFromConferenceAvailable = true,
    )

    @Test
    fun pairwiseConferenceRequiresTelecomConferenceableEvidence() {
        assertEquals(
            ConferenceControlDecision.Allowed,
            ConferenceControlPolicy.decide(
                ConferenceControlAction.ConferenceWith(2L),
                baseFacts,
            ),
        )

        val rejected = ConferenceControlPolicy.decide(
            ConferenceControlAction.ConferenceWith(3L),
            baseFacts,
        )
        assertTrue(rejected is ConferenceControlDecision.Rejected)
    }

    @Test
    fun callCannotConferenceWithItself() {
        val result = ConferenceControlPolicy.decide(
            ConferenceControlAction.ConferenceWith(1L),
            baseFacts.copy(conferenceableSessionIds = setOf(1L)),
        )
        assertEquals(
            ConferenceControlDecision.Rejected("A call cannot conference with itself"),
            result,
        )
    }

    @Test
    fun mergeSwapAndSeparateRequireLiveCapabilityEvidence() {
        assertTrue(
            ConferenceControlPolicy.decide(
                ConferenceControlAction.MergeConference,
                baseFacts.copy(mergeConferenceAvailable = false),
            ) is ConferenceControlDecision.Rejected,
        )
        assertTrue(
            ConferenceControlPolicy.decide(
                ConferenceControlAction.SwapConference,
                baseFacts.copy(swapConferenceAvailable = false),
            ) is ConferenceControlDecision.Rejected,
        )
        assertTrue(
            ConferenceControlPolicy.decide(
                ConferenceControlAction.SeparateFromConference,
                baseFacts.copy(separateFromConferenceAvailable = false),
            ) is ConferenceControlDecision.Rejected,
        )
    }

    @Test
    fun separateRequiresTrackedParentRelationship() {
        val result = ConferenceControlPolicy.decide(
            ConferenceControlAction.SeparateFromConference,
            baseFacts.copy(hasParent = false),
        )
        assertEquals(
            ConferenceControlDecision.Rejected(
                "This call is not currently attached to a tracked conference",
            ),
            result,
        )
    }

    @Test
    fun conferenceControlsRejectNonConnectedLifecycleStates() {
        val result = ConferenceControlPolicy.decide(
            ConferenceControlAction.ConferenceWith(2L),
            baseFacts.copy(state = CallLifecycleState.RINGING),
        )
        assertTrue(result is ConferenceControlDecision.Rejected)
    }
}
