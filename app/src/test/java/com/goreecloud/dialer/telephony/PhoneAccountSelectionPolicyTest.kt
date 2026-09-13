package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneAccountSelectionPolicyTest {
    @Test
    fun noExplicitSelectionUsesSystemDefault() {
        assertEquals(
            PhoneAccountSelectionDecision.SystemDefault,
            PhoneAccountSelectionPolicy.decide(
                availableRouteIds = setOf(1L, 2L),
                requestedRouteId = null,
            ),
        )
    }

    @Test
    fun availableExplicitSelectionIsPreserved() {
        assertEquals(
            PhoneAccountSelectionDecision.Explicit(2L),
            PhoneAccountSelectionPolicy.decide(
                availableRouteIds = setOf(1L, 2L),
                requestedRouteId = 2L,
            ),
        )
    }

    @Test
    fun staleExplicitSelectionFailsClosed() {
        assertEquals(
            PhoneAccountSelectionDecision.Rejected(
                "Selected phone account is no longer available",
            ),
            PhoneAccountSelectionPolicy.decide(
                availableRouteIds = setOf(1L),
                requestedRouteId = 2L,
            ),
        )
    }
}
