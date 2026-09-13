package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class CallEndpointRoutingRuntimeTest {
    @Test
    fun detachedRuntimeFailsClosed() {
        val target = FakeTarget()
        InCallEndpointRoutingRuntime.attach(target)
        InCallEndpointRoutingRuntime.detach(target)

        assertEquals(
            CallEndpointRoutingResult.Rejected(
                "Call endpoint routing is unavailable on this runtime",
            ),
            InCallEndpointRoutingRuntime.request(7L),
        )
    }

    @Test
    fun attachedRuntimeDelegatesOnlyOpaqueRouteId() {
        val target = FakeTarget()
        InCallEndpointRoutingRuntime.attach(target)
        try {
            assertEquals(
                CallEndpointRoutingResult.Submitted,
                InCallEndpointRoutingRuntime.request(42L),
            )
            assertEquals(listOf(42L), target.routeIds)
        } finally {
            InCallEndpointRoutingRuntime.detach(target)
        }
    }

    private class FakeTarget : CallEndpointRoutingTarget {
        val routeIds = mutableListOf<Long>()

        override fun request(routeId: Long): CallEndpointRoutingResult {
            routeIds += routeId
            return CallEndpointRoutingResult.Submitted
        }
    }
}
