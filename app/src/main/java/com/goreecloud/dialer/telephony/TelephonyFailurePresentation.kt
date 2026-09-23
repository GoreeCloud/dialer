package com.goreecloud.dialer.telephony

/**
 * Privacy-safe user-visible failure reasons for Android Telecom operations.
 *
 * Android exception messages are intentionally not projected because provider, account, endpoint,
 * device, number, or implementation details may be embedded in platform-supplied text.
 */
internal object TelephonyFailurePresentation {
    const val AUDIO_CONTROL = "Android Telecom audio-control request failed"
    const val CALL_CONTROL = "Android Telecom call-control request failed"
    const val OUTGOING_CALL = "Android Telecom outgoing-call request failed"
    const val PHONE_ACCOUNT_DISCOVERY = "Android Telecom phone-account discovery failed"
    const val ENDPOINT_ROUTING = "Android Telecom endpoint-routing request failed"
    const val ENDPOINT_CHANGE = "Android rejected the endpoint change"
}
