package com.goreecloud.dialer.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeDialerContractTest {
    @Test
    fun sourceMappingPinsCurrentStableV14Authority() {
        assertEquals("1.4.0", GlazeDialerContract.VERSION)
        assertEquals(
            "84cb3db4884042f0fa25ed6d475a127fb110f596",
            GlazeDialerContract.STABLE_SOURCE_REVISION,
        )
        assertEquals("ADOPTION_IN_PROGRESS", GlazeDialerContract.ADOPTION_STATE)
        assertEquals(48, GlazeDialerContract.ORDINARY_INTERACTION_FLOOR_DP)
        assertEquals(56, GlazeDialerContract.TOUCH_ASSISTANCE_FLOOR_DP)
    }

    @Test
    fun applicationAcceptanceRemainsFailClosed() {
        assertFalse(GlazeDialerContract.OPTICAL_ENGINE_ACCEPTED)
        assertFalse(GlazeDialerContract.REDUCED_TRANSPARENCY_ACCEPTED)
        assertFalse(GlazeDialerContract.INCREASED_CONTRAST_ACCEPTED)
        assertFalse(GlazeDialerContract.PHYSICAL_DEVICE_ACCEPTED)
        assertFalse(GlazeDialerContract.HUMAN_VISUAL_ACCEPTED)
    }

    @Test
    fun ordinaryOpticsRemainNeutralSemanticAndContentIndependent() {
        val state = GlazeDialerOptics.resolve()

        assertEquals(GlazeDialerOptics.State.Mode.NEUTRAL_OPTICAL, state.mode)
        assertEquals(1f, state.semanticProtection)
        assertEquals(0f, state.environmentalColorMemoryInfluence)
        assertFalse(state.decorativeTintAllowed)
    }

    @Test
    fun accessibilityModesFailClosedWithoutDecorativeAuthority() {
        val reduced = GlazeDialerOptics.resolve(
            GlazeDialerOptics.Accessibility(reducedTransparency = true),
        )
        val forced = GlazeDialerOptics.resolve(
            GlazeDialerOptics.Accessibility(forcedColors = true),
        )
        val contrast = GlazeDialerOptics.resolve(
            GlazeDialerOptics.Accessibility(increasedContrast = true),
        )

        for (state in listOf(reduced, forced)) {
            assertEquals(GlazeDialerOptics.State.Mode.SOLID_ACCESSIBLE, state.mode)
            assertEquals(0f, state.blurScale)
            assertEquals(1f, state.semanticProtection)
            assertFalse(state.decorativeTintAllowed)
        }
        assertTrue(contrast.blurScale < GlazeDialerOptics.resolve().blurScale)
        assertFalse(contrast.decorativeTintAllowed)
    }

    @Test
    fun sensitiveCallingStateCanNeverDriveOptics() {
        assertEquals(0f, GlazeDialerOptics.MAX_ENVIRONMENTAL_COLOR_MEMORY_INFLUENCE)
        assertFalse(GlazeDialerOptics.PHONE_NUMBER_MAY_DRIVE_OPTICS)
        assertFalse(GlazeDialerOptics.CONTACT_IDENTITY_MAY_DRIVE_OPTICS)
        assertFalse(GlazeDialerOptics.SUBSCRIPTION_OR_CARRIER_MAY_DRIVE_OPTICS)
        assertFalse(GlazeDialerOptics.CALL_STATE_MAY_DRIVE_OPTICS)
        assertFalse(GlazeDialerOptics.SECURITY_OR_PRIVACY_STATE_MAY_DRIVE_OPTICS)
        assertFalse(GlazeDialerOptics.REMOTE_CONTEXT_ALLOWED)
        assertFalse(GlazeDialerOptics.TELEMETRY_REQUIRED)
    }
}
