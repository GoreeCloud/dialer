package com.goreecloud.dialer.telephony

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat

/** Runtime facts required before GoreeCloud may submit a managed carrier call to Telecom. */
data class OutgoingCallPlacementFacts(
    val hasTelephony: Boolean,
    val telecomManagerAvailable: Boolean,
    val defaultDialerRoleHeld: Boolean,
    val callPhonePermissionGranted: Boolean,
    val placementPathAccepted: Boolean,
)

sealed interface OutgoingCallPlacementDecision {
    data object Allowed : OutgoingCallPlacementDecision
    data class Rejected(val reason: String) : OutgoingCallPlacementDecision
}

object OutgoingCallPlacementPolicy {
    fun decide(facts: OutgoingCallPlacementFacts): OutgoingCallPlacementDecision = when {
        !facts.hasTelephony -> OutgoingCallPlacementDecision.Rejected("Telephony is unavailable")
        !facts.telecomManagerAvailable -> OutgoingCallPlacementDecision.Rejected("Android TelecomManager is unavailable")
        !facts.placementPathAccepted -> OutgoingCallPlacementDecision.Rejected("Outgoing call placement has not passed GoreeCloud runtime acceptance")
        !facts.defaultDialerRoleHeld -> OutgoingCallPlacementDecision.Rejected("GoreeCloud Dialer is not the active default dialer")
        !facts.callPhonePermissionGranted -> OutgoingCallPlacementDecision.Rejected("CALL_PHONE permission is not granted")
        else -> OutgoingCallPlacementDecision.Allowed
    }
}

sealed interface OutgoingCallPlacementResult {
    data object Submitted : OutgoingCallPlacementResult
    data class Rejected(val reason: String) : OutgoingCallPlacementResult
    data class Failed(val reason: String) : OutgoingCallPlacementResult
}

/**
 * Dormant Android Telecom placement boundary.
 *
 * The current Development acceptance gate intentionally keeps this path rejected. The adapter
 * exists so call placement can be validated without introducing ACTION_CALL fallbacks or silently
 * bypassing Android's default-dialer, permission, phone-account, or emergency-routing boundaries.
 */
class AndroidOutgoingCallPlacer(
    private val context: Context,
) {
    fun place(
        number: String,
        requestedPhoneAccountRouteId: Long? = null,
    ): OutgoingCallPlacementResult {
        val sanitizedNumber = number.trim()
        if (sanitizedNumber.isEmpty()) {
            return OutgoingCallPlacementResult.Rejected("Phone number is empty")
        }

        val packageManager = context.packageManager
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val roleManager = context.getSystemService(RoleManager::class.java)
        val acceptance = DevelopmentDefaultDialerAcceptance.current
        val facts = OutgoingCallPlacementFacts(
            hasTelephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CALLING),
            telecomManagerAvailable = telecomManager != null,
            defaultDialerRoleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true,
            callPhonePermissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE,
            ) == PackageManager.PERMISSION_GRANTED,
            placementPathAccepted = acceptance.outgoingCallPlacementAccepted,
        )

        return when (val decision = OutgoingCallPlacementPolicy.decide(facts)) {
            is OutgoingCallPlacementDecision.Rejected ->
                OutgoingCallPlacementResult.Rejected(decision.reason)

            OutgoingCallPlacementDecision.Allowed -> {
                val emergencyClassification = AndroidEmergencyNumberClassifier(context)
                    .classify(sanitizedNumber)
                val phoneAccountDecision = OutgoingPhoneAccountRoutingPolicy.decide(
                    emergencyClassification = emergencyClassification,
                    availableRouteIds = PhoneAccountRoutingRuntime.availableRouteIds(),
                    requestedRouteId = requestedPhoneAccountRouteId,
                )

                val extras = when (phoneAccountDecision) {
                    PhoneAccountSelectionDecision.SystemDefault -> Bundle.EMPTY
                    is PhoneAccountSelectionDecision.Rejected ->
                        return OutgoingCallPlacementResult.Rejected(phoneAccountDecision.reason)
                    is PhoneAccountSelectionDecision.Explicit -> {
                        val phoneAccountHandle = PhoneAccountRoutingRuntime.resolve(
                            phoneAccountDecision.routeId,
                        ) ?: return OutgoingCallPlacementResult.Rejected(
                            "Selected phone account is no longer available",
                        )
                        Bundle().apply {
                            putParcelable(
                                TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                                phoneAccountHandle,
                            )
                        }
                    }
                }

                try {
                    telecomManager!!.placeCall(
                        Uri.fromParts("tel", sanitizedNumber, null),
                        extras,
                    )
                    OutgoingCallPlacementResult.Submitted
                } catch (securityException: SecurityException) {
                    OutgoingCallPlacementResult.Failed(
                        "Android rejected call placement authorization",
                    )
                } catch (_: RuntimeException) {
                    OutgoingCallPlacementResult.Failed(
                        TelephonyFailurePresentation.OUTGOING_CALL,
                    )
                }
            }
        }
    }
}
