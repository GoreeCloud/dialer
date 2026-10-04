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
        val stateSummary = stateBreakdown(snapshot)
        val conferenceSummary = conferenceBreakdown(snapshot)
        val capacity = when (snapshot.canAddCall) {
            true -> "add-call capacity available"
            false -> "add-call capacity unavailable"
            null -> "add-call capacity awaiting Telecom"
        }
        return (listOf(base) + stateSummary + conferenceSummary + capacity).joinToString(" • ")
    }

    fun conferenceBreakdown(snapshot: InCallRuntimeSnapshot): List<String> {
        val conferenceParents = snapshot.calls.count { it.childSessionIds.isNotEmpty() }
        val conferenceChildren = snapshot.calls.count { it.parentSessionId != null }
        return buildList {
            if (conferenceParents > 0) {
                add(
                    if (conferenceParents == 1) {
                        "1 conference"
                    } else {
                        "$conferenceParents conferences"
                    },
                )
            }
            if (conferenceChildren > 0) {
                add("$conferenceChildren conference legs")
            }
        }
    }

    fun stateBreakdown(snapshot: InCallRuntimeSnapshot): List<String> {
        val ringing =
            snapshot.stateCounts.getOrDefault(CallLifecycleState.RINGING, 0) +
                snapshot.stateCounts.getOrDefault(CallLifecycleState.SIMULATED_RINGING, 0)
        val active = snapshot.stateCounts.getOrDefault(CallLifecycleState.ACTIVE, 0)
        val held = snapshot.stateCounts.getOrDefault(CallLifecycleState.HOLDING, 0)

        return buildList {
            if (ringing > 0) add("$ringing ringing")
            if (active > 0) add("$active active")
            if (held > 0) add("$held held")
        }
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
