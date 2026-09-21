package com.goreecloud.dialer.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DialerAndroidGlazeContextTest {
    @Test
    fun platformSignalsMapOnlyToPresentationContext() {
        val context = DialerAndroidGlazeContext.fromSignals(
            fontScale = 2.0f,
            animatorsEnabled = false,
            touchExplorationEnabled = true,
        )

        assertTrue(context.reducedMotion)
        assertTrue(context.largeText)
        assertTrue(context.extraLargeText)
        assertTrue(context.touchAssistance)
        assertTrue(context.screenReaderOptimized)
        assertFalse(context.reducedTransparency)
        assertFalse(context.increasedContrast)
    }

    @Test
    fun invalidFontScaleFailsBackToNeutralScale() {
        val context = DialerAndroidGlazeContext.fromSignals(
            fontScale = Float.NaN,
            animatorsEnabled = true,
            touchExplorationEnabled = false,
        )

        assertFalse(context.reducedMotion)
        assertFalse(context.largeText)
        assertFalse(context.extraLargeText)
        assertFalse(context.touchAssistance)
        assertFalse(context.screenReaderOptimized)
    }
}
