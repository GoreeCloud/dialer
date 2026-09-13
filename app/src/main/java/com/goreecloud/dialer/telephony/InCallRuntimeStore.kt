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
 * categories, generic direction/terminal-outcome evidence, aggregate state, endpoint categories,
 * content-free audio-control state, narrow control-capability booleans, conference relationships
 * expressed only as generated session IDs, and minimized post-dial wait state. No number, caller
 * name, account identifier, endpoint device name, Call.Details object, provider-specific disconnect
 * reason, post-dial sequence content, transcript, recording, or audio is persisted or projected.
 */
data class CallRuntimeSummary(
    val sessionId: Long,
    val state: CallLifecycleState,
    val direction: CallDirection,
    val terminalOutcome: CallTerminalOutcome?,
    val holdSupported: Boolean,
    val holdCurrentlyAvailable: Boolean,
    val muteSupported: Boolean,
    val manageConferenceSupported: Boolean,
    val mergeConferenceAvailable: Boolean,
    val swapConferenceAvailable: Boolean,
    val separateFromConferenceAvailable: Boolean,
    val conferenceableSessionIds: List<Long>,
    val parentSessionId: Long?,
    val childSessionIds: List<Long>,
    val postDialWaitPending: Boolean,
    val postDialRemainingCharacterCount: Int,
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
        var direction: CallDirection,
        var terminalOutcome: CallTerminalOutcome?,
        var capabilities: CallControlCapabilities,
        var conferenceableCalls: List<Call>,
        var parent: Call?,
        var children: List<Call>,
        var postDialWaitPending: Boolean,
        var postDialRemainingCharacterCount: Int,
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
        val state = CallLifecycleStateMapper.fromAndroid(call.state)
        val details = call.details
        val disposition = AndroidCallDisposition.from(details, state)
        val tracked = TrackedCall(
            sessionId = nextSessionId.getAndIncrement(),
            call = call,
            state = state,
            direction = disposition.direction,
            terminalOutcome = disposition.terminalOutcome,
            capabilities = AndroidCallControlCapabilities.from(details),
            conferenceableCalls = call.conferenceableCalls.toList(),
            parent = call.parent,
            children = call.children.toList(),
            postDialWaitPending = false,
            postDialRemainingCharacterCount = 0,
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
        refreshDisposition(tracked, call.details)
        if (tracked.state == CallLifecycleState.DISCONNECTED) {
            tracked.postDialWaitPending = false
            tracked.postDialRemainingCharacterCount = 0
        }
        publish()
    }

    @Synchronized
    fun onCallDetailsChanged(call: Call, details: Call.Details) {
        val tracked = trackedByCall[call] ?: return
        tracked.capabilities = AndroidCallControlCapabilities.from(details)
        refreshDisposition(tracked, details)
        publish()
    }

    @Synchronized
    fun onConferenceableCallsChanged(call: Call, conferenceableCalls: List<Call>) {
        val tracked = trackedByCall[call] ?: return
        tracked.conferenceableCalls = conferenceableCalls.toList()
        publish()
    }

    @Synchronized
    fun onParentChanged(call: Call, parent: Call?) {
        val tracked = trackedByCall[call] ?: return
        tracked.parent = parent
        publish()
    }

    @Synchronized
    fun onChildrenChanged(call: Call, children: List<Call>) {
        val tracked = trackedByCall[call] ?: return
        tracked.children = children.toList()
        publish()
    }

    @Synchronized
    fun onPostDialWait(call: Call, remainingPostDialSequence: String) {
        val tracked = trackedByCall[call] ?: return
        tracked.postDialWaitPending = true
        tracked.postDialRemainingCharacterCount = remainingPostDialSequence.length.coerceAtLeast(0)
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
    fun executeConference(
        sessionId: Long,
        action: ConferenceControlAction,
    ): ConferenceControlResult {
        val tracked = trackedById[sessionId]
            ?: return ConferenceControlResult.Rejected("Call session is no longer active")

        val conferenceableSessionIds = tracked.conferenceableCalls
            .mapNotNull { trackedByCall[it]?.sessionId }
            .toSet()
        val hasTrackedParent = tracked.parent?.let { trackedByCall.containsKey(it) } == true
        val facts = ConferenceControlFacts(
            sourceSessionId = tracked.sessionId,
            state = tracked.state,
            conferenceableSessionIds = conferenceableSessionIds,
            hasParent = hasTrackedParent,
            mergeConferenceAvailable = tracked.capabilities.mergeConferenceAvailable,
            swapConferenceAvailable = tracked.capabilities.swapConferenceAvailable,
            separateFromConferenceAvailable = tracked.capabilities.separateFromConferenceAvailable,
        )

        return when (val decision = ConferenceControlPolicy.decide(action, facts)) {
            is ConferenceControlDecision.Rejected ->
                ConferenceControlResult.Rejected(decision.reason)

            ConferenceControlDecision.Allowed -> try {
                when (action) {
                    is ConferenceControlAction.ConferenceWith -> {
                        val other = trackedById[action.otherSessionId]
                            ?: return ConferenceControlResult.Rejected(
                                "Conference target session is no longer active",
                            )
                        tracked.call.conference(other.call)
                    }

                    ConferenceControlAction.MergeConference -> tracked.call.mergeConference()
                    ConferenceControlAction.SwapConference -> tracked.call.swapConference()
                    ConferenceControlAction.SeparateFromConference -> tracked.call.splitFromConference()
                }
                ConferenceControlResult.Submitted
            } catch (runtimeException: RuntimeException) {
                ConferenceControlResult.Failed(
                    "Android Telecom conference operation failed: " +
                        runtimeException::class.java.simpleName,
                )
            }
        }
    }

    @Synchronized
    fun executePostDial(
        sessionId: Long,
        action: PostDialControlAction,
    ): PostDialControlResult {
        val tracked = trackedById[sessionId]
            ?: return PostDialControlResult.Rejected("Call session is no longer active")

        val decision = PostDialControlPolicy.decide(
            PostDialControlFacts(
                state = tracked.state,
                waitPending = tracked.postDialWaitPending,
            ),
        )
        if (decision is PostDialControlDecision.Rejected) {
            return PostDialControlResult.Rejected(decision.reason)
        }

        return try {
            tracked.call.postDialContinue(action == PostDialControlAction.Continue)
            tracked.postDialWaitPending = false
            tracked.postDialRemainingCharacterCount = 0
            publish()
            PostDialControlResult.Submitted
        } catch (runtimeException: RuntimeException) {
            PostDialControlResult.Failed(
                "Android Telecom post-dial request failed: " +
                    runtimeException::class.java.simpleName,
            )
        }
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

    private fun refreshDisposition(
        tracked: TrackedCall,
        details: Call.Details?,
    ) {
        val disposition = AndroidCallDisposition.from(details, tracked.state)
        if (details != null || tracked.direction == CallDirection.UNKNOWN) {
            tracked.direction = disposition.direction
        }
        tracked.terminalOutcome = disposition.terminalOutcome
    }

    private fun publish() {
        val summaries = trackedById.values.map { tracked ->
            CallRuntimeSummary(
                sessionId = tracked.sessionId,
                state = tracked.state,
                direction = tracked.direction,
                terminalOutcome = tracked.terminalOutcome,
                holdSupported = tracked.capabilities.holdSupported,
                holdCurrentlyAvailable = tracked.capabilities.holdCurrentlyAvailable,
                muteSupported = tracked.capabilities.muteSupported,
                manageConferenceSupported = tracked.capabilities.manageConferenceSupported,
                mergeConferenceAvailable = tracked.capabilities.mergeConferenceAvailable,
                swapConferenceAvailable = tracked.capabilities.swapConferenceAvailable,
                separateFromConferenceAvailable =
                    tracked.capabilities.separateFromConferenceAvailable,
                conferenceableSessionIds = tracked.conferenceableCalls
                    .mapNotNull { trackedByCall[it]?.sessionId }
                    .distinct(),
                parentSessionId = tracked.parent?.let { trackedByCall[it]?.sessionId },
                childSessionIds = tracked.children
                    .mapNotNull { trackedByCall[it]?.sessionId }
                    .distinct(),
                postDialWaitPending = tracked.postDialWaitPending,
                postDialRemainingCharacterCount = tracked.postDialRemainingCharacterCount,
            )
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
