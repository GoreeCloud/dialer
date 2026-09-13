package com.goreecloud.dialer.telephony

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import java.util.IdentityHashMap

/**
 * Development InCallService lifecycle, presentation, and audio-control boundary.
 *
 * The service tracks only process-local call sessions and content-minimized lifecycle/audio state.
 * Development incoming/ongoing presentation exists, but production UI replacement and ringtone
 * ownership remain undeclared until end-to-end device validation is complete.
 */
class GoreeCloudInCallService : InCallService() {
    private val callbacks = IdentityHashMap<Call, Call.Callback>()
    private val sessionIds = IdentityHashMap<Call, Long>()
    private val incomingCallPresenter by lazy { AndroidIncomingCallPresenter(this) }
    private val audioControlTarget = object : CallAudioControlTarget {
        override fun setMuted(isMuted: Boolean) {
            this@GoreeCloudInCallService.setMuted(isMuted)
        }
    }

    override fun onCreate() {
        super.onCreate()
        InCallAudioControlRuntime.attach(audioControlTarget)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        val callback = object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                InCallRuntimeStore.onCallStateChanged(call, state)
                sessionIds[call]?.let { sessionId ->
                    incomingCallPresenter.sync(
                        sessionId = sessionId,
                        state = CallLifecycleStateMapper.fromAndroid(state),
                    )
                }
            }
        }

        callbacks[call] = callback
        call.registerCallback(callback)
        val sessionId = InCallRuntimeStore.onCallAdded(call)
        sessionIds[call] = sessionId
        incomingCallPresenter.sync(
            sessionId = sessionId,
            state = CallLifecycleStateMapper.fromAndroid(call.state),
        )
    }

    override fun onCallRemoved(call: Call) {
        sessionIds.remove(call)?.let(incomingCallPresenter::cancel)
        callbacks.remove(call)?.let(call::unregisterCallback)
        InCallRuntimeStore.onCallRemoved(call)
        super.onCallRemoved(call)
    }

    override fun onCanAddCallChanged(canAddCall: Boolean) {
        super.onCanAddCallChanged(canAddCall)
        InCallRuntimeStore.onCanAddCallChanged(canAddCall)
    }

    @Suppress("DEPRECATION")
    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        InCallRuntimeStore.onMuteStateChanged(audioState.isMuted)
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        InCallRuntimeStore.onMuteStateChanged(isMuted)
    }

    override fun onDestroy() {
        InCallAudioControlRuntime.detach(audioControlTarget)
        sessionIds.values.forEach(incomingCallPresenter::cancel)
        sessionIds.clear()
        callbacks.forEach { (call, callback) -> call.unregisterCallback(callback) }
        callbacks.clear()
        InCallRuntimeStore.clear()
        super.onDestroy()
    }
}
