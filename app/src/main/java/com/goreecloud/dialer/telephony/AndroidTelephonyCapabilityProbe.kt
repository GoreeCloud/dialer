package com.goreecloud.dialer.telephony

import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import com.goreecloud.dialer.core.capability.CapabilityState

/**
 * Reads platform facts without requesting roles or permissions.
 *
 * GoreeCloud Dialer intentionally does not expose a ROLE_DIALER request yet:
 * Android requires an ACTION_DIAL activity plus a complete InCallService with
 * incoming and ongoing call UI before the app should ask to become default.
 */
class AndroidTelephonyCapabilityProbe(
    private val context: Context,
) {
    fun snapshot(): TelephonyCapabilitySnapshot {
        val packageManager = context.packageManager
        val hasTelephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        val roleManager = context.getSystemService(RoleManager::class.java)

        val defaultDialerRole = DialerRoleCapabilityResolver.resolve(
            hasTelephony = hasTelephony,
            roleManagerAvailable = roleManager != null,
            roleAvailable = roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true,
            roleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true,
            roleName = RoleManager.ROLE_DIALER,
        )

        fun notImplemented(reason: String): CapabilityState =
            if (hasTelephony) CapabilityState.Unavailable(reason) else CapabilityState.Unsupported

        return TelephonyCapabilitySnapshot(
            defaultDialerRole = defaultDialerRole,
            outgoingCalls = notImplemented("Carrier call integration not implemented"),
            incomingCalls = notImplemented("InCallService integration not implemented"),
            inCallControls = notImplemented("In-call controls not implemented"),
            multiSimRouting = notImplemented("Subscription routing not implemented"),
            callScreening = notImplemented("Call screening service not implemented"),
            visualVoicemail = notImplemented("Voicemail adapter not implemented"),
            callRecording = notImplemented("Recording capability not implemented"),
        )
    }
}
