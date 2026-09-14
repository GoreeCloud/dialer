package com.goreecloud.dialer

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.InCallService
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.goreecloud.dialer.telephony.AndroidDefaultDialerRoleRequestPreparer
import com.goreecloud.dialer.telephony.AndroidIncomingCallNotificationAccess
import com.goreecloud.dialer.telephony.DefaultDialerRoleRequestPreparation
import com.goreecloud.dialer.telephony.DevelopmentDefaultDialerAcceptance
import com.goreecloud.dialer.telephony.GoreeCloudInCallService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Executable emulator checks for deterministic Android phone-app contracts. */
@RunWith(AndroidJUnit4::class)
class DialerPlatformAcceptanceTest {
    private val instrumentation
        get() = InstrumentationRegistry.getInstrumentation()

    private val targetContext
        get() = instrumentation.targetContext

    @Test
    fun mainActivityLaunchesOnAndroidRuntime() {
        val intent = Intent(targetContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = instrumentation.startActivitySync(intent)
        try {
            assertEquals(MainActivity::class.java, activity::class.java)
        } finally {
            instrumentation.runOnMainSync { activity.finish() }
        }
    }

    @Test
    fun actionDialTelIntentResolvesToGoreeCloudDialer() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:5551234"))
        val matches = targetContext.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )

        assertTrue(
            matches.any {
                it.activityInfo.packageName == targetContext.packageName &&
                    it.activityInfo.name == MainActivity::class.java.name
            },
        )
    }

    @Test
    fun inCallServiceManifestMatchesDefaultPhoneBindingContract() {
        val info = targetContext.packageManager.getServiceInfo(
            ComponentName(targetContext, GoreeCloudInCallService::class.java),
            PackageManager.GET_META_DATA,
        )

        assertTrue(info.exported)
        assertEquals(Manifest.permission.BIND_INCALL_SERVICE, info.permission)
        assertFalse(info.metaData?.getBoolean(InCallService.METADATA_IN_CALL_SERVICE_UI, false) == true)
        assertFalse(info.metaData?.getBoolean(InCallService.METADATA_IN_CALL_SERVICE_RINGING, false) == true)
    }

    @Test
    fun incomingCallPresentationPermissionsRemainDeclared() {
        val packageInfo = targetContext.packageManager.getPackageInfo(
            targetContext.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        val requestedPermissions = packageInfo.requestedPermissions?.toSet().orEmpty()

        assertTrue(Manifest.permission.POST_NOTIFICATIONS in requestedPermissions)
        assertTrue(Manifest.permission.USE_FULL_SCREEN_INTENT in requestedPermissions)
    }

    @Test
    fun currentAcceptanceCannotPrepareDialerRoleConsent() {
        assertFalse(DevelopmentDefaultDialerAcceptance.current.accepted)

        val preparation = AndroidDefaultDialerRoleRequestPreparer(targetContext).prepare()
        assertFalse(preparation is DefaultDialerRoleRequestPreparation.Prepared)
    }

    @Test
    fun androidAutomaticBackupFlagRemainsDisabled() {
        val applicationInfo = targetContext.packageManager.getApplicationInfo(
            targetContext.packageName,
            0,
        )
        assertEquals(0, applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP)
    }

    @Test
    @SdkSuppress(maxSdkVersion = 32)
    fun preAndroid13DoesNotRequireModernNotificationAccess() {
        val access = AndroidIncomingCallNotificationAccess(targetContext).snapshot()

        assertFalse(access.postNotificationsPermissionApplicable)
        assertNull(access.postNotificationsPermissionGranted)
        assertFalse(access.fullScreenIntentAccessApplicable)
        assertNull(access.fullScreenIntentAllowed)
    }
}
