package com.goreecloud.dialer.telephony

/** User-driven mute actions owned by the active InCallService rather than an individual Call. */
sealed interface CallAudioControlAction {
    data object Mute : CallAudioControlAction
    data object Unmute : CallAudioControlAction
}

sealed interface CallAudioControlResult {
    data object Succeeded : CallAudioControlResult
    data class Rejected(val reason: String) : CallAudioControlResult
    data class Failed(val reason: String) : CallAudioControlResult
}

interface CallAudioControlTarget {
    fun setMuted(isMuted: Boolean)
}

class CallAudioControlEngine(
    private val target: CallAudioControlTarget,
) {
    fun execute(action: CallAudioControlAction): CallAudioControlResult = try {
        target.setMuted(action == CallAudioControlAction.Mute)
        CallAudioControlResult.Succeeded
    } catch (runtimeException: RuntimeException) {
        CallAudioControlResult.Failed(
            runtimeException.message?.takeIf { it.isNotBlank() }
                ?: runtimeException::class.java.simpleName,
        )
    }
}

/**
 * Process-local bridge to the currently bound InCallService audio authority.
 * The target is attached only while the service exists and is cleared on service destruction.
 */
object InCallAudioControlRuntime {
    private var target: CallAudioControlTarget? = null

    @Synchronized
    fun attach(target: CallAudioControlTarget) {
        this.target = target
    }

    @Synchronized
    fun detach(target: CallAudioControlTarget) {
        if (this.target === target) this.target = null
    }

    @Synchronized
    fun execute(action: CallAudioControlAction): CallAudioControlResult {
        val activeTarget = target
            ?: return CallAudioControlResult.Rejected("InCallService audio authority is not active")
        return CallAudioControlEngine(activeTarget).execute(action)
    }
}
