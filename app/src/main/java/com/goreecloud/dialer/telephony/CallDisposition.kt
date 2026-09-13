package com.goreecloud.dialer.telephony

import android.telecom.Call
import android.telecom.DisconnectCause

/** Direction evidence exposed by Android Telecom without projecting caller identity. */
enum class CallDirection {
    UNKNOWN,
    INCOMING,
    OUTGOING,
}

/**
 * Generic terminal outcome reported by Android Telecom.
 *
 * These values intentionally mirror only DisconnectCause's generic code. Provider-specific reason
 * strings, labels, descriptions, phone numbers, and identity data are outside this contract.
 */
enum class CallTerminalOutcome {
    UNKNOWN,
    ERROR,
    LOCAL,
    REMOTE,
    CANCELED,
    MISSED,
    REJECTED,
    BUSY,
    RESTRICTED,
    OTHER,
    CONNECTION_MANAGER_NOT_SUPPORTED,
    ANSWERED_ELSEWHERE,
    CALL_PULLED,
}

data class CallDisposition(
    val direction: CallDirection,
    val terminalOutcome: CallTerminalOutcome?,
)

/** Pure mapping boundary so direction/outcome semantics are unit-testable without a live Call. */
object CallDispositionPolicy {
    fun fromAndroidEvidence(
        direction: Int?,
        disconnectCode: Int?,
        state: CallLifecycleState,
    ): CallDisposition = CallDisposition(
        direction = when (direction) {
            Call.Details.DIRECTION_INCOMING -> CallDirection.INCOMING
            Call.Details.DIRECTION_OUTGOING -> CallDirection.OUTGOING
            Call.Details.DIRECTION_UNKNOWN, null -> CallDirection.UNKNOWN
            else -> CallDirection.UNKNOWN
        },
        terminalOutcome = if (state == CallLifecycleState.DISCONNECTED) {
            when (disconnectCode) {
                DisconnectCause.ERROR -> CallTerminalOutcome.ERROR
                DisconnectCause.LOCAL -> CallTerminalOutcome.LOCAL
                DisconnectCause.REMOTE -> CallTerminalOutcome.REMOTE
                DisconnectCause.CANCELED -> CallTerminalOutcome.CANCELED
                DisconnectCause.MISSED -> CallTerminalOutcome.MISSED
                DisconnectCause.REJECTED -> CallTerminalOutcome.REJECTED
                DisconnectCause.BUSY -> CallTerminalOutcome.BUSY
                DisconnectCause.RESTRICTED -> CallTerminalOutcome.RESTRICTED
                DisconnectCause.OTHER -> CallTerminalOutcome.OTHER
                DisconnectCause.CONNECTION_MANAGER_NOT_SUPPORTED ->
                    CallTerminalOutcome.CONNECTION_MANAGER_NOT_SUPPORTED
                DisconnectCause.ANSWERED_ELSEWHERE -> CallTerminalOutcome.ANSWERED_ELSEWHERE
                DisconnectCause.CALL_PULLED -> CallTerminalOutcome.CALL_PULLED
                DisconnectCause.UNKNOWN, null -> CallTerminalOutcome.UNKNOWN
                else -> CallTerminalOutcome.UNKNOWN
            }
        } else {
            null
        },
    )
}

object AndroidCallDisposition {
    fun from(
        details: Call.Details?,
        state: CallLifecycleState,
    ): CallDisposition = CallDispositionPolicy.fromAndroidEvidence(
        direction = details?.callDirection,
        disconnectCode = if (state == CallLifecycleState.DISCONNECTED) {
            details?.disconnectCause?.code
        } else {
            null
        },
        state = state,
    )
}
