package com.goreecloud.dialer.telephony

import android.telecom.Call

/**
 * Content-minimized control capability evidence derived from Android Telecom.
 *
 * These booleans intentionally exclude phone numbers, account identities, participant labels,
 * carrier metadata, and the underlying Call.Details object.
 */
data class CallControlCapabilities(
    val holdSupported: Boolean = false,
    val holdCurrentlyAvailable: Boolean = false,
    val muteSupported: Boolean = false,
    val manageConferenceSupported: Boolean = false,
    val mergeConferenceAvailable: Boolean = false,
    val swapConferenceAvailable: Boolean = false,
    val separateFromConferenceAvailable: Boolean = false,
)

object AndroidCallControlCapabilities {
    fun from(details: Call.Details?): CallControlCapabilities {
        if (details == null) return CallControlCapabilities()

        return CallControlCapabilities(
            holdSupported = details.can(Call.Details.CAPABILITY_SUPPORT_HOLD),
            holdCurrentlyAvailable = details.can(Call.Details.CAPABILITY_HOLD),
            muteSupported = details.can(Call.Details.CAPABILITY_MUTE),
            manageConferenceSupported = details.can(Call.Details.CAPABILITY_MANAGE_CONFERENCE),
            mergeConferenceAvailable = details.can(Call.Details.CAPABILITY_MERGE_CONFERENCE),
            swapConferenceAvailable = details.can(Call.Details.CAPABILITY_SWAP_CONFERENCE),
            separateFromConferenceAvailable = details.can(
                Call.Details.CAPABILITY_SEPARATE_FROM_CONFERENCE,
            ),
        )
    }
}
