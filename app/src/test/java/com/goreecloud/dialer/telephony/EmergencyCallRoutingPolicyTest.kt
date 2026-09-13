package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class EmergencyCallRoutingPolicyTest {
    @Test
    fun emergencyCallIgnoresExplicitPhoneAccountSelection() {
        assertEquals(
            PhoneAccountSelectionDecision.SystemDefault,
            OutgoingPhoneAccountRoutingPolicy.decide(
                emergencyClassification = EmergencyNumberClassification.EMERGENCY,
                availableRouteIds = setOf(1L, 2L),
                requestedRouteId = 2L,
            ),
        )
    }

    @Test
    fun unknownEmergencyStatusAlsoDelegatesRoutingToAndroid() {
        assertEquals(
            PhoneAccountSelectionDecision.SystemDefault,
            OutgoingPhoneAccountRoutingPolicy.decide(
                emergencyClassification = EmergencyNumberClassification.UNKNOWN,
                availableRouteIds = setOf(1L, 2L),
                requestedRouteId = 2L,
            ),
        )
    }

    @Test
    fun nonEmergencyCallPreservesAvailableExplicitSelection() {
        assertEquals(
            PhoneAccountSelectionDecision.Explicit(2L),
            OutgoingPhoneAccountRoutingPolicy.decide(
                emergencyClassification = EmergencyNumberClassification.NON_EMERGENCY,
                availableRouteIds = setOf(1L, 2L),
                requestedRouteId = 2L,
            ),
        )
    }

    @Test
    fun nonEmergencyCallRejectsStaleExplicitSelection() {
        assertEquals(
            PhoneAccountSelectionDecision.Rejected(
                "Selected phone account is no longer available",
            ),
            OutgoingPhoneAccountRoutingPolicy.decide(
                emergencyClassification = EmergencyNumberClassification.NON_EMERGENCY,
                availableRouteIds = setOf(1L),
                requestedRouteId = 2L,
            ),
        )
    }
}
