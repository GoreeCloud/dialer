package com.goreecloud.dialer.telephony

import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import com.goreecloud.dialer.core.capability.CapabilityState

/** Reads platform facts without requesting roles or permissions. */
class AndroidTelephonyCapabilityProbe(
    private val context: Context,
) {
    fun snapshot(): TelephonyCapabilitySnapshot {
        val packageManager = context.packageManager
        val hasTelephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        val roleManager = context.getSystemService(RoleManager::class.java)
        val applicationAcceptance = DevelopmentDefaultDialerAcceptance.current

        val defaultDialerRole = DialerRoleCapabilityResolver.resolve(
            hasTelephony = hasTelephony,
            roleManagerAvailable = roleManager != null,
            roleAvailable = roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true,
            roleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true,
            roleName = RoleManager.ROLE_DIALER,
            applicationRequirementsAccepted = applicationAcceptance.accepted,
            requirementsReason = applicationAcceptance.unavailableReason(),
        )

        fun unavailable(reason: String): CapabilityState =
            if (hasTelephony) CapabilityState.Unavailable(reason) else CapabilityState.Unsupported

        return TelephonyCapabilitySnapshot(
            defaultDialerRole = defaultDialerRole,
            dialIntentHandling = CapabilityState.Available,
            outgoingCalls = unavailable(
                "TelecomManager placement boundary exists; carrier call placement runtime acceptance is incomplete",
            ),
            incomingCalls = unavailable(
                "InCallService lifecycle exists; production incoming-call presentation is not accepted",
            ),
            inCallControls = unavailable(
                "Development controls exist; production in-call UI acceptance is incomplete",
            ),
            multiSimRouting = unavailable(
                "Call-capable phone-account discovery and explicit routing exist; production multi-SIM acceptance is incomplete",
            ),
            wifiCallingState = unavailable(
                "Precise Wi-Fi Calling/IMS state requires authorized precise-telephony or carrier access; this Development build does not request or infer it",
            ),
            supplementaryServices = unavailable(
                "Supplementary-service state is exposed only when Android provides an authorized platform signal; no accepted observer exists in this Development build",
            ),
            callScreening = unavailable("Call screening service not implemented"),
            visualVoicemail = unavailable("Voicemail adapter not implemented"),
            callRecording = unavailable("Recording capability not implemented"),
        )
    }
}
