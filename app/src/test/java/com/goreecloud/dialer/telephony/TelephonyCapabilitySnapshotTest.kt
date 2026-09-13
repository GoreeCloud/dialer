package com.goreecloud.dialer.telephony

import com.goreecloud.dialer.core.capability.CapabilityState
import org.junit.Assert.assertTrue
import org.junit.Test

class TelephonyCapabilitySnapshotTest {
    @Test
    fun developmentPlaceholderDoesNotClaimTelephonyAvailability() {
        val snapshot = TelephonyCapabilitySnapshot.developmentPlaceholder()
        assertTrue(snapshot.defaultDialerRole is CapabilityState.Unavailable)
        assertTrue(snapshot.dialIntentHandling is CapabilityState.Unavailable)
        assertTrue(snapshot.outgoingCalls is CapabilityState.Unavailable)
        assertTrue(snapshot.incomingCalls is CapabilityState.Unavailable)
        assertTrue(snapshot.callScreening is CapabilityState.Unavailable)
        assertTrue(snapshot.callRecording is CapabilityState.Unavailable)
    }
}
