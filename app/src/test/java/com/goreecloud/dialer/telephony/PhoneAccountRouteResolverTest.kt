package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneAccountRouteResolverTest {
    @Test
    fun exactSubscriptionMatchResolvesOneHandle() {
        val decision = PhoneAccountCandidatePolicy.resolve(
            candidates = listOf(
                PhoneAccountCandidate(handle = "account-a", subscriptionId = 10),
                PhoneAccountCandidate(handle = "account-b", subscriptionId = 20),
            ),
            subscriptionId = 20,
        )

        assertTrue(decision is PhoneAccountCandidateDecision.Resolved)
        assertEquals(
            "account-b",
            (decision as PhoneAccountCandidateDecision.Resolved).handle,
        )
    }

    @Test
    fun noMatchingHandleFailsClosed() {
        assertEquals(
            PhoneAccountCandidateDecision.NoMatch,
            PhoneAccountCandidatePolicy.resolve(
                candidates = listOf(PhoneAccountCandidate("account-a", 10)),
                subscriptionId = 20,
            ),
        )
    }

    @Test
    fun multipleDistinctHandlesForOneSubscriptionAreAmbiguous() {
        assertEquals(
            PhoneAccountCandidateDecision.Ambiguous,
            PhoneAccountCandidatePolicy.resolve(
                candidates = listOf(
                    PhoneAccountCandidate("account-a", 20),
                    PhoneAccountCandidate("account-b", 20),
                ),
                subscriptionId = 20,
            ),
        )
    }

    @Test
    fun duplicateObservationOfSameHandleDoesNotCreateFalseAmbiguity() {
        val decision = PhoneAccountCandidatePolicy.resolve(
            candidates = listOf(
                PhoneAccountCandidate("account-a", 20),
                PhoneAccountCandidate("account-a", 20),
            ),
            subscriptionId = 20,
        )

        assertTrue(decision is PhoneAccountCandidateDecision.Resolved)
        assertEquals(
            "account-a",
            (decision as PhoneAccountCandidateDecision.Resolved).handle,
        )
    }

    @Test
    fun unrelatedAccountsCannotBecomeFallbacks() {
        assertEquals(
            PhoneAccountCandidateDecision.NoMatch,
            PhoneAccountCandidatePolicy.resolve(
                candidates = listOf(
                    PhoneAccountCandidate("account-a", 10),
                    PhoneAccountCandidate("account-b", 30),
                ),
                subscriptionId = 20,
            ),
        )
    }
}
