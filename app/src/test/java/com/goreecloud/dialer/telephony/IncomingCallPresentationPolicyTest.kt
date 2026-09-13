package com.goreecloud.dialer.telephony

import org.junit.Assert.assertEquals
import org.junit.Test

class IncomingCallPresentationPolicyTest {
    @Test
    fun ringingCallUsesFullScreenWhenAllowed() {
        assertEquals(
            IncomingCallPresentationDecision.Present(requestFullScreen = true),
            IncomingCallPresentationPolicy.decide(
                IncomingCallPresentationFacts(
                    state = CallLifecycleState.RINGING,
                    notificationsAllowed = true,
                    fullScreenAllowed = true,
                ),
            ),
        )
    }

    @Test
    fun ringingCallFallsBackToNotificationWhenFullScreenIsUnavailable() {
        assertEquals(
            IncomingCallPresentationDecision.Present(requestFullScreen = false),
            IncomingCallPresentationPolicy.decide(
                IncomingCallPresentationFacts(
                    state = CallLifecycleState.RINGING,
                    notificationsAllowed = true,
                    fullScreenAllowed = false,
                ),
            ),
        )
    }

    @Test
    fun notificationDenialFailsClosed() {
        assertEquals(
            IncomingCallPresentationDecision.Blocked(
                "Incoming-call notifications are not allowed",
            ),
            IncomingCallPresentationPolicy.decide(
                IncomingCallPresentationFacts(
                    state = CallLifecycleState.RINGING,
                    notificationsAllowed = false,
                    fullScreenAllowed = true,
                ),
            ),
        )
    }

    @Test
    fun nonRingingCallDoesNotPresentIncomingUi() {
        assertEquals(
            IncomingCallPresentationDecision.NotApplicable,
            IncomingCallPresentationPolicy.decide(
                IncomingCallPresentationFacts(
                    state = CallLifecycleState.ACTIVE,
                    notificationsAllowed = true,
                    fullScreenAllowed = true,
                ),
            ),
        )
    }

    @Test
    fun notificationModeTracksCallLifecycle() {
        assertEquals(CallNotificationMode.INCOMING, CallNotificationModeResolver.resolve(CallLifecycleState.RINGING))
        assertEquals(CallNotificationMode.ONGOING, CallNotificationModeResolver.resolve(CallLifecycleState.CONNECTING))
        assertEquals(CallNotificationMode.ONGOING, CallNotificationModeResolver.resolve(CallLifecycleState.DIALING))
        assertEquals(CallNotificationMode.ONGOING, CallNotificationModeResolver.resolve(CallLifecycleState.ACTIVE))
        assertEquals(CallNotificationMode.ONGOING, CallNotificationModeResolver.resolve(CallLifecycleState.HOLDING))
        assertEquals(CallNotificationMode.NONE, CallNotificationModeResolver.resolve(CallLifecycleState.DISCONNECTED))
        assertEquals(CallNotificationMode.NONE, CallNotificationModeResolver.resolve(CallLifecycleState.UNKNOWN))
    }
}
