package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultDialerAcceptanceTest {
    @Test
    fun missingRequirementsAreExplicitAndOrdered() {
        val acceptance = DefaultDialerAcceptance(
            actionDialAccepted = true,
            inCallServiceAccepted = true,
            outgoingCallPlacementAccepted = false,
            incomingCallUiAccepted = false,
            ongoingCallUiAccepted = false,
        )

        assertFalse(acceptance.accepted)
        assertEquals(
            listOf(
                DefaultDialerRequirement.OUTGOING_CALL_PLACEMENT,
                DefaultDialerRequirement.INCOMING_CALL_UI,
                DefaultDialerRequirement.ONGOING_CALL_UI,
            ),
            acceptance.missingRequirements,
        )
        assertTrue(acceptance.unavailableReason().contains("outgoing call placement"))
        assertTrue(acceptance.unavailableReason().contains("incoming call UI"))
        assertTrue(acceptance.unavailableReason().contains("ongoing call UI"))
    }

    @Test
    fun acceptanceRequiresEveryRequirement() {
        val acceptance = DefaultDialerAcceptance(
            actionDialAccepted = true,
            inCallServiceAccepted = true,
            outgoingCallPlacementAccepted = true,
            incomingCallUiAccepted = true,
            ongoingCallUiAccepted = true,
        )

        assertTrue(acceptance.accepted)
        assertTrue(acceptance.missingRequirements.isEmpty())
    }

    @Test
    fun currentDevelopmentGateRemainsClosed() {
        assertFalse(DevelopmentDefaultDialerAcceptance.current.accepted)
    }
}
