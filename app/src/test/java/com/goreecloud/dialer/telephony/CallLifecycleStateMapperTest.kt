package com.goreecloud.dialer.telephony

import android.telecom.Call
import org.junit.Assert.assertEquals
import org.junit.Test

class CallLifecycleStateMapperTest {
    @Test
    fun mapsKnownAndroidStates() {
        assertEquals(CallLifecycleState.NEW, CallLifecycleStateMapper.fromAndroid(Call.STATE_NEW))
        assertEquals(CallLifecycleState.CONNECTING, CallLifecycleStateMapper.fromAndroid(Call.STATE_CONNECTING))
        assertEquals(
            CallLifecycleState.SELECTING_PHONE_ACCOUNT,
            CallLifecycleStateMapper.fromAndroid(Call.STATE_SELECT_PHONE_ACCOUNT),
        )
        assertEquals(CallLifecycleState.DIALING, CallLifecycleStateMapper.fromAndroid(Call.STATE_DIALING))
        assertEquals(CallLifecycleState.RINGING, CallLifecycleStateMapper.fromAndroid(Call.STATE_RINGING))
        assertEquals(
            CallLifecycleState.SIMULATED_RINGING,
            CallLifecycleStateMapper.fromAndroid(Call.STATE_SIMULATED_RINGING),
        )
        assertEquals(CallLifecycleState.ACTIVE, CallLifecycleStateMapper.fromAndroid(Call.STATE_ACTIVE))
        assertEquals(CallLifecycleState.HOLDING, CallLifecycleStateMapper.fromAndroid(Call.STATE_HOLDING))
        assertEquals(
            CallLifecycleState.DISCONNECTING,
            CallLifecycleStateMapper.fromAndroid(Call.STATE_DISCONNECTING),
        )
        assertEquals(
            CallLifecycleState.PULLING_CALL,
            CallLifecycleStateMapper.fromAndroid(Call.STATE_PULLING_CALL),
        )
        assertEquals(
            CallLifecycleState.AUDIO_PROCESSING,
            CallLifecycleStateMapper.fromAndroid(Call.STATE_AUDIO_PROCESSING),
        )
        assertEquals(
            CallLifecycleState.DISCONNECTED,
            CallLifecycleStateMapper.fromAndroid(Call.STATE_DISCONNECTED),
        )
    }

    @Test
    fun unknownStateRemainsUnknown() {
        assertEquals(CallLifecycleState.UNKNOWN, CallLifecycleStateMapper.fromAndroid(Int.MAX_VALUE))
    }
}
