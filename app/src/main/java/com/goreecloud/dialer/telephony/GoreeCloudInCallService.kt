package com.goreecloud.dialer.telephony

import android.telecom.Call
import android.telecom.InCallService
import java.util.IdentityHashMap

/**
 * Development InCallService lifecycle boundary.
 *
 * This service intentionally does not claim incoming/ongoing call UI, ringing ownership,
 * or call-control acceptance yet. It tracks only content-free call lifecycle state so the
 * next UI/control increment can consume a tested Telecom boundary without persisting data.
 */
class GoreeCloudInCallService : InCallService() {
    private val callbacks = IdentityHashMap<Call, Call.Callback>()

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        val callback = object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                InCallRuntimeStore.onCallStateChanged(call, state)
            }
        }

        callbacks[call] = callback
        call.registerCallback(callback)
        InCallRuntimeStore.onCallAdded(call)
    }

    override fun onCallRemoved(call: Call) {
        callbacks.remove(call)?.let(call::unregisterCallback)
        InCallRuntimeStore.onCallRemoved(call)
        super.onCallRemoved(call)
    }

    override fun onCanAddCallChanged(canAddCall: Boolean) {
        super.onCanAddCallChanged(canAddCall)
        InCallRuntimeStore.onCanAddCallChanged(canAddCall)
    }

    override fun onDestroy() {
        callbacks.forEach { (call, callback) -> call.unregisterCallback(callback) }
        callbacks.clear()
        InCallRuntimeStore.clear()
        super.onDestroy()
    }
}
