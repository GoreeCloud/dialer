package com.goreecloud.dialer.telephony

import android.telecom.Call

data class CallControlCapabilities(
    val holdSupported: Boolean,
    val holdCurrentlyAvailable: Boolean,
)

object AndroidCallControlCapabilities {
    fun from(details: Call.Details): CallControlCapabilities = CallControlCapabilities(
        holdSupported = details.can(Call.Details.CAPABILITY_SUPPORT_HOLD),
        holdCurrentlyAvailable = details.can(Call.Details.CAPABILITY_HOLD),
    )
}
