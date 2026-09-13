package com.goreecloud.dialer.telephony

import android.telecom.Call
import android.telecom.DisconnectCause
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallDispositionPolicyTest {
    @Test
    fun mapsAndroidCallDirectionWithoutIdentityData() {
        assertEquals(
            CallDirection.INCOMING,
            CallDispositionPolicy.fromAndroidEvidence(
                direction = Call.Details.DIRECTION_INCOMING,
                disconnectCode = null,
                state = CallLifecycleState.RINGING,
            ).direction,
        )
        assertEquals(
            CallDirection.OUTGOING,
            CallDispositionPolicy.fromAndroidEvidence(
                direction = Call.Details.DIRECTION_OUTGOING,
                disconnectCode = null,
                state = CallLifecycleState.DIALING,
            ).direction,
        )
        assertEquals(
            CallDirection.UNKNOWN,
            CallDispositionPolicy.fromAndroidEvidence(
                direction = Int.MAX_VALUE,
                disconnectCode = null,
                state = CallLifecycleState.ACTIVE,
            ).direction,
        )
    }

    @Test
    fun disconnectCauseIsNotProjectedBeforeTerminalState() {
        val disposition = CallDispositionPolicy.fromAndroidEvidence(
            direction = Call.Details.DIRECTION_INCOMING,
            disconnectCode = DisconnectCause.MISSED,
            state = CallLifecycleState.RINGING,
        )

        assertNull(disposition.terminalOutcome)
    }

    @Test
    fun mapsGenericDisconnectCausesWhenDisconnected() {
        val expected = mapOf(
            DisconnectCause.UNKNOWN to CallTerminalOutcome.UNKNOWN,
            DisconnectCause.ERROR to CallTerminalOutcome.ERROR,
            DisconnectCause.LOCAL to CallTerminalOutcome.LOCAL,
            DisconnectCause.REMOTE to CallTerminalOutcome.REMOTE,
            DisconnectCause.CANCELED to CallTerminalOutcome.CANCELED,
            DisconnectCause.MISSED to CallTerminalOutcome.MISSED,
            DisconnectCause.REJECTED to CallTerminalOutcome.REJECTED,
            DisconnectCause.BUSY to CallTerminalOutcome.BUSY,
            DisconnectCause.RESTRICTED to CallTerminalOutcome.RESTRICTED,
            DisconnectCause.OTHER to CallTerminalOutcome.OTHER,
            DisconnectCause.CONNECTION_MANAGER_NOT_SUPPORTED to
                CallTerminalOutcome.CONNECTION_MANAGER_NOT_SUPPORTED,
            DisconnectCause.ANSWERED_ELSEWHERE to CallTerminalOutcome.ANSWERED_ELSEWHERE,
            DisconnectCause.CALL_PULLED to CallTerminalOutcome.CALL_PULLED,
        )

        expected.forEach { (code, outcome) ->
            assertEquals(
                outcome,
                CallDispositionPolicy.fromAndroidEvidence(
                    direction = Call.Details.DIRECTION_UNKNOWN,
                    disconnectCode = code,
                    state = CallLifecycleState.DISCONNECTED,
                ).terminalOutcome,
            )
        }
    }

    @Test
    fun missingOrUnknownDisconnectCauseFailsClosedToUnknown() {
        assertEquals(
            CallTerminalOutcome.UNKNOWN,
            CallDispositionPolicy.fromAndroidEvidence(
                direction = null,
                disconnectCode = null,
                state = CallLifecycleState.DISCONNECTED,
            ).terminalOutcome,
        )
        assertEquals(
            CallTerminalOutcome.UNKNOWN,
            CallDispositionPolicy.fromAndroidEvidence(
                direction = null,
                disconnectCode = Int.MAX_VALUE,
                state = CallLifecycleState.DISCONNECTED,
            ).terminalOutcome,
        )
    }
}
