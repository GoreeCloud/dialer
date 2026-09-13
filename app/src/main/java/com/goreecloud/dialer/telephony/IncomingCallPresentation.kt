package com.goreecloud.dialer.telephony

/**
 * Content-minimized facts used to decide whether GoreeCloud may present an incoming-call surface.
 * Caller identity, phone numbers, call details, and audio are deliberately outside this contract.
 */
data class IncomingCallPresentationFacts(
    val state: CallLifecycleState,
    val notificationsAllowed: Boolean,
    val fullScreenAllowed: Boolean,
)

enum class CallNotificationMode {
    NONE,
    INCOMING,
    ONGOING,
}

object CallNotificationModeResolver {
    fun resolve(state: CallLifecycleState): CallNotificationMode = when (state) {
        CallLifecycleState.RINGING -> CallNotificationMode.INCOMING
        CallLifecycleState.CONNECTING,
        CallLifecycleState.DIALING,
        CallLifecycleState.ACTIVE,
        CallLifecycleState.HOLDING,
        -> CallNotificationMode.ONGOING

        CallLifecycleState.NEW,
        CallLifecycleState.DISCONNECTED,
        CallLifecycleState.UNKNOWN,
        -> CallNotificationMode.NONE
    }
}

sealed interface IncomingCallPresentationDecision {
    data object NotApplicable : IncomingCallPresentationDecision
    data class Blocked(val reason: String) : IncomingCallPresentationDecision
    data class Present(val requestFullScreen: Boolean) : IncomingCallPresentationDecision
}

object IncomingCallPresentationPolicy {
    fun decide(facts: IncomingCallPresentationFacts): IncomingCallPresentationDecision = when {
        facts.state != CallLifecycleState.RINGING -> IncomingCallPresentationDecision.NotApplicable
        !facts.notificationsAllowed -> IncomingCallPresentationDecision.Blocked(
            "Incoming-call notifications are not allowed",
        )
        else -> IncomingCallPresentationDecision.Present(
            requestFullScreen = facts.fullScreenAllowed,
        )
    }
}

sealed interface IncomingCallPresentationResult {
    data object NotApplicable : IncomingCallPresentationResult
    data class Presented(val fullScreenRequested: Boolean) : IncomingCallPresentationResult
    data class Blocked(val reason: String) : IncomingCallPresentationResult
    data class Failed(val reason: String) : IncomingCallPresentationResult
}
