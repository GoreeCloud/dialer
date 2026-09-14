package com.goreecloud.dialer.telephony

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

data class IncomingCallNotificationAccessSnapshot(
    val notificationsEnabled: Boolean,
    val postNotificationsPermissionApplicable: Boolean,
    val postNotificationsPermissionGranted: Boolean?,
    val fullScreenIntentAccessApplicable: Boolean,
    val fullScreenIntentAllowed: Boolean?,
) {
    val canPostIncomingNotifications: Boolean
        get() = notificationsEnabled && postNotificationsPermissionGranted != false

    val canUseFullScreenIntent: Boolean
        get() = !fullScreenIntentAccessApplicable || fullScreenIntentAllowed == true
}

/**
 * Content-free Android access probe for Development incoming-call presentation.
 *
 * This intentionally reports authorization/platform state only. It never reads caller identity,
 * call details, notification contents, endpoint identity, or any persisted user data.
 */
class AndroidIncomingCallNotificationAccess(
    context: Context,
) {
    private val applicationContext = context.applicationContext

    fun snapshot(): IncomingCallNotificationAccessSnapshot {
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        val permissionApplicable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        val permissionGranted = if (permissionApplicable) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            null
        }
        val fullScreenApplicable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        val fullScreenAllowed = if (fullScreenApplicable) {
            notificationManager?.let(::fullScreenAllowedApi34) ?: false
        } else {
            null
        }

        return IncomingCallNotificationAccessSnapshot(
            notificationsEnabled = notificationManager != null &&
                NotificationManagerCompat.from(applicationContext).areNotificationsEnabled(),
            postNotificationsPermissionApplicable = permissionApplicable,
            postNotificationsPermissionGranted = permissionGranted,
            fullScreenIntentAccessApplicable = fullScreenApplicable,
            fullScreenIntentAllowed = fullScreenAllowed,
        )
    }

    fun notificationSettingsIntent(): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, applicationContext.packageName)

    fun fullScreenIntentSettingsIntent(): Intent? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
        return fullScreenIntentSettingsIntentApi34()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun fullScreenAllowedApi34(notificationManager: NotificationManager): Boolean =
        notificationManager.canUseFullScreenIntent()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun fullScreenIntentSettingsIntentApi34(): Intent =
        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
            .setData(Uri.parse("package:${applicationContext.packageName}"))
}
