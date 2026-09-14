package com.goreecloud.dialer

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.telecom.InCallService
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.dialer.telephony.AndroidDefaultDialerRoleRequestPreparer
import com.goreecloud.dialer.telephony.DefaultDialerRoleRequestPreparation
import com.goreecloud.dialer.telephony.DevelopmentDefaultDialerAcceptance
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Android 14+ runtime checks for the modern Telecom/notification platform boundary. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 34)
class DialerAndroid14AcceptanceTest {
    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun runtimeIsAndroid14OrNewer() {
        assertTrue(Build.VERSION.SDK_INT >= 34)
    }

    @Test
    fun modernCallEndpointApisArePresentAtRuntime() {
        val callEndpointClass = Class.forName("android.telecom.CallEndpoint")
        val outcomeReceiverClass = Class.forName("android.os.OutcomeReceiver")

        val requestMethod = InCallService::class.java.getMethod(
            "requestCallEndpointChange",
            callEndpointClass,
            Executor::class.java,
            outcomeReceiverClass,
        )
        val callbackMethod = InCallService::class.java.getMethod(
            "onCallEndpointChanged",
            callEndpointClass,
        )

        assertEquals("requestCallEndpointChange", requestMethod.name)
        assertEquals("onCallEndpointChanged", callbackMethod.name)
    }

    @Test
    fun fullScreenIntentCapabilityProbeIsCallable() {
        val notificationManager = targetContext.getSystemService(NotificationManager::class.java)
        val method = NotificationManager::class.java.getMethod("canUseFullScreenIntent")
        val result = method.invoke(notificationManager)

        assertTrue(result is Boolean)
    }

    @Test
    fun defaultDialerRoleConsentRemainsFailClosedOnAndroid14() {
        assertFalse(DevelopmentDefaultDialerAcceptance.current.accepted)

        val preparation = AndroidDefaultDialerRoleRequestPreparer(targetContext).prepare()
        assertFalse(preparation is DefaultDialerRoleRequestPreparation.Prepared)
    }
}
