package com.goreecloud.dialer.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Explicit, non-exported notification action receiver for a process-local Telecom session. */
class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getLongExtra(AndroidIncomingCallPresenter.EXTRA_SESSION_ID, -1L)
        if (sessionId <= 0L) return

        val action = when (intent.action) {
            ACTION_ANSWER -> CallControlAction.AnswerAudio
            ACTION_DECLINE -> CallControlAction.Decline
            else -> return
        }
        InCallRuntimeStore.execute(sessionId, action)
    }

    companion object {
        const val ACTION_ANSWER = "com.goreecloud.dialer.action.ANSWER"
        const val ACTION_DECLINE = "com.goreecloud.dialer.action.DECLINE"
    }
}
