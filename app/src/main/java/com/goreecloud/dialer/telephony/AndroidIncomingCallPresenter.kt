package com.goreecloud.dialer.telephony

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import com.goreecloud.dialer.R

/**
 * Development incoming-call presentation adapter.
 *
 * This presenter intentionally uses a generic caller label and generated session ID only. Caller
 * identity remains outside the public runtime snapshot until a separate privacy-authorized caller
 * identity projection is designed and accepted.
 */
class AndroidIncomingCallPresenter(
    context: Context,
) {
    private val applicationContext = context.applicationContext

    fun sync(
        sessionId: Long,
        state: CallLifecycleState,
    ): IncomingCallPresentationResult {
        if (state != CallLifecycleState.RINGING) {
            cancel(sessionId)
            return IncomingCallPresentationResult.NotApplicable
        }

        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            ?: return IncomingCallPresentationResult.Failed("Android NotificationManager is unavailable")

        val decision = IncomingCallPresentationPolicy.decide(
            IncomingCallPresentationFacts(
                state = state,
                notificationsAllowed = notificationsAllowed(),
                fullScreenAllowed = fullScreenAllowed(notificationManager),
            ),
        )

        return when (decision) {
            IncomingCallPresentationDecision.NotApplicable -> IncomingCallPresentationResult.NotApplicable
            is IncomingCallPresentationDecision.Blocked ->
                IncomingCallPresentationResult.Blocked(decision.reason)

            is IncomingCallPresentationDecision.Present -> try {
                ensureChannel(notificationManager)
                val contentIntent = activityIntent(sessionId)
                val declineIntent = actionIntent(
                    sessionId = sessionId,
                    action = CallActionReceiver.ACTION_DECLINE,
                    salt = 2,
                )
                val answerIntent = actionIntent(
                    sessionId = sessionId,
                    action = CallActionReceiver.ACTION_ANSWER,
                    salt = 3,
                )
                val caller = Person.Builder()
                    .setName("Incoming call")
                    .setImportant(true)
                    .build()

                val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_call_notification)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setContentTitle("Incoming call")
                    .setContentText("Caller identity is not projected in this Development boundary.")
                    .setContentIntent(contentIntent)
                    .setStyle(
                        NotificationCompat.CallStyle.forIncomingCall(
                            caller,
                            declineIntent,
                            answerIntent,
                        ),
                    )
                    .addPerson(caller)

                if (decision.requestFullScreen) {
                    builder.setFullScreenIntent(contentIntent, true)
                }

                NotificationManagerCompat.from(applicationContext).notify(
                    notificationId(sessionId),
                    builder.build(),
                )
                IncomingCallPresentationResult.Presented(
                    fullScreenRequested = decision.requestFullScreen,
                )
            } catch (securityException: SecurityException) {
                IncomingCallPresentationResult.Failed(
                    "Android rejected incoming-call notification authorization",
                )
            } catch (runtimeException: RuntimeException) {
                IncomingCallPresentationResult.Failed(
                    runtimeException.message?.takeIf { it.isNotBlank() }
                        ?: runtimeException::class.java.simpleName,
                )
            }
        }
    }

    fun cancel(sessionId: Long) {
        NotificationManagerCompat.from(applicationContext).cancel(notificationId(sessionId))
    }

    private fun notificationsAllowed(): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        return runtimePermissionGranted &&
            NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
    }

    private fun fullScreenAllowed(notificationManager: NotificationManager): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationManager.canUseFullScreenIntent()
        } else {
            true
        }

    private fun ensureChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Incoming calls",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Urgent GoreeCloud Dialer incoming-call presentation"
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            setSound(null, null)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun activityIntent(sessionId: Long): PendingIntent = PendingIntent.getActivity(
        applicationContext,
        requestCode(sessionId, 1),
        Intent(applicationContext, IncomingCallActivity::class.java)
            .putExtra(EXTRA_SESSION_ID, sessionId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun actionIntent(
        sessionId: Long,
        action: String,
        salt: Int,
    ): PendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        requestCode(sessionId, salt),
        Intent(applicationContext, CallActionReceiver::class.java)
            .setAction(action)
            .putExtra(EXTRA_SESSION_ID, sessionId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun notificationId(sessionId: Long): Int = requestCode(sessionId, 97)

    private fun requestCode(sessionId: Long, salt: Int): Int =
        ((sessionId * 37L + salt.toLong()) and 0x7FFFFFFFL).toInt().coerceAtLeast(1)

    companion object {
        const val EXTRA_SESSION_ID = "com.goreecloud.dialer.extra.CALL_SESSION_ID"
        private const val CHANNEL_ID = "incoming_calls"
    }
}
