package com.goreecloud.dialer.telephony

/**
 * User-driven control intents for an already tracked Android Telecom call.
 * No action here is automatic; callers must select a specific process-local call session.
 */
sealed interface CallControlAction {
    data object AnswerAudio : CallControlAction
    data object Decline : CallControlAction
    data object End : CallControlAction
    data object Hold : CallControlAction
    data object Resume : CallControlAction
    data class StartDtmf(val digit: Char) : CallControlAction
    data object StopDtmf : CallControlAction
}

sealed interface CallControlResult {
    data object Succeeded : CallControlResult
    data class Rejected(val reason: String) : CallControlResult
    data class Failed(val reason: String) : CallControlResult
}

interface CallControlTarget {
    val state: CallLifecycleState

    fun answerAudio()
    fun decline()
    fun disconnect()
    fun hold()
    fun resume()
    fun startDtmf(digit: Char)
    fun stopDtmf()
}

/**
 * State-aware execution boundary. It refuses controls that do not make sense for the
 * current call state rather than blindly invoking Android Telecom operations.
 */
class CallControlEngine(
    private val target: CallControlTarget,
) {
    fun execute(action: CallControlAction): CallControlResult {
        val rejection = rejectionReason(action, target.state)
        if (rejection != null) return CallControlResult.Rejected(rejection)

        return try {
            when (action) {
                CallControlAction.AnswerAudio -> target.answerAudio()
                CallControlAction.Decline -> target.decline()
                CallControlAction.End -> target.disconnect()
                CallControlAction.Hold -> target.hold()
                CallControlAction.Resume -> target.resume()
                is CallControlAction.StartDtmf -> target.startDtmf(action.digit)
                CallControlAction.StopDtmf -> target.stopDtmf()
            }
            CallControlResult.Succeeded
        } catch (throwable: RuntimeException) {
            CallControlResult.Failed(
                throwable.message?.takeIf { it.isNotBlank() }
                    ?: throwable::class.java.simpleName,
            )
        }
    }

    private fun rejectionReason(
        action: CallControlAction,
        state: CallLifecycleState,
    ): String? = when (action) {
        CallControlAction.AnswerAudio ->
            if (state == CallLifecycleState.RINGING) null else "Call is not ringing"

        CallControlAction.Decline ->
            if (state == CallLifecycleState.RINGING) null else "Call is not ringing"

        CallControlAction.End ->
            if (state in ENDABLE_STATES) null else "Call cannot be ended from $state"

        CallControlAction.Hold ->
            if (state == CallLifecycleState.ACTIVE) null else "Only an active call can be held"

        CallControlAction.Resume ->
            if (state == CallLifecycleState.HOLDING) null else "Only a held call can be resumed"

        is CallControlAction.StartDtmf -> when {
            action.digit !in DTMF_DIGITS -> "Invalid DTMF digit"
            state !in DTMF_STATES -> "DTMF is unavailable while call state is $state"
            else -> null
        }

        CallControlAction.StopDtmf ->
            if (state in DTMF_STATES) null else "DTMF is unavailable while call state is $state"
    }

    private companion object {
        val ENDABLE_STATES = setOf(
            CallLifecycleState.CONNECTING,
            CallLifecycleState.DIALING,
            CallLifecycleState.RINGING,
            CallLifecycleState.ACTIVE,
            CallLifecycleState.HOLDING,
        )
        val DTMF_STATES = setOf(CallLifecycleState.ACTIVE, CallLifecycleState.HOLDING)
        val DTMF_DIGITS = setOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '#')
    }
}
