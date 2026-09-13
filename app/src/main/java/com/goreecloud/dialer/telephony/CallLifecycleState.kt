package com.goreecloud.dialer.telephony

import android.telecom.Call

enum class CallLifecycleState {
    NEW,
    CONNECTING,
    SELECTING_PHONE_ACCOUNT,
    DIALING,
    RINGING,
    SIMULATED_RINGING,
    ACTIVE,
    HOLDING,
    DISCONNECTING,
    PULLING_CALL,
    AUDIO_PROCESSING,
    DISCONNECTED,
    UNKNOWN,
}

object CallLifecycleStateMapper {
    fun fromAndroid(state: Int): CallLifecycleState = when (state) {
        Call.STATE_NEW -> CallLifecycleState.NEW
        Call.STATE_CONNECTING -> CallLifecycleState.CONNECTING
        Call.STATE_SELECT_PHONE_ACCOUNT -> CallLifecycleState.SELECTING_PHONE_ACCOUNT
        Call.STATE_DIALING -> CallLifecycleState.DIALING
        Call.STATE_RINGING -> CallLifecycleState.RINGING
        Call.STATE_SIMULATED_RINGING -> CallLifecycleState.SIMULATED_RINGING
        Call.STATE_ACTIVE -> CallLifecycleState.ACTIVE
        Call.STATE_HOLDING -> CallLifecycleState.HOLDING
        Call.STATE_DISCONNECTING -> CallLifecycleState.DISCONNECTING
        Call.STATE_PULLING_CALL -> CallLifecycleState.PULLING_CALL
        Call.STATE_AUDIO_PROCESSING -> CallLifecycleState.AUDIO_PROCESSING
        Call.STATE_DISCONNECTED -> CallLifecycleState.DISCONNECTED
        else -> CallLifecycleState.UNKNOWN
    }
}
