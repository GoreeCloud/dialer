package com.goreecloud.dialer.telephony

import android.content.Intent

/** Android adapter for ACTION_DIAL. It does not place a call. */
object AndroidDialRequestReader {
    fun read(intent: Intent?): DialRequest? {
        val data = intent?.data
        return DialRequestParser.parse(
            action = intent?.action,
            scheme = data?.scheme,
            schemeSpecificPart = data?.schemeSpecificPart,
        )
    }
}
