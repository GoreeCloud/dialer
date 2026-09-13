package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class CallStateReadPolicyTest {
    @Test
    fun `api 29 uses legacy Call state`() {
        assertEquals(
            CallStateReadSource.LEGACY_CALL,
            CallStateReadPolicy.sourceForSdk(29),
        )
    }

    @Test
    fun `api 30 uses legacy Call state`() {
        assertEquals(
            CallStateReadSource.LEGACY_CALL,
            CallStateReadPolicy.sourceForSdk(30),
        )
    }

    @Test
    fun `api 31 uses Call Details state`() {
        assertEquals(
            CallStateReadSource.DETAILS,
            CallStateReadPolicy.sourceForSdk(31),
        )
    }

    @Test
    fun `newer runtimes continue using Call Details state`() {
        assertEquals(
            CallStateReadSource.DETAILS,
            CallStateReadPolicy.sourceForSdk(36),
        )
    }
}
