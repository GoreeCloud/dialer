package com.goreecloud.dialer.telephony

object CallDurationPresentationPolicy {
    fun shouldPresent(state: CallLifecycleState): Boolean =
        state == CallLifecycleState.ACTIVE || state == CallLifecycleState.HOLDING

    fun monotonicAnchorMillis(
        connectTimeMillis: Long?,
        wallClockNowMillis: Long,
        elapsedRealtimeNowMillis: Long,
    ): Long? {
        val connectedAtWallClock = connectTimeMillis?.takeIf { it > 0L } ?: return null
        if (wallClockNowMillis < connectedAtWallClock || elapsedRealtimeNowMillis < 0L) return null
        val elapsedSinceConnect = wallClockNowMillis - connectedAtWallClock
        if (elapsedSinceConnect > elapsedRealtimeNowMillis) return 0L
        return elapsedRealtimeNowMillis - elapsedSinceConnect
    }

    fun elapsedSeconds(
        connectedAtElapsedRealtimeMillis: Long?,
        elapsedRealtimeNowMillis: Long,
    ): Long? {
        val connectedAt = connectedAtElapsedRealtimeMillis?.takeIf { it >= 0L } ?: return null
        if (elapsedRealtimeNowMillis < connectedAt) return null
        return (elapsedRealtimeNowMillis - connectedAt) / 1_000L
    }

    fun label(elapsedSeconds: Long): String {
        val total = elapsedSeconds.coerceAtLeast(0L)
        val hours = total / 3_600L
        val minutes = (total % 3_600L) / 60L
        val seconds = total % 60L
        val duration = if (hours > 0L) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
        return "Connected • $duration"
    }
}
