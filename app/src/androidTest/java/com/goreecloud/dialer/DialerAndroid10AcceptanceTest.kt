package com.goreecloud.dialer

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.dialer.telephony.AndroidDefaultDialerRoleRequestPreparer
import com.goreecloud.dialer.telephony.AndroidIncomingCallNotificationAccess
import com.goreecloud.dialer.telephony.DefaultDialerRoleRequestPreparation
import com.goreecloud.dialer.telephony.DevelopmentDefaultDialerAcceptance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/** Android 10 / API 29 acceptance checks for the project's minimum supported runtime. */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 29, maxSdkVersion = 29)
class DialerAndroid10AcceptanceTest {
    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun runtimeIsAndroid10Api29() {
        assertEquals(Build.VERSION_CODES.Q, Build.VERSION.SDK_INT)
    }

    @Test
    fun modernIncomingCallAccessStateIsNotClaimedOnAndroid10() {
        val access = AndroidIncomingCallNotificationAccess(targetContext).snapshot()

        assertFalse(access.postNotificationsPermissionApplicable)
        assertNull(access.postNotificationsPermissionGranted)
        assertFalse(access.fullScreenIntentAccessApplicable)
        assertNull(access.fullScreenIntentAllowed)
        assertNull(AndroidIncomingCallNotificationAccess(targetContext).fullScreenIntentSettingsIntent())
    }

    @Test
    fun defaultDialerRoleConsentRemainsFailClosedOnAndroid10() {
        assertFalse(DevelopmentDefaultDialerAcceptance.current.accepted)
        val preparation = AndroidDefaultDialerRoleRequestPreparer(targetContext).prepare()
        assertFalse(preparation is DefaultDialerRoleRequestPreparation.Prepared)
    }
}
