package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class CallCapabilitiesTest {
    @Test
    fun missingDetailsFailsClosed() {
        assertEquals(
            CallControlCapabilities(),
            AndroidCallControlCapabilities.from(null),
        )
    }
}
