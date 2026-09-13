package com.goreecloud.dialer.core.capability

/**
 * Runtime capability state. This intentionally separates support, authorization,
 * activation, and operation outcome so the UI cannot collapse them into one flag.
 */
sealed interface CapabilityState {
    data object Unsupported : CapabilityState
    data class Unavailable(val reason: String) : CapabilityState
    data class PermissionRequired(val permission: String) : CapabilityState
    data class RoleRequired(val role: String) : CapabilityState
    data object Available : CapabilityState
    data object Active : CapabilityState
    data class Failed(val reason: String) : CapabilityState
    data object Succeeded : CapabilityState
}
