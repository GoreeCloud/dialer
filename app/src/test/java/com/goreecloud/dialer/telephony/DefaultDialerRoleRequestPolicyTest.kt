package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDialerRoleRequestPolicyTest {
    private val acceptedFacts = DefaultDialerRoleRequestFacts(
        hasTelephony = true,
        roleManagerAvailable = true,
        roleAvailable = true,
        roleHeld = false,
        applicationRequirementsAccepted = true,
        acceptanceReason = "accepted",
    )

    @Test
    fun acceptedRuntimeMayPrepareRoleRequest() {
        assertEquals(
            DefaultDialerRoleRequestDecision.Ready,
            DefaultDialerRoleRequestPolicy.decide(acceptedFacts),
        )
    }

    @Test
    fun currentUnacceptedApplicationRequirementsFailClosed() {
        val result = DefaultDialerRoleRequestPolicy.decide(
            acceptedFacts.copy(
                applicationRequirementsAccepted = false,
                acceptanceReason = "Default-dialer acceptance incomplete: incoming call UI",
            ),
        )
        assertEquals(
            DefaultDialerRoleRequestDecision.Rejected(
                "Default-dialer acceptance incomplete: incoming call UI",
            ),
            result,
        )
    }

    @Test
    fun alreadyHeldRoleDoesNotPrepareAnotherRequest() {
        assertEquals(
            DefaultDialerRoleRequestDecision.AlreadyHeld,
            DefaultDialerRoleRequestPolicy.decide(acceptedFacts.copy(roleHeld = true)),
        )
    }

    @Test
    fun unavailableRoleManagerFailsClosed() {
        assertEquals(
            DefaultDialerRoleRequestDecision.Rejected("Android RoleManager is unavailable"),
            DefaultDialerRoleRequestPolicy.decide(
                acceptedFacts.copy(roleManagerAvailable = false),
            ),
        )
    }
}
