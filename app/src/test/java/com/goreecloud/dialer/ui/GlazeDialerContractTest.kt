package com.goreecloud.dialer.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeDialerContractTest {
    @Test
    fun sourceMappingPinsCurrentStableV141Authority() {
        assertEquals("1.4.1", GlazeDialerContract.VERSION)
        assertEquals(
            "4fab9da0fad2e5c974e0e66ec88632c61745751c",
            GlazeDialerContract.STABLE_SOURCE_REVISION,
        )
        assertEquals("ADOPTION_IN_PROGRESS", GlazeDialerContract.ADOPTION_STATE)
        assertEquals("1.4.0", GlazeDialerContract.ROLLBACK_BASELINE_VERSION)
        assertEquals(48, GlazeDialerContract.ORDINARY_INTERACTION_FLOOR_DP)
        assertEquals(56, GlazeDialerContract.TOUCH_ASSISTANCE_FLOOR_DP)
    }

    @Test
    fun sharedV141QualificationDoesNotFabricateDialerAcceptance() {
        assertFalse(GlazeDialerContract.OPTICAL_ENGINE_ACCEPTED)
        assertFalse(GlazeDialerContract.REDUCED_TRANSPARENCY_ACCEPTED)
        assertFalse(GlazeDialerContract.INCREASED_CONTRAST_ACCEPTED)
        assertFalse(GlazeDialerContract.PHYSICAL_DEVICE_ACCEPTED)
        assertFalse(GlazeDialerContract.MANUAL_ASSISTIVE_TECH_ACCEPTED)
        assertFalse(GlazeDialerContract.HUMAN_VISUAL_ACCEPTED)
        assertFalse(GlazeDialerContract.REPRESENTATIVE_PERFORMANCE_ACCEPTED)
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
