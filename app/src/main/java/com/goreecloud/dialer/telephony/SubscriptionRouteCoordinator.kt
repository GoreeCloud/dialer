package com.goreecloud.dialer.telephony

sealed interface SubscriptionRouteReadiness {
    data object PermissionRequired : SubscriptionRouteReadiness
    data object Unsupported : SubscriptionRouteReadiness
    data class Unavailable(val reason: String) : SubscriptionRouteReadiness
    data class Decision(val decision: SubscriptionRouteDecision) : SubscriptionRouteReadiness
}

/**
 * Connects read-only Android subscription inventory to the pure route policy without gaining call
 * placement, runtime-permission, default-SIM mutation, or emergency-routing authority.
 */
object SubscriptionRouteCoordinator {
    fun evaluate(
        inventory: SubscriptionInventoryResult,
        explicitlySelectedSubscriptionId: Int?,
        isEmergencyCall: Boolean,
    ): SubscriptionRouteReadiness = when (inventory) {
        SubscriptionInventoryResult.PermissionRequired -> SubscriptionRouteReadiness.PermissionRequired
        SubscriptionInventoryResult.Unsupported -> SubscriptionRouteReadiness.Unsupported
        is SubscriptionInventoryResult.Unavailable ->
            SubscriptionRouteReadiness.Unavailable(inventory.reason)
        is SubscriptionInventoryResult.Available -> SubscriptionRouteReadiness.Decision(
            SubscriptionRoutePolicy.decide(
                activeSubscriptions = inventory.subscriptions,
                explicitlySelectedSubscriptionId = explicitlySelectedSubscriptionId,
                isEmergencyCall = isEmergencyCall,
            ),
        )
    }
}
