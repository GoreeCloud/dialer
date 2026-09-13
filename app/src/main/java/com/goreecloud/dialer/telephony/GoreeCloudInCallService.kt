package com.goreecloud.dialer.telephony

import android.telecom.Call
import android.telecom.InCallService
import java.util.IdentityHashMap

/**
 * Development InCallService lifecycle and presentation boundary.
 *
 * The service tracks only process-local call sessions and content-minimized lifecycle state. It
 * now owns a Development incoming-call notification/full-screen surface, but it deliberately does
 * not claim production UI replacement or ringtone ownership in the manifest yet.
 */
class GoreeCloudInCallService : InCallService() {
    private val callbacks = IdentityHashMap<Call, Call.Callback>()
    private val sessionIds = IdentityHashMap<Call, Long>()
    private val incomingCallPresenter by lazy { AndroidIncomingCallPresenter(this) }

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

    override fun onDestroy() {
        sessionIds.values.forEach(incomingCallPresenter::cancel)
        sessionIds.clear()
        callbacks.forEach { (call, callback) -> call.unregisterCallback(callback) }
        callbacks.clear()
        InCallRuntimeStore.clear()
        super.onDestroy()
    }
}
