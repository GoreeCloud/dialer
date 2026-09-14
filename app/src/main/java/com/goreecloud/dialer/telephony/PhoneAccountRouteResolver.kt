package com.goreecloud.dialer.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

sealed interface PhoneAccountRouteResult {
    /** Exactly one enabled call-capable Telecom account maps to the requested subscription. */
    data class Available(val phoneAccountHandle: PhoneAccountHandle) : PhoneAccountRouteResult

    /** The accepted Android mapping API requires READ_PHONE_STATE and it is not currently granted. */
    data object PermissionRequired : PhoneAccountRouteResult

    /** This Android version/device cannot provide the accepted subscription-to-account mapping. */
    data object Unsupported : PhoneAccountRouteResult

    /** A safe unique mapping cannot be established from the current platform state. */
    data class Unavailable(val reason: String) : PhoneAccountRouteResult
}

internal data class PhoneAccountCandidate<T>(
    val handle: T,
    val subscriptionId: Int,
)

internal sealed interface PhoneAccountCandidateDecision<out T> {
    data class Resolved<T>(val handle: T) : PhoneAccountCandidateDecision<T>
    data object NoMatch : PhoneAccountCandidateDecision<Nothing>
    data object Ambiguous : PhoneAccountCandidateDecision<Nothing>
}

/** Pure exact-match policy so ambiguity handling is independently testable from Android Telecom. */
internal object PhoneAccountCandidatePolicy {
    fun <T> resolve(
        candidates: List<PhoneAccountCandidate<T>>,
        subscriptionId: Int,
    ): PhoneAccountCandidateDecision<T> {
        val matches = candidates
            .filter { it.subscriptionId == subscriptionId }
            .distinctBy { it.handle }

        return when (matches.size) {
            0 -> PhoneAccountCandidateDecision.NoMatch
            1 -> PhoneAccountCandidateDecision.Resolved(matches.single().handle)
            else -> PhoneAccountCandidateDecision.Ambiguous
        }
    }
}

/**
 * Read-only Android Telecom resolver for an already-selected active carrier subscription.
 *
 * This class does not place calls, change the user's outgoing account, alter subscription defaults,
 * infer a fallback route, or persist a PhoneAccountHandle. A future call-placement boundary must
 * re-read current subscription state and re-resolve the handle immediately before Telecom use.
 */
class PhoneAccountRouteResolver(
    private val context: Context,
) {
    fun resolve(subscriptionId: Int): PhoneAccountRouteResult {
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return PhoneAccountRouteResult.Unavailable("Invalid subscription identifier")
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // TelephonyManager.getSubscriptionId(PhoneAccountHandle) is API 30+.
            return PhoneAccountRouteResult.Unsupported
        }
        if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return PhoneAccountRouteResult.PermissionRequired
        }
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_SUBSCRIPTION)) {
            return PhoneAccountRouteResult.Unsupported
        }

        val telecomManager = context.getSystemService(TelecomManager::class.java)
            ?: return PhoneAccountRouteResult.Unsupported
        val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            ?: return PhoneAccountRouteResult.Unsupported

        return try {
            val candidates = telecomManager.callCapablePhoneAccounts.mapNotNull { handle ->
                val mappedSubscriptionId = telephonyManager.getSubscriptionId(handle)
                if (mappedSubscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    null
                } else {
                    PhoneAccountCandidate(
                        handle = handle,
                        subscriptionId = mappedSubscriptionId,
                    )
                }
            }

            when (val decision = PhoneAccountCandidatePolicy.resolve(candidates, subscriptionId)) {
                is PhoneAccountCandidateDecision.Resolved ->
                    PhoneAccountRouteResult.Available(decision.handle)
                PhoneAccountCandidateDecision.NoMatch ->
                    PhoneAccountRouteResult.Unavailable(
                        "No enabled call-capable Telecom account maps to the selected subscription",
                    )
                PhoneAccountCandidateDecision.Ambiguous ->
                    PhoneAccountRouteResult.Unavailable(
                        "Multiple enabled Telecom accounts map to the selected subscription",
                    )
            }
        } catch (_: SecurityException) {
            // Permission/default-dialer state may change between preflight and platform reads.
            PhoneAccountRouteResult.PermissionRequired
        } catch (exception: RuntimeException) {
            PhoneAccountRouteResult.Unavailable(exception::class.java.simpleName)
        }
    }
}
