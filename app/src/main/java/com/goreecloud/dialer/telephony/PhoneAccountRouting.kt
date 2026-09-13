package com.goreecloud.dialer.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicLong

data class PhoneAccountRouteSummary(
    val routeId: Long,
    val isSystemDefault: Boolean,
)

sealed interface PhoneAccountDiscoveryState {
    data object Unsupported : PhoneAccountDiscoveryState
    data object TelecomUnavailable : PhoneAccountDiscoveryState
    data class PermissionRequired(val permission: String) : PhoneAccountDiscoveryState
    data class Available(
        val routes: List<PhoneAccountRouteSummary>,
        val systemDefaultRouteId: Long?,
    ) : PhoneAccountDiscoveryState
    data class Failed(val reason: String) : PhoneAccountDiscoveryState
}

sealed interface PhoneAccountSelectionDecision {
    data object SystemDefault : PhoneAccountSelectionDecision
    data class Explicit(val routeId: Long) : PhoneAccountSelectionDecision
    data class Rejected(val reason: String) : PhoneAccountSelectionDecision
}

object PhoneAccountSelectionPolicy {
    fun decide(
        availableRouteIds: Set<Long>,
        requestedRouteId: Long?,
    ): PhoneAccountSelectionDecision {
        if (requestedRouteId == null) return PhoneAccountSelectionDecision.SystemDefault
        if (requestedRouteId !in availableRouteIds) {
            return PhoneAccountSelectionDecision.Rejected(
                "Selected phone account is no longer available",
            )
        }
        return PhoneAccountSelectionDecision.Explicit(requestedRouteId)
    }
}

/**
 * Process-local authority for Android call-capable PhoneAccountHandle values.
 *
 * Public state intentionally exposes only generated route IDs and whether Android reports a route
 * as the current default. PhoneAccountHandle ids, component names, labels, phone numbers, ICCIDs,
 * carrier names, and subscription identifiers are not projected by this boundary.
 *
 * Permission/platform loss revokes the process-local handle authority immediately. UI may retain
 * an opaque saved route ID, but that ID can no longer resolve to a carrier account until discovery
 * is authorized again and Android reports the account as call-capable.
 */
object PhoneAccountRoutingRuntime {
    private val nextRouteId = AtomicLong(1)
    private val routeIdsByHandle = linkedMapOf<PhoneAccountHandle, Long>()
    private val handlesByRouteId = linkedMapOf<Long, PhoneAccountHandle>()

    @Synchronized
    fun discover(context: Context): PhoneAccountDiscoveryState {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            clearAuthority()
            return PhoneAccountDiscoveryState.Unsupported
        }
        if (
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            clearAuthority()
            return PhoneAccountDiscoveryState.PermissionRequired(Manifest.permission.READ_PHONE_STATE)
        }

        val telecomManager = context.getSystemService(TelecomManager::class.java)
        if (telecomManager == null) {
            clearAuthority()
            return PhoneAccountDiscoveryState.TelecomUnavailable
        }

        return try {
            val handles = telecomManager.callCapablePhoneAccounts
            val activeHandles = handles.toSet()
            routeIdsByHandle.keys.filterNot { it in activeHandles }.forEach { staleHandle ->
                routeIdsByHandle.remove(staleHandle)?.let(handlesByRouteId::remove)
            }

            val defaultHandle = telecomManager.getDefaultOutgoingPhoneAccount("tel")
            val routes = handles.map { handle ->
                val routeId = routeIdsByHandle.getOrPut(handle) {
                    nextRouteId.getAndIncrement().also { handlesByRouteId[it] = handle }
                }
                handlesByRouteId[routeId] = handle
                PhoneAccountRouteSummary(
                    routeId = routeId,
                    isSystemDefault = handle == defaultHandle,
                )
            }

            PhoneAccountDiscoveryState.Available(
                routes = routes,
                systemDefaultRouteId = routes.firstOrNull { it.isSystemDefault }?.routeId,
            )
        } catch (securityException: SecurityException) {
            clearAuthority()
            PhoneAccountDiscoveryState.PermissionRequired(Manifest.permission.READ_PHONE_STATE)
        } catch (runtimeException: RuntimeException) {
            clearAuthority()
            PhoneAccountDiscoveryState.Failed(
                runtimeException.message?.takeIf { it.isNotBlank() }
                    ?: runtimeException::class.java.simpleName,
            )
        }
    }

    @Synchronized
    fun availableRouteIds(): Set<Long> = handlesByRouteId.keys.toSet()

    @Synchronized
    fun resolve(routeId: Long): PhoneAccountHandle? = handlesByRouteId[routeId]

    @Synchronized
    fun clear() {
        clearAuthority()
    }

    private fun clearAuthority() {
        routeIdsByHandle.clear()
        handlesByRouteId.clear()
    }
}
