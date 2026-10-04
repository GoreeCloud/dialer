package com.goreecloud.dialer.telephony

object CallDurationPresentationPolicy {
    fun shouldPresent(state: CallLifecycleState): Boolean =
        state == CallLifecycleState.ACTIVE || state == CallLifecycleState.HOLDING

    fun elapsedSeconds(connectTimeMillis: Long?, nowMillis: Long): Long? {
        val connectedAt = connectTimeMillis?.takeIf { it > 0L } ?: return null
        if (nowMillis < connectedAt) return null
        return (nowMillis - connectedAt) / 1_000L
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
