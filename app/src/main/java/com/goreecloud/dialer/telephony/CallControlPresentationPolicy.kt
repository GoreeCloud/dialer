package com.goreecloud.dialer.telephony

/**
 * Maps a content-minimized call lifecycle state to user-visible essential controls.
 *
 * This policy does not inspect caller identity, phone numbers, call details, or audio. It also
 * does not make any control automatic: every returned action still requires an explicit tap.
 */
object CallControlPresentationPolicy {
    fun actionsFor(state: CallLifecycleState): List<CallControlAction> = when (state) {
        CallLifecycleState.RINGING -> listOf(
            CallControlAction.AnswerAudio,
            CallControlAction.Decline,
        )

        CallLifecycleState.CONNECTING,
        CallLifecycleState.DIALING,
        -> listOf(CallControlAction.End)

        CallLifecycleState.ACTIVE -> listOf(
            CallControlAction.Hold,
            CallControlAction.End,
        )

        CallLifecycleState.HOLDING -> listOf(
            CallControlAction.Resume,
            CallControlAction.End,
        )

        CallLifecycleState.NEW,
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
