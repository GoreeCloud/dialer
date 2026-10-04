package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CallDurationPresentationPolicyTest {
    @Test
    fun presentsOnlyConnectedLifecycleStates() {
        assertTrue(CallDurationPresentationPolicy.shouldPresent(CallLifecycleState.ACTIVE))
        assertTrue(CallDurationPresentationPolicy.shouldPresent(CallLifecycleState.HOLDING))
        assertFalse(CallDurationPresentationPolicy.shouldPresent(CallLifecycleState.DIALING))
        assertFalse(CallDurationPresentationPolicy.shouldPresent(CallLifecycleState.DISCONNECTED))
    }

    @Test
    fun convertsWallClockConnectTimeIntoPrivacyMinimizedMonotonicAnchor() {
        assertNull(
            CallDurationPresentationPolicy.monotonicAnchorMillis(
                connectTimeMillis = null,
                wallClockNowMillis = 70_000L,
                elapsedRealtimeNowMillis = 30_000L,
            ),
        )
        assertNull(
            CallDurationPresentationPolicy.monotonicAnchorMillis(
                connectTimeMillis = 71_000L,
                wallClockNowMillis = 70_000L,
                elapsedRealtimeNowMillis = 30_000L,
            ),
        )
        assertEquals(
            25_000L,
            CallDurationPresentationPolicy.monotonicAnchorMillis(
                connectTimeMillis = 65_000L,
                wallClockNowMillis = 70_000L,
                elapsedRealtimeNowMillis = 30_000L,
            ),
        )
        assertEquals(
            0L,
            CallDurationPresentationPolicy.monotonicAnchorMillis(
                connectTimeMillis = 1_000L,
                wallClockNowMillis = 70_000L,
                elapsedRealtimeNowMillis = 30_000L,
            ),
        )
    }

    @Test
    fun elapsedTimeRequiresAValidNonFutureMonotonicAnchor() {
        assertNull(CallDurationPresentationPolicy.elapsedSeconds(null, 10_000L))
        assertNull(CallDurationPresentationPolicy.elapsedSeconds(11_000L, 10_000L))
        assertEquals(
            65L,
            CallDurationPresentationPolicy.elapsedSeconds(5_000L, 70_999L),
        )
    }

    @Test
    fun formatsMinuteAndHourDurationsWithoutCallContent() {
        assertEquals("Connected • 00:00", CallDurationPresentationPolicy.label(0L))
        assertEquals("Connected • 01:05", CallDurationPresentationPolicy.label(65L))
        assertEquals("Connected • 1:01:01", CallDurationPresentationPolicy.label(3_661L))
    }
}
