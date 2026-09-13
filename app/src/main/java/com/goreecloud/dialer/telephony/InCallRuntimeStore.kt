package com.goreecloud.dialer.telephony

import android.telecom.Call
import java.util.IdentityHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Process-local call runtime authority.
 *
 * The store retains Android Call objects only while Telecom owns the live call so explicit
 * controls can be executed. Public snapshots expose only generated session IDs, lifecycle
 * categories, and aggregate state. No number, caller name, account identifier, Call.Details,
 * transcript, recording, or audio is persisted or projected.
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

    @Volatile
    var snapshot: InCallRuntimeSnapshot = InCallRuntimeSnapshot()
        private set

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
        publish()
    }

    private fun publish() {
        val summaries = trackedById.values.map {
            CallRuntimeSummary(sessionId = it.sessionId, state = it.state)
        }
        snapshot = InCallRuntimeSnapshot(
            trackedCallCount = summaries.size,
            calls = summaries,
            stateCounts = summaries.groupingBy { it.state }.eachCount(),
            canAddCall = canAddCall,
        )
    }
}
