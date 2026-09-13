package com.goreecloud.dialer.telephony

import android.os.Build
import android.telecom.Call
import androidx.annotation.RequiresApi

internal enum class CallStateReadSource {
    DETAILS,
    LEGACY_CALL,
}

internal object CallStateReadPolicy {
    fun sourceForSdk(sdkInt: Int): CallStateReadSource =
        if (sdkInt >= Build.VERSION_CODES.S) {
            CallStateReadSource.DETAILS
        } else {
            CallStateReadSource.LEGACY_CALL
        }
}

/**
 * Reads the current Telecom lifecycle state without spreading deprecated Call.state usage.
 *
 * Android 12 / API 31 added Call.Details.state as the replacement for Call.state. GoreeCloud
 * therefore prefers Call.Details on API 31+ and retains one isolated legacy fallback for the
 * supported Android 10-11 range (API 29-30). If Details is unexpectedly unavailable on a newer
 * runtime, the compatibility boundary falls back to the legacy value instead of inventing state.
 */
object AndroidCallStateReader {
    fun lifecycle(call: Call): CallLifecycleState {
        // Keep the SDK check visible at the NewApi call site so Android lint can prove this path
        // cannot execute on the supported API 29-30 legacy range.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val details = call.details
            if (details != null) {
                return lifecycleFromDetailsApi31(details)
            }
        }
        return lifecycleFromLegacyCall(call)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun lifecycleFromDetailsApi31(details: Call.Details): CallLifecycleState =
        CallLifecycleStateMapper.fromAndroid(details.state)

    @Suppress("DEPRECATION")
    private fun lifecycleFromLegacyCall(call: Call): CallLifecycleState =
        CallLifecycleStateMapper.fromAndroid(call.state)
}
