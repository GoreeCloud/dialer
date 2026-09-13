package com.goreecloud.dialer.telephony

import com.goreecloud.dialer.core.capability.CapabilityState

/** Pure mapping from Android/platform facts to the Dialer capability model. */
object DialerRoleCapabilityResolver {
    fun resolve(
        hasTelephony: Boolean,
        roleManagerAvailable: Boolean,
        roleAvailable: Boolean,
        roleHeld: Boolean,
        roleName: String,
        applicationRequirementsAccepted: Boolean,
        requirementsReason: String,
    ): CapabilityState {
        if (!hasTelephony) return CapabilityState.Unsupported
        if (!roleManagerAvailable) {
            return CapabilityState.Unavailable("Android RoleManager is unavailable")
        }
        if (!roleAvailable) {
            return CapabilityState.Unavailable("Android dialer role is unavailable")
        }
        if (roleHeld) return CapabilityState.Active
        if (!applicationRequirementsAccepted) {
            return CapabilityState.Unavailable(requirementsReason)
        }
        return CapabilityState.RoleRequired(roleName)
    }
}
