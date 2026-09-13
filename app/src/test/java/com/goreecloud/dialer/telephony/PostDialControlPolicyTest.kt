package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostDialControlPolicyTest {
    @Test
    fun pendingWaitAllowsExplicitContinueOrCancel() {
        val facts = PostDialControlFacts(
            state = CallLifecycleState.ACTIVE,
            waitPending = true,
        )
        assertEquals(PostDialControlDecision.Allowed, PostDialControlPolicy.decide(facts))
    }

    @Test
    fun noPendingWaitFailsClosed() {
        val result = PostDialControlPolicy.decide(
            PostDialControlFacts(
                state = CallLifecycleState.ACTIVE,
                waitPending = false,
            ),
        )
        assertEquals(
            PostDialControlDecision.Rejected("No post-dial confirmation is currently pending"),
            result,
        )
    }

    @Test
    fun disconnectedCallCannotContinuePostDial() {
        val result = PostDialControlPolicy.decide(
            PostDialControlFacts(
                state = CallLifecycleState.DISCONNECTED,
                waitPending = true,
            ),
        )
        assertTrue(result is PostDialControlDecision.Rejected)
    }
}
