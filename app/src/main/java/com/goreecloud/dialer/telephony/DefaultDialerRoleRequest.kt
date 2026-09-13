package com.goreecloud.dialer.telephony

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/** Runtime facts required before GoreeCloud may even prepare a ROLE_DIALER request. */
data class DefaultDialerRoleRequestFacts(
    val hasTelephony: Boolean,
    val roleManagerAvailable: Boolean,
    val roleAvailable: Boolean,
    val roleHeld: Boolean,
    val applicationRequirementsAccepted: Boolean,
    val acceptanceReason: String,
)

sealed interface DefaultDialerRoleRequestDecision {
    data object AlreadyHeld : DefaultDialerRoleRequestDecision
    data object Ready : DefaultDialerRoleRequestDecision
    data class Rejected(val reason: String) : DefaultDialerRoleRequestDecision
}

object DefaultDialerRoleRequestPolicy {
    fun decide(facts: DefaultDialerRoleRequestFacts): DefaultDialerRoleRequestDecision = when {
        !facts.hasTelephony -> DefaultDialerRoleRequestDecision.Rejected("Telephony is unavailable")
        !facts.roleManagerAvailable -> DefaultDialerRoleRequestDecision.Rejected(
            "Android RoleManager is unavailable",
        )
        !facts.roleAvailable -> DefaultDialerRoleRequestDecision.Rejected(
            "Android dialer role is unavailable",
        )
        facts.roleHeld -> DefaultDialerRoleRequestDecision.AlreadyHeld
        !facts.applicationRequirementsAccepted -> DefaultDialerRoleRequestDecision.Rejected(
            facts.acceptanceReason,
        )
        else -> DefaultDialerRoleRequestDecision.Ready
    }
}

sealed interface DefaultDialerRoleRequestPreparation {
    data object AlreadyHeld : DefaultDialerRoleRequestPreparation
    data class Prepared(val intent: Intent) : DefaultDialerRoleRequestPreparation
    data class Rejected(val reason: String) : DefaultDialerRoleRequestPreparation
}

/**
 * Prepares Android's user-consent role request only after GoreeCloud's independent acceptance gate
 * is satisfied. This class never starts an Activity and therefore cannot silently request the role.
 */
class AndroidDefaultDialerRoleRequestPreparer(
    private val context: Context,
) {
    fun prepare(): DefaultDialerRoleRequestPreparation {
        val packageManager = context.packageManager
        val roleManager = context.getSystemService(RoleManager::class.java)
        val acceptance = DevelopmentDefaultDialerAcceptance.current
        val facts = DefaultDialerRoleRequestFacts(
            hasTelephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY),
            roleManagerAvailable = roleManager != null,
            roleAvailable = roleManager?.isRoleAvailable(RoleManager.ROLE_DIALER) == true,
            roleHeld = roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true,
            applicationRequirementsAccepted = acceptance.accepted,
            acceptanceReason = acceptance.unavailableReason(),
        )

        return when (val decision = DefaultDialerRoleRequestPolicy.decide(facts)) {
            DefaultDialerRoleRequestDecision.AlreadyHeld ->
                DefaultDialerRoleRequestPreparation.AlreadyHeld

            DefaultDialerRoleRequestDecision.Ready ->
                DefaultDialerRoleRequestPreparation.Prepared(
                    roleManager!!.createRequestRoleIntent(RoleManager.ROLE_DIALER),
                )

            is DefaultDialerRoleRequestDecision.Rejected ->
                DefaultDialerRoleRequestPreparation.Rejected(decision.reason)
        }
    }
}
