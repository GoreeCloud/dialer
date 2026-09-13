package com.goreecloud.dialer.telephony

import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager

enum class EmergencyNumberClassification {
    EMERGENCY,
    NON_EMERGENCY,
    UNKNOWN,
}

/**
 * Emergency calls and indeterminate emergency status always delegate account choice to Android.
 * A user-selected GoreeCloud phone-account route is applied only after Android classifies the
 * number as non-emergency and the selected route is still available.
 */
object OutgoingPhoneAccountRoutingPolicy {
    fun decide(
        emergencyClassification: EmergencyNumberClassification,
        availableRouteIds: Set<Long>,
        requestedRouteId: Long?,
    ): PhoneAccountSelectionDecision = when (emergencyClassification) {
        EmergencyNumberClassification.EMERGENCY,
        EmergencyNumberClassification.UNKNOWN,
        -> PhoneAccountSelectionDecision.SystemDefault

        EmergencyNumberClassification.NON_EMERGENCY ->
            PhoneAccountSelectionPolicy.decide(availableRouteIds, requestedRouteId)
    }
}

class AndroidEmergencyNumberClassifier(
    private val context: Context,
) {
    fun classify(number: String): EmergencyNumberClassification {
        if (number.isBlank()) return EmergencyNumberClassification.UNKNOWN
        if (
            !context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CALLING)
        ) {
            return EmergencyNumberClassification.UNKNOWN
        }

        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            ?: return EmergencyNumberClassification.UNKNOWN

        return try {
            if (telephonyManager.isEmergencyNumber(number)) {
                EmergencyNumberClassification.EMERGENCY
            } else {
                EmergencyNumberClassification.NON_EMERGENCY
            }
        } catch (runtimeException: RuntimeException) {
            EmergencyNumberClassification.UNKNOWN
        }
    }
}
