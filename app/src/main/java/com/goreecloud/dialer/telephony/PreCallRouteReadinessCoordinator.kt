package com.goreecloud.dialer.telephony

import android.content.Context
import android.telecom.PhoneAccountHandle

sealed interface PreCallRouteReadiness {
    /** Non-emergency route prerequisites currently agree on one exact active subscription/account. */
    data class Ready(
        val subscriptionId: Int,
        val phoneAccountHandle: PhoneAccountHandle,
    ) : PreCallRouteReadiness

    /** Emergency routing remains entirely owned by Android Telecom. */
    data object DeferEmergencyToPlatform : PreCallRouteReadiness

    /** The user/runtime must grant the already-declared phone-state authority. */
    data object PermissionRequired : PreCallRouteReadiness

    /** The current Android runtime/device cannot establish the accepted route mapping. */
    data object Unsupported : PreCallRouteReadiness

    /** Multiple active subscriptions exist and the user must choose one explicitly. */
    data class RequiresUserSelection(val subscriptionIds: List<Int>) : PreCallRouteReadiness

    /** Current state cannot prove one safe route. */
    data class Unavailable(val reason: String) : PreCallRouteReadiness
}

internal sealed interface PhoneAccountProjection<out T> {
    data class Available<T>(val account: T) : PhoneAccountProjection<T>
    data object PermissionRequired : PhoneAccountProjection<Nothing>
    data object Unsupported : PhoneAccountProjection<Nothing>
    data class Unavailable(val reason: String) : PhoneAccountProjection<Nothing>
}

internal sealed interface PreCallRouteDecision<out T> {
    data class Ready<T>(
        val subscriptionId: Int,
        val account: T,
    ) : PreCallRouteDecision<T>

    data object DeferEmergencyToPlatform : PreCallRouteDecision<Nothing>
    data object PermissionRequired : PreCallRouteDecision<Nothing>
    data object Unsupported : PreCallRouteDecision<Nothing>
    data class RequiresUserSelection(val subscriptionIds: List<Int>) : PreCallRouteDecision<Nothing>
    data class Unavailable(val reason: String) : PreCallRouteDecision<Nothing>
}

/**
 * Pure composition policy for inventory -> explicit multi-SIM policy -> exact Telecom account.
 *
 * The resolver is invoked only after the current inventory and route policy authorize one exact
 * subscription. Emergency evaluation never invokes inventory-derived account resolution.
 */
internal object PreCallRouteEvaluationPolicy {
    fun <T> evaluate(
        inventory: SubscriptionInventoryResult,
        explicitlySelectedSubscriptionId: Int?,
        isEmergencyCall: Boolean,
        resolvePhoneAccount: (Int) -> PhoneAccountProjection<T>,
    ): PreCallRouteDecision<T> {
        if (isEmergencyCall) return PreCallRouteDecision.DeferEmergencyToPlatform

        val subscriptions = when (inventory) {
            SubscriptionInventoryResult.PermissionRequired ->
                return PreCallRouteDecision.PermissionRequired
            SubscriptionInventoryResult.Unsupported ->
                return PreCallRouteDecision.Unsupported
            is SubscriptionInventoryResult.Unavailable ->
                return PreCallRouteDecision.Unavailable(inventory.reason)
            is SubscriptionInventoryResult.Available -> inventory.subscriptions
        }

        return when (
            val route = SubscriptionRoutePolicy.decide(
                activeSubscriptions = subscriptions,
                explicitlySelectedSubscriptionId = explicitlySelectedSubscriptionId,
                isEmergencyCall = false,
            )
        ) {
            SubscriptionRouteDecision.DeferEmergencyToPlatform ->
                PreCallRouteDecision.DeferEmergencyToPlatform
            is SubscriptionRouteDecision.RequiresUserSelection ->
                PreCallRouteDecision.RequiresUserSelection(route.subscriptionIds)
            is SubscriptionRouteDecision.Unavailable ->
                PreCallRouteDecision.Unavailable(route.reason)
            is SubscriptionRouteDecision.UseSubscription -> {
                when (val account = resolvePhoneAccount(route.subscriptionId)) {
                    is PhoneAccountProjection.Available ->
                        PreCallRouteDecision.Ready(
                            subscriptionId = route.subscriptionId,
                            account = account.account,
                        )
                    PhoneAccountProjection.PermissionRequired ->
                        PreCallRouteDecision.PermissionRequired
                    PhoneAccountProjection.Unsupported ->
                        PreCallRouteDecision.Unsupported
                    is PhoneAccountProjection.Unavailable ->
                        PreCallRouteDecision.Unavailable(account.reason)
                }
            }
        }
    }
}

/**
 * Read-only pre-call readiness boundary.
 *
 * Every evaluation re-reads the current active subscription inventory, re-runs the independent
 * multi-SIM policy, and resolves the selected subscription to one enabled Telecom account. It does
 * not place a call, persist the returned PhoneAccountHandle, mutate a default route, request the
 * default-dialer role, or override emergency routing.
 */
class PreCallRouteReadinessCoordinator(
    private val inventoryGateway: SubscriptionInventoryGateway,
    private val phoneAccountRouteResolver: PhoneAccountRouteResolver,
) {
    constructor(context: Context) : this(
        inventoryGateway = SubscriptionInventoryGateway(context.applicationContext),
        phoneAccountRouteResolver = PhoneAccountRouteResolver(context.applicationContext),
    )

    fun evaluate(
        explicitlySelectedSubscriptionId: Int?,
        isEmergencyCall: Boolean,
    ): PreCallRouteReadiness {
        val decision = PreCallRouteEvaluationPolicy.evaluate(
            inventory = inventoryGateway.read(),
            explicitlySelectedSubscriptionId = explicitlySelectedSubscriptionId,
            isEmergencyCall = isEmergencyCall,
            resolvePhoneAccount = { subscriptionId ->
                phoneAccountRouteResolver.resolve(subscriptionId).toProjection()
            },
        )

        return when (decision) {
            is PreCallRouteDecision.Ready ->
                PreCallRouteReadiness.Ready(
                    subscriptionId = decision.subscriptionId,
                    phoneAccountHandle = decision.account,
                )
            PreCallRouteDecision.DeferEmergencyToPlatform ->
                PreCallRouteReadiness.DeferEmergencyToPlatform
            PreCallRouteDecision.PermissionRequired ->
                PreCallRouteReadiness.PermissionRequired
            PreCallRouteDecision.Unsupported ->
                PreCallRouteReadiness.Unsupported
            is PreCallRouteDecision.RequiresUserSelection ->
                PreCallRouteReadiness.RequiresUserSelection(decision.subscriptionIds)
            is PreCallRouteDecision.Unavailable ->
                PreCallRouteReadiness.Unavailable(decision.reason)
        }
    }
}

private fun PhoneAccountRouteResult.toProjection(): PhoneAccountProjection<PhoneAccountHandle> =
    when (this) {
        is PhoneAccountRouteResult.Available -> PhoneAccountProjection.Available(phoneAccountHandle)
        PhoneAccountRouteResult.PermissionRequired -> PhoneAccountProjection.PermissionRequired
        PhoneAccountRouteResult.Unsupported -> PhoneAccountProjection.Unsupported
        is PhoneAccountRouteResult.Unavailable -> PhoneAccountProjection.Unavailable(reason)
    }
