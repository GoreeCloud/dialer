package com.goreecloud.dialer.telephony

/**
 * User-visible dial input received from Android. The value is kept verbatim;
 * normalization and carrier placement belong to later, independently accepted layers.
 */
data class DialRequest(
    val number: String?,
)

object DialRequestParser {
    const val ACTION_DIAL = "android.intent.action.DIAL"
    const val TEL_SCHEME = "tel"

    fun parse(
        action: String?,
        scheme: String?,
        schemeSpecificPart: String?,
    ): DialRequest? {
        if (action != ACTION_DIAL) return null
        if (scheme == null) return DialRequest(number = null)
        if (!scheme.equals(TEL_SCHEME, ignoreCase = true)) return null
        return DialRequest(number = schemeSpecificPart?.takeIf { it.isNotBlank() })
    }
}
