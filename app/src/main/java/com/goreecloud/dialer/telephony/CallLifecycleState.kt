package com.goreecloud.dialer.telephony

import android.telecom.Call

enum class CallLifecycleState {
    NEW,
    CONNECTING,
    DIALING,
    RINGING,
    ACTIVE,
    HOLDING,
    DISCONNECTED,
    UNKNOWN,
}

object CallLifecycleStateMapper {
    fun fromAndroid(state: Int): CallLifecycleState = when (state) {
        Call.STATE_NEW -> CallLifecycleState.NEW
        Call.STATE_CONNECTING -> CallLifecycleState.CONNECTING
        Call.STATE_DIALING -> CallLifecycleState.DIALING
        Call.STATE_RINGING -> CallLifecycleState.RINGING
        Call.STATE_ACTIVE -> CallLifecycleState.ACTIVE
        Call.STATE_HOLDING -> CallLifecycleState.HOLDING
        Call.STATE_DISCONNECTED -> CallLifecycleState.DISCONNECTED
        else -> CallLifecycleState.UNKNOWN
    }
}
