package com.goreecloud.dialer.ui

/**
 * Privacy-safe Android runtime projection into Dialer's GLAZE UI V1.6 presentation context.
 *
 * Only explicit platform presentation signals are accepted. No phone number, call state,
 * carrier/SIM identity, Android Telecom capability, permission/role state, emergency
 * classification, or physical-device/carrier acceptance evidence may enter this projection.
 */
object DialerAndroidGlazeContext {
    const val DefaultFontScale = 1.0f
    const val ExtraLargeTextScale = 2.0f

    fun fromSignals(
        fontScale: Float,
        animatorsEnabled: Boolean,
        touchExplorationEnabled: Boolean,
    ): GlazeDialerPresentationContext {
        val normalized = fontScale.takeIf { it.isFinite() && it > 0f } ?: DefaultFontScale
        return GlazeDialerPresentationContext(
            reducedMotion = !animatorsEnabled,
            largeText = normalized > DefaultFontScale,
            extraLargeText = normalized >= ExtraLargeTextScale,
            touchAssistance = touchExplorationEnabled,
            screenReaderOptimized = touchExplorationEnabled,
        )
    }
}
