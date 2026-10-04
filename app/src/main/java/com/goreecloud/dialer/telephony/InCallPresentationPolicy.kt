package com.goreecloud.dialer.telephony

object InCallPresentationPolicy {
    fun orderedCalls(calls: List<CallRuntimeSummary>): List<CallRuntimeSummary> =
        calls.sortedWith(
            compareBy<CallRuntimeSummary> { statePriority(it.state) }
                .thenBy { it.sessionId },
        )

    fun callSetLabel(snapshot: InCallRuntimeSnapshot): String {
        val count = snapshot.trackedCallCount.coerceAtLeast(0)
        val base = if (count == 1) "1 live call" else "$count live calls"
        val capacity = when (snapshot.canAddCall) {
            true -> "add-call capacity available"
            false -> "add-call capacity unavailable"
            null -> "add-call capacity awaiting Telecom"
        }
        return "$base • $capacity"
    }

    private fun statePriority(state: CallLifecycleState): Int = when (state) {
        CallLifecycleState.RINGING,
        CallLifecycleState.SIMULATED_RINGING,
        -> 0
        CallLifecycleState.ACTIVE -> 1
        CallLifecycleState.HOLDING -> 2
        CallLifecycleState.DIALING,
        CallLifecycleState.CONNECTING,
        CallLifecycleState.SELECTING_PHONE_ACCOUNT,
        -> 3
        CallLifecycleState.NEW,
        CallLifecycleState.PULLING_CALL,
        CallLifecycleState.AUDIO_PROCESSING,
        -> 4
        CallLifecycleState.DISCONNECTING -> 5
        CallLifecycleState.DISCONNECTED -> 6
        CallLifecycleState.UNKNOWN -> 7
    }
}
