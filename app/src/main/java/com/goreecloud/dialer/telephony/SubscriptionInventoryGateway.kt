package com.goreecloud.dialer.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager

sealed interface SubscriptionInventoryResult {
    /** Android has granted the read authority and returned the currently active subscription IDs. */
    data class Available(val subscriptions: List<ActiveSubscription>) : SubscriptionInventoryResult

    /** The manifest declares the authority, but the user/runtime has not granted it. */
    data object PermissionRequired : SubscriptionInventoryResult

    /** This device/runtime cannot provide a subscription inventory through the accepted API. */
    data object Unsupported : SubscriptionInventoryResult

    /** Android rejected or failed the inventory read for a non-permission platform reason. */
    data class Unavailable(val reason: String) : SubscriptionInventoryResult
}

/**
 * Small platform seam so policy and result normalization can be exercised without granting phone
 * authority to JVM tests. Implementations must return IDs only; presentation metadata must not
 * become routing authority.
 */
internal interface SubscriptionInventoryPlatform {
    fun hasReadPhoneStatePermission(): Boolean
    fun activeSubscriptionIds(): PlatformSubscriptionRead
}

internal sealed interface PlatformSubscriptionRead {
    data class Available(val subscriptionIds: List<Int>) : PlatformSubscriptionRead
    data object Unsupported : PlatformSubscriptionRead
    data object PermissionDenied : PlatformSubscriptionRead
    data class Failed(val failureType: String) : PlatformSubscriptionRead
}

/**
 * Read-only, fail-closed gateway for the active carrier-subscription inventory.
 *
 * This boundary deliberately does not request runtime permission, infer a preferred route, mutate
 * Android subscription defaults, place calls, or expose subscription presentation identifiers.
 */
class SubscriptionInventoryGateway internal constructor(
    private val platform: SubscriptionInventoryPlatform,
) {
    constructor(context: Context) : this(AndroidSubscriptionInventoryPlatform(context.applicationContext))

    fun read(): SubscriptionInventoryResult {
        if (!platform.hasReadPhoneStatePermission()) {
            return SubscriptionInventoryResult.PermissionRequired
        }

        return when (val result = platform.activeSubscriptionIds()) {
            is PlatformSubscriptionRead.Available -> {
                val normalizedIds = result.subscriptionIds
                    .asSequence()
                    .filter { it != SubscriptionManager.INVALID_SUBSCRIPTION_ID }
                    .distinct()
                    .sorted()
                    .toList()
                SubscriptionInventoryResult.Available(
                    normalizedIds.map(::ActiveSubscription),
                )
            }
            PlatformSubscriptionRead.PermissionDenied -> SubscriptionInventoryResult.PermissionRequired
            PlatformSubscriptionRead.Unsupported -> SubscriptionInventoryResult.Unsupported
            is PlatformSubscriptionRead.Failed -> SubscriptionInventoryResult.Unavailable(result.failureType)
        }
    }
}

private class AndroidSubscriptionInventoryPlatform(
    private val context: Context,
) : SubscriptionInventoryPlatform {
    override fun hasReadPhoneStatePermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

    override fun activeSubscriptionIds(): PlatformSubscriptionRead {
        val manager = context.getSystemService(SubscriptionManager::class.java)
            ?: return PlatformSubscriptionRead.Unsupported

        return try {
            val ids = manager.activeSubscriptionInfoList
                .orEmpty()
                .map { it.subscriptionId }
            PlatformSubscriptionRead.Available(ids)
        } catch (_: SecurityException) {
            // Permission can be revoked between the explicit preflight and the platform read.
            PlatformSubscriptionRead.PermissionDenied
        } catch (exception: RuntimeException) {
            PlatformSubscriptionRead.Failed(exception::class.java.simpleName)
        }
    }
}
