package com.goreecloud.dialer.telephony

import com.goreecloud.dialer.core.capability.CapabilityState

/**
 * Pure mapping from Android platform facts to the Dialer capability model.
 *
 * Keeping this mapping Android-free makes role-state behavior deterministic in
 * unit tests and prevents the UI from inventing a broader "ready" state.
 */
object DialerRoleCapabilityResolver {
    fun resolve(
        hasTelephony: Boolean,
        roleManagerAvailable: Boolean,
        roleAvailable: Boolean,
        roleHeld: Boolean,
        roleName: String,
    ): CapabilityState {
        if (!hasTelephony) return CapabilityState.Unsupported
        if (!roleManagerAvailable) {
            return CapabilityState.Unavailable("Android RoleManager is unavailable")
        }
        if (!roleAvailable) {
            return CapabilityState.Unavailable("Android dialer role is unavailable")
        }
        return if (roleHeld) {
            CapabilityState.Active
        } else {
            CapabilityState.RoleRequired(roleName)
        }
    }
}
