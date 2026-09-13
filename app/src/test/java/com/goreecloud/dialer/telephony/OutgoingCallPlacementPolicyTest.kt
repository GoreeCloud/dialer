package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OutgoingCallPlacementPolicyTest {
    private val allowedFacts = OutgoingCallPlacementFacts(
        hasTelephony = true,
        telecomManagerAvailable = true,
        defaultDialerRoleHeld = true,
        callPhonePermissionGranted = true,
        placementPathAccepted = true,
    )

    @Test
    fun allRuntimeFactsAllowPlacement() {
        assertEquals(
            OutgoingCallPlacementDecision.Allowed,
            OutgoingCallPlacementPolicy.decide(allowedFacts),
        )
    }

    @Test
    fun unacceptedPathFailsBeforeRoleOrPermissionClaims() {
        val result = OutgoingCallPlacementPolicy.decide(
            allowedFacts.copy(
                placementPathAccepted = false,
                defaultDialerRoleHeld = false,
                callPhonePermissionGranted = false,
            ),
        )
        assertTrue(result is OutgoingCallPlacementDecision.Rejected)
        assertEquals(
            "Outgoing call placement has not passed GoreeCloud runtime acceptance",
            (result as OutgoingCallPlacementDecision.Rejected).reason,
        )
    }

    @Test
    fun missingDefaultRoleRejectsPlacement() {
        val result = OutgoingCallPlacementPolicy.decide(
            allowedFacts.copy(defaultDialerRoleHeld = false),
        )
        assertEquals(
            OutgoingCallPlacementDecision.Rejected("GoreeCloud Dialer is not the active default dialer"),
            result,
        )
    }

    @Test
    fun missingCallPermissionRejectsPlacement() {
        val result = OutgoingCallPlacementPolicy.decide(
            allowedFacts.copy(callPhonePermissionGranted = false),
        )
        assertEquals(
            OutgoingCallPlacementDecision.Rejected("CALL_PHONE permission is not granted"),
            result,
        )
    }
}
