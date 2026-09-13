package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionRouteCoordinatorTest {
    @Test
    fun permissionAndPlatformFailuresDoNotBecomeNoSimDecisions() {
        assertEquals(
            SubscriptionRouteReadiness.PermissionRequired,
            SubscriptionRouteCoordinator.evaluate(
                inventory = SubscriptionInventoryResult.PermissionRequired,
                explicitlySelectedSubscriptionId = null,
                isEmergencyCall = false,
            ),
        )
        assertEquals(
            SubscriptionRouteReadiness.Unsupported,
            SubscriptionRouteCoordinator.evaluate(
                inventory = SubscriptionInventoryResult.Unsupported,
                explicitlySelectedSubscriptionId = null,
                isEmergencyCall = false,
            ),
        )
        assertEquals(
            SubscriptionRouteReadiness.Unavailable("PlatformFailure"),
            SubscriptionRouteCoordinator.evaluate(
                inventory = SubscriptionInventoryResult.Unavailable("PlatformFailure"),
                explicitlySelectedSubscriptionId = null,
                isEmergencyCall = false,
            ),
        )
    }

    @Test
    fun successfulInventoryFlowsThroughExistingPolicy() {
        val readiness = SubscriptionRouteCoordinator.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(11), ActiveSubscription(22)),
            ),
            explicitlySelectedSubscriptionId = 22,
            isEmergencyCall = false,
        )

        assertEquals(
            SubscriptionRouteReadiness.Decision(SubscriptionRouteDecision.UseSubscription(22)),
            readiness,
        )
    }

    @Test
    fun successfulEmptyInventoryRemainsARealNoRouteDecision() {
        val readiness = SubscriptionRouteCoordinator.evaluate(
            inventory = SubscriptionInventoryResult.Available(emptyList()),
            explicitlySelectedSubscriptionId = null,
            isEmergencyCall = false,
        )

        assertTrue(
            (readiness as SubscriptionRouteReadiness.Decision).decision is
                SubscriptionRouteDecision.Unavailable,
        )
    }

    @Test
    fun emergencyRoutingStillDefersToAndroidTelecom() {
        val readiness = SubscriptionRouteCoordinator.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(11), ActiveSubscription(22)),
            ),
            explicitlySelectedSubscriptionId = 22,
            isEmergencyCall = true,
        )

        assertEquals(
            SubscriptionRouteReadiness.Decision(
                SubscriptionRouteDecision.DeferEmergencyToPlatform,
            ),
            readiness,
        )
    }
}
