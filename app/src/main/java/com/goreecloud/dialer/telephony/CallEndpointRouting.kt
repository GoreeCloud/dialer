package com.goreecloud.dialer.telephony

enum class CallEndpointKind {
    EARPIECE,
    BLUETOOTH,
    WIRED_HEADSET,
    SPEAKER,
    STREAMING,
    UNKNOWN,
}

data class CallEndpointRuntimeSummary(
    val routeId: Long,
    val kind: CallEndpointKind,
)

enum class CallEndpointRequestState {
    SUBMITTED,
    SUCCEEDED,
    FAILED,
}

data class CallEndpointRequestEvidence(
    val routeId: Long,
    val state: CallEndpointRequestState,
    val reason: String? = null,
)

sealed interface CallEndpointRoutingResult {
    data object Submitted : CallEndpointRoutingResult
    data class Rejected(val reason: String) : CallEndpointRoutingResult
    data class Failed(val reason: String) : CallEndpointRoutingResult
}

interface CallEndpointRoutingTarget {
    fun request(routeId: Long): CallEndpointRoutingResult
}

/**
 * Process-local bridge to the currently bound API 34+ Telecom endpoint authority.
 * No device endpoint name or Bluetooth identity is exposed through this bridge.
 */
object InCallEndpointRoutingRuntime {
    private var target: CallEndpointRoutingTarget? = null

    @Synchronized
    fun attach(target: CallEndpointRoutingTarget) {
        this.target = target
    }

    @Synchronized
    fun detach(target: CallEndpointRoutingTarget) {
        if (this.target === target) this.target = null
    }

    @Synchronized
    fun request(routeId: Long): CallEndpointRoutingResult {
        val activeTarget = target
            ?: return CallEndpointRoutingResult.Rejected(
                "Call endpoint routing is unavailable on this runtime",
            )
        return activeTarget.request(routeId)
    }
}
