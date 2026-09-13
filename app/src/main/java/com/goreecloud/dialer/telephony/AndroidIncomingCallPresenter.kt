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
import com.goreecloud.dialer.MainActivity
import com.goreecloud.dialer.R

/**
 * Development call-notification presenter.
 *
 * The class name reflects its original incoming-call scope, but it now preserves presentation
 * continuity into ongoing call states as well. Caller identity remains deliberately absent from
 * the notification contract until a separate privacy-authorized identity projection exists.
 */
class AndroidIncomingCallPresenter(
    context: Context,
) {
    private val applicationContext = context.applicationContext

    fun sync(
        sessionId: Long,
        state: CallLifecycleState,
    ): IncomingCallPresentationResult = when (CallNotificationModeResolver.resolve(state)) {
        CallNotificationMode.NONE -> {
            cancel(sessionId)
            IncomingCallPresentationResult.NotApplicable
        }

        CallNotificationMode.INCOMING -> presentIncoming(sessionId, state)
        CallNotificationMode.ONGOING -> presentOngoing(sessionId)
    }

    fun cancel(sessionId: Long) {
        NotificationManagerCompat.from(applicationContext).cancel(notificationId(sessionId))
    }

    private fun presentIncoming(
        sessionId: Long,
        state: CallLifecycleState,
    ): IncomingCallPresentationResult {
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
                val contentIntent = incomingActivityIntent(sessionId)
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
                val caller = genericPerson("Incoming call")

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

                post(sessionId, builder.build())
                IncomingCallPresentationResult.Presented(
                    fullScreenRequested = decision.requestFullScreen,
                )
            } catch (securityException: SecurityException) {
                IncomingCallPresentationResult.Failed(
                    "Android rejected incoming-call notification authorization",
                )
            } catch (runtimeException: RuntimeException) {
                IncomingCallPresentationResult.Failed(runtimeException.safeMessage())
            }
        }
    }

    private fun presentOngoing(sessionId: Long): IncomingCallPresentationResult {
        if (!notificationsAllowed()) {
            return IncomingCallPresentationResult.Blocked(
                "Ongoing-call notifications are not allowed",
            )
        }
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            ?: return IncomingCallPresentationResult.Failed("Android NotificationManager is unavailable")

        return try {
            ensureChannel(notificationManager)
            val contentIntent = mainActivityIntent(sessionId)
            val endIntent = actionIntent(
                sessionId = sessionId,
                action = CallActionReceiver.ACTION_END,
                salt = 4,
            )
            val caller = genericPerson("Active call")
            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_call_notification)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentTitle("Call in progress")
                .setContentText("Open GoreeCloud Dialer for accepted call controls.")
                .setContentIntent(contentIntent)
                .setStyle(
                    NotificationCompat.CallStyle.forOngoingCall(
                        caller,
                        endIntent,
                    ),
                )
                .addPerson(caller)
                .build()
            post(sessionId, notification)
            IncomingCallPresentationResult.Presented(fullScreenRequested = false)
        } catch (securityException: SecurityException) {
            IncomingCallPresentationResult.Failed(
                "Android rejected ongoing-call notification authorization",
            )
        } catch (runtimeException: RuntimeException) {
            IncomingCallPresentationResult.Failed(runtimeException.safeMessage())
        }
    }

    private fun post(sessionId: Long, notification: Notification) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("POST_NOTIFICATIONS is not granted")
        }
        NotificationManagerCompat.from(applicationContext).notify(
            notificationId(sessionId),
            notification,
        )
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
            "Calls",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Urgent and ongoing GoreeCloud Dialer call presentation"
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            setSound(null, null)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun genericPerson(label: String): Person = Person.Builder()
        .setName(label)
        .setImportant(true)
        .build()

    private fun incomingActivityIntent(sessionId: Long): PendingIntent = PendingIntent.getActivity(
        applicationContext,
        requestCode(sessionId, 1),
        Intent(applicationContext, IncomingCallActivity::class.java)
            .putExtra(EXTRA_SESSION_ID, sessionId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun mainActivityIntent(sessionId: Long): PendingIntent = PendingIntent.getActivity(
        applicationContext,
        requestCode(sessionId, 5),
        Intent(applicationContext, MainActivity::class.java)
            .putExtra(EXTRA_SESSION_ID, sessionId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
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

    private fun RuntimeException.safeMessage(): String =
        message?.takeIf { it.isNotBlank() } ?: this::class.java.simpleName

    companion object {
        const val EXTRA_SESSION_ID = "com.goreecloud.dialer.extra.CALL_SESSION_ID"
        private const val CHANNEL_ID = "incoming_calls"
    }
}
