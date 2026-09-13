package com.goreecloud.dialer.telephony

import android.telecom.Call
import java.util.IdentityHashMap

/**
 * Process-local, content-free view of call lifecycle state.
 *
 * It deliberately stores no phone number, caller name, transcript, recording, account,
 * or call details. Durable call history belongs to a later Privacy Shield-authorized layer.
 */
data class InCallRuntimeSnapshot(
    val trackedCallCount: Int = 0,
    val stateCounts: Map<CallLifecycleState, Int> = emptyMap(),
    val canAddCall: Boolean? = null,
)

object InCallRuntimeStore {
    private val states = IdentityHashMap<Call, CallLifecycleState>()
    private var canAddCall: Boolean? = null

    @Volatile
    var snapshot: InCallRuntimeSnapshot = InCallRuntimeSnapshot()
        private set

    @Synchronized
    fun onCallAdded(call: Call) {
        states[call] = CallLifecycleStateMapper.fromAndroid(call.state)
        publish()
    }

    @Synchronized
    fun onCallStateChanged(call: Call, state: Int) {
        if (states.containsKey(call)) {
            states[call] = CallLifecycleStateMapper.fromAndroid(state)
            publish()
        }
    }

    @Synchronized
    fun onCallRemoved(call: Call) {
        states.remove(call)
        publish()
    }

    @Synchronized
    fun onCanAddCallChanged(value: Boolean) {
        canAddCall = value
        publish()
    }

    @Synchronized
    fun clear() {
        states.clear()
        canAddCall = null
        publish()
    }

    private fun publish() {
        snapshot = InCallRuntimeSnapshot(
            trackedCallCount = states.size,
            stateCounts = states.values.groupingBy { it }.eachCount(),
            canAddCall = canAddCall,
        )
    }
}
