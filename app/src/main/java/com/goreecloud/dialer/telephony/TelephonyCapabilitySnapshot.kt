package com.goreecloud.dialer.telephony

import com.goreecloud.dialer.core.capability.CapabilityState

/** A point-in-time view of independently accepted telephony capabilities. */
data class TelephonyCapabilitySnapshot(
    val defaultDialerRole: CapabilityState,
    val dialIntentHandling: CapabilityState,
    val outgoingCalls: CapabilityState,
    val incomingCalls: CapabilityState,
    val inCallControls: CapabilityState,
    val multiSimRouting: CapabilityState,
    val callScreening: CapabilityState,
    val visualVoicemail: CapabilityState,
    val callRecording: CapabilityState,
) {
    companion object {
        fun developmentPlaceholder() = TelephonyCapabilitySnapshot(
            defaultDialerRole = CapabilityState.Unavailable("Telecom role integration not implemented"),
            dialIntentHandling = CapabilityState.Unavailable("ACTION_DIAL handling not implemented"),
            outgoingCalls = CapabilityState.Unavailable("Carrier call integration not implemented"),
            incomingCalls = CapabilityState.Unavailable("Carrier call integration not implemented"),
            inCallControls = CapabilityState.Unavailable("In-call service not implemented"),
            multiSimRouting = CapabilityState.Unavailable("Subscription routing not implemented"),
            callScreening = CapabilityState.Unavailable("Call screening service not implemented"),
            visualVoicemail = CapabilityState.Unavailable("Voicemail adapter not implemented"),
            callRecording = CapabilityState.Unavailable("Recording capability not implemented"),
        )
    }
}
