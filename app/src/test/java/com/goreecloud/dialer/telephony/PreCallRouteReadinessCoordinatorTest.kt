package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreCallRouteReadinessCoordinatorTest {
    @Test
    fun emergencyDefersBeforeInventoryOrAccountAuthority() {
        var resolverCalls = 0

        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.PermissionRequired,
            explicitlySelectedSubscriptionId = 10,
            isEmergencyCall = true,
            resolvePhoneAccount = {
                resolverCalls += 1
                PhoneAccountProjection.Available("account-a")
            },
        )

        assertEquals(PreCallRouteDecision.DeferEmergencyToPlatform, decision)
        assertEquals(0, resolverCalls)
    }

    @Test
    fun permissionRequiredInventoryFailsBeforeAccountResolution() {
        var resolverCalls = 0

        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.PermissionRequired,
            explicitlySelectedSubscriptionId = null,
            isEmergencyCall = false,
            resolvePhoneAccount = {
                resolverCalls += 1
                PhoneAccountProjection.Available("account-a")
            },
        )

        assertEquals(PreCallRouteDecision.PermissionRequired, decision)
        assertEquals(0, resolverCalls)
    }

    @Test
    fun multipleSubscriptionsWithoutSelectionRequireUserChoice() {
        var resolverCalls = 0

        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(10), ActiveSubscription(20)),
            ),
            explicitlySelectedSubscriptionId = null,
            isEmergencyCall = false,
            resolvePhoneAccount = {
                resolverCalls += 1
                PhoneAccountProjection.Available("unexpected")
            },
        )

        assertTrue(decision is PreCallRouteDecision.RequiresUserSelection)
        assertEquals(
            listOf(10, 20),
            (decision as PreCallRouteDecision.RequiresUserSelection).subscriptionIds,
        )
        assertEquals(0, resolverCalls)
    }

    @Test
    fun staleExplicitSelectionFailsWithoutFallingBack() {
        var resolverCalls = 0

        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(10), ActiveSubscription(20)),
            ),
            explicitlySelectedSubscriptionId = 30,
            isEmergencyCall = false,
            resolvePhoneAccount = {
                resolverCalls += 1
                PhoneAccountProjection.Available("unexpected")
            },
        )

        assertTrue(decision is PreCallRouteDecision.Unavailable)
        assertEquals(0, resolverCalls)
    }

    @Test
    fun singleSubscriptionMustResolveItsExactTelecomAccount() {
        val resolvedIds = mutableListOf<Int>()

        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(20)),
            ),
            explicitlySelectedSubscriptionId = null,
            isEmergencyCall = false,
            resolvePhoneAccount = { subscriptionId ->
                resolvedIds += subscriptionId
                PhoneAccountProjection.Available("account-b")
            },
        )

        assertTrue(decision is PreCallRouteDecision.Ready)
        decision as PreCallRouteDecision.Ready
        assertEquals(20, decision.subscriptionId)
        assertEquals("account-b", decision.account)
        assertEquals(listOf(20), resolvedIds)
    }

    @Test
    fun explicitActiveSubscriptionResolvesOnlyThatSubscription() {
        val resolvedIds = mutableListOf<Int>()

        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(10), ActiveSubscription(20)),
            ),
            explicitlySelectedSubscriptionId = 20,
            isEmergencyCall = false,
            resolvePhoneAccount = { subscriptionId ->
                resolvedIds += subscriptionId
                PhoneAccountProjection.Available("account-$subscriptionId")
            },
        )

        assertTrue(decision is PreCallRouteDecision.Ready)
        decision as PreCallRouteDecision.Ready
        assertEquals(20, decision.subscriptionId)
        assertEquals("account-20", decision.account)
        assertEquals(listOf(20), resolvedIds)
    }

    @Test
    fun telecomPermissionRevocationFailsClosed() {
        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(10)),
            ),
            explicitlySelectedSubscriptionId = null,
            isEmergencyCall = false,
            resolvePhoneAccount = { PhoneAccountProjection.PermissionRequired },
        )

        assertEquals(PreCallRouteDecision.PermissionRequired, decision)
    }

    @Test
    fun unresolvedTelecomAccountNeverBecomesReady() {
        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = SubscriptionInventoryResult.Available(
                listOf(ActiveSubscription(10)),
            ),
            explicitlySelectedSubscriptionId = null,
            isEmergencyCall = false,
            resolvePhoneAccount = {
                PhoneAccountProjection.Unavailable("No exact account mapping")
            },
        )

        assertTrue(decision is PreCallRouteDecision.Unavailable)
        assertEquals(
            "No exact account mapping",
            (decision as PreCallRouteDecision.Unavailable).reason,
        )
    }
}
