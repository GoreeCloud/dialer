package com.goreecloud.dialer.telephony

import android.telecom.Call
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-local call runtime authority.
 *
 * The store retains Android Call objects only while Telecom owns the live call so explicit
 * controls can be executed. Public snapshots expose only generated session/route IDs, lifecycle
 * categories, aggregate state, endpoint categories, and content-free audio-control state. No
 * number, caller name, account identifier, endpoint device name, Call.Details, transcript,
 * recording, or audio is persisted or projected.
 */
data class CallRuntimeSummary(
    val sessionId: Long,
    val state: CallLifecycleState,
)

data class InCallRuntimeSnapshot(
    val trackedCallCount: Int = 0,
    val calls: List<CallRuntimeSummary> = emptyList(),
    val stateCounts: Map<CallLifecycleState, Int> = emptyMap(),
    val canAddCall: Boolean? = null,
    val isMuted: Boolean? = null,
    val endpointRoutingSupported: Boolean = false,
    val availableEndpoints: List<CallEndpointRuntimeSummary> = emptyList(),
    val currentEndpointId: Long? = null,
    val lastEndpointRequest: CallEndpointRequestEvidence? = null,
)

object InCallRuntimeStore {
    private data class TrackedCall(
        val sessionId: Long,
        val call: Call,
        var state: CallLifecycleState,
    )

    private val nextSessionId = AtomicLong(1)
    private val trackedByCall = IdentityHashMap<Call, TrackedCall>()
    private val trackedById = linkedMapOf<Long, TrackedCall>()
    private var canAddCall: Boolean? = null
    private var isMuted: Boolean? = null
    private var endpointRoutingSupported: Boolean = false
    private var availableEndpoints: List<CallEndpointRuntimeSummary> = emptyList()
    private var currentEndpointId: Long? = null
    private var lastEndpointRequest: CallEndpointRequestEvidence? = null

    private val mutableSnapshots = MutableStateFlow(InCallRuntimeSnapshot())
    val snapshots: StateFlow<InCallRuntimeSnapshot> = mutableSnapshots.asStateFlow()

    val snapshot: InCallRuntimeSnapshot
        get() = mutableSnapshots.value

    @Synchronized
    fun onCallAdded(call: Call): Long {
        trackedByCall[call]?.let { return it.sessionId }
        val tracked = TrackedCall(
            sessionId = nextSessionId.getAndIncrement(),
            call = call,
            state = CallLifecycleStateMapper.fromAndroid(call.state),
        )
        trackedByCall[call] = tracked
        trackedById[tracked.sessionId] = tracked
        publish()
        return tracked.sessionId
    }

    @Synchronized
    fun onCallStateChanged(call: Call, state: Int) {
        val tracked = trackedByCall[call] ?: return
        tracked.state = CallLifecycleStateMapper.fromAndroid(state)
        publish()
    }

    @Synchronized
    fun onCallRemoved(call: Call) {
        val tracked = trackedByCall.remove(call) ?: return
        trackedById.remove(tracked.sessionId)
        publish()
    }

    @Synchronized
    fun onCanAddCallChanged(value: Boolean) {
        canAddCall = value
        publish()
    }

    @Synchronized
    fun onMuteStateChanged(value: Boolean) {
        isMuted = value
        publish()
    }

    @Synchronized
    fun onAvailableEndpointsChanged(
        supported: Boolean,
        endpoints: List<CallEndpointRuntimeSummary>,
    ) {
        endpointRoutingSupported = supported
        availableEndpoints = endpoints
        if (currentEndpointId !in endpoints.map { it.routeId }) {
            currentEndpointId = null
        }
        publish()
    }

    @Synchronized
    fun onCurrentEndpointChanged(routeId: Long) {
        currentEndpointId = routeId
        publish()
    }

    @Synchronized
    fun onEndpointRequestChanged(evidence: CallEndpointRequestEvidence) {
        lastEndpointRequest = evidence
        publish()
    }

    @Synchronized
    fun execute(sessionId: Long, action: CallControlAction): CallControlResult {
        val tracked = trackedById[sessionId]
            ?: return CallControlResult.Rejected("Call session is no longer active")
        return CallControlEngine(AndroidCallControlTarget(tracked.call)).execute(action)
    }

    @Synchronized
    fun clear() {
        trackedByCall.clear()
        trackedById.clear()
        canAddCall = null
        isMuted = null
        endpointRoutingSupported = false
        availableEndpoints = emptyList()
        currentEndpointId = null
        lastEndpointRequest = null
        publish()
    }

    private fun publish() {
        val summaries = trackedById.values.map {
            CallRuntimeSummary(sessionId = it.sessionId, state = it.state)
        }
        mutableSnapshots.value = InCallRuntimeSnapshot(
            trackedCallCount = summaries.size,
            calls = summaries,
            stateCounts = summaries.groupingBy { it.state }.eachCount(),
            canAddCall = canAddCall,
            isMuted = isMuted,
            endpointRoutingSupported = endpointRoutingSupported,
            availableEndpoints = availableEndpoints,
            currentEndpointId = currentEndpointId,
            lastEndpointRequest = lastEndpointRequest,
        )
    }
}
