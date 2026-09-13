package com.goreecloud.dialer.telephony

import android.telecom.Call
import org.junit.Assert.assertEquals
import org.junit.Test

class CallLifecycleStateMapperTest {
    @Test
    fun mapsKnownAndroidStates() {
        assertEquals(CallLifecycleState.RINGING, CallLifecycleStateMapper.fromAndroid(Call.STATE_RINGING))
        assertEquals(CallLifecycleState.ACTIVE, CallLifecycleStateMapper.fromAndroid(Call.STATE_ACTIVE))
        assertEquals(CallLifecycleState.HOLDING, CallLifecycleStateMapper.fromAndroid(Call.STATE_HOLDING))
        assertEquals(CallLifecycleState.DISCONNECTED, CallLifecycleStateMapper.fromAndroid(Call.STATE_DISCONNECTED))
    }

    @Test
    fun unknownStateRemainsUnknown() {
        assertEquals(CallLifecycleState.UNKNOWN, CallLifecycleStateMapper.fromAndroid(Int.MAX_VALUE))
    }
}
