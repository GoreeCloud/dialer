package com.goreecloud.dialer.telephony

/**
 * Maps content-minimized call lifecycle/capability state to user-visible essential controls.
 *
 * This policy does not inspect caller identity, phone numbers, call details, or audio. It also
 * does not make any control automatic: every returned action still requires an explicit tap.
 */
object CallControlPresentationPolicy {
    fun actionsFor(
        state: CallLifecycleState,
        holdCurrentlyAvailable: Boolean = true,
    ): List<CallControlAction> = when (state) {
        CallLifecycleState.RINGING,
        CallLifecycleState.SIMULATED_RINGING,
        -> listOf(
            CallControlAction.AnswerAudio,
            CallControlAction.Decline,
        )

        CallLifecycleState.CONNECTING,
        CallLifecycleState.DIALING,
        -> listOf(CallControlAction.End)

        CallLifecycleState.ACTIVE -> buildList {
            if (holdCurrentlyAvailable) add(CallControlAction.Hold)
            add(CallControlAction.End)
        }

        CallLifecycleState.HOLDING -> buildList {
            if (holdCurrentlyAvailable) add(CallControlAction.Resume)
            add(CallControlAction.End)
        }

        CallLifecycleState.NEW,
        CallLifecycleState.SELECTING_PHONE_ACCOUNT,
        CallLifecycleState.DISCONNECTING,
        CallLifecycleState.PULLING_CALL,
        CallLifecycleState.AUDIO_PROCESSING,
        CallLifecycleState.DISCONNECTED,
        CallLifecycleState.UNKNOWN,
        -> emptyList()
    }
}

fun CallControlAction.presentationLabel(): String = when (this) {
    CallControlAction.AnswerAudio -> "Answer"
    CallControlAction.Decline -> "Decline"
    CallControlAction.End -> "End"
    CallControlAction.Hold -> "Hold"
    CallControlAction.Resume -> "Resume"
    is CallControlAction.StartDtmf -> "DTMF $digit"
    CallControlAction.StopDtmf -> "Stop DTMF"
}
