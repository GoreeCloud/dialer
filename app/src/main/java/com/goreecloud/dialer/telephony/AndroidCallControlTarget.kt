package com.goreecloud.dialer.telephony

import android.telecom.Call
import android.telecom.VideoProfile

/** Thin Android Telecom adapter used only after the state/capability-aware policy accepts an action. */
class AndroidCallControlTarget(
    private val call: Call,
) : CallControlTarget {
    override val state: CallLifecycleState
        get() = CallLifecycleStateMapper.fromAndroid(call.state)

    override val holdCurrentlyAvailable: Boolean
        get() = call.details.can(Call.Details.CAPABILITY_HOLD)

    override fun answerAudio() {
        @Suppress("DEPRECATION")
        call.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    override fun decline() {
        @Suppress("DEPRECATION")
        call.reject(false, null)
    }

    override fun disconnect() = call.disconnect()

    override fun hold() = call.hold()

    override fun resume() = call.unhold()

    override fun startDtmf(digit: Char) = call.playDtmfTone(digit)

    override fun stopDtmf() = call.stopDtmfTone()
}
