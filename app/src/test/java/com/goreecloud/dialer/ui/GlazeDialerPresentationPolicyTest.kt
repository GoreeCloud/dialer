package com.goreecloud.dialer.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlazeDialerPresentationPolicyTest {
    @Test
    fun exactStableV16AuthorityIsPinned() {
        assertEquals("1.6.0", GlazeDialerPresentationPolicy.StableVersion)
        assertEquals(
            "a7180679ea851389e0f3004515f9a25f420e716d",
            GlazeDialerPresentationPolicy.StableSourceRevision,
        )
        assertEquals(44, GlazeDialerPresentationPolicy.InheritedCoarseInteractionFloorDp)
        assertEquals(32, GlazeDialerPresentationPolicy.InheritedPointerCompactFloorDp)
        assertEquals(2, GlazeDialerPresentationPolicy.InheritedFocusRingWidthDp)
        assertEquals(2, GlazeDialerPresentationPolicy.InheritedFocusRingOffsetDp)
    }

    @Test
    fun dialerKeepsStricterTouchTargetsThanInheritedCoarseFloor() {
        assertEquals(48, GlazeDialerPresentationPolicy.InteractionFloorDp)
        assertEquals(56, GlazeDialerPresentationPolicy.TouchAssistanceFloorDp)
        assertTrue(
            GlazeDialerPresentationPolicy.InteractionFloorDp >
                GlazeDialerPresentationPolicy.InheritedCoarseInteractionFloorDp,
        )
    }

    @Test
    fun reducedTransparencyFailsGlassDownToSolid() {
        val resolved = GlazeDialerPresentationPolicy.resolve(
            requestedMaterial = GlazeDialerMaterialRole.FUNCTIONAL_GLASS,
            context = GlazeDialerPresentationContext(reducedTransparency = true),
        )

        assertEquals(GlazeDialerMaterialRole.SOLID, resolved.materialRole)
        assertEquals(GlazeDialerMotionMode.STANDARD, resolved.motionMode)
    }

    @Test
    fun essentialPerformanceUsesSolidMinimalPresentation() {
        val resolved = GlazeDialerPresentationPolicy.resolve(
            requestedMaterial = GlazeDialerMaterialRole.RAISED,
            context = GlazeDialerPresentationContext(
                performanceLevel = GlazeDialerPerformanceLevel.ESSENTIAL,
            ),
        )

        assertEquals(GlazeDialerMaterialRole.SOLID, resolved.materialRole)
        assertEquals(GlazeDialerMotionMode.MINIMAL, resolved.motionMode)
    }

    @Test
    fun touchAssistanceKeepsLargerTargetWithoutCreatingTelephonyAuthority() {
        val resolved = GlazeDialerPresentationPolicy.resolve(
            requestedMaterial = GlazeDialerMaterialRole.SOLID,
            context = GlazeDialerPresentationContext(touchAssistance = true),
        )

        assertEquals(56, resolved.minimumInteractionTargetDp)
        assertFalse(GlazeDialerPresentationPolicy.TelephonyAuthorityMayBeDerivedFromPresentation)
        assertFalse(GlazeDialerPresentationPolicy.CarrierAcceptanceMayBeDerivedFromPresentation)
        assertFalse(GlazeDialerPresentationPolicy.EmergencyAuthorityMayBeDerivedFromPresentation)
    }

    @Test
    fun largeTextYieldsDensityAndKeyboardRequiresVisibleFocus() {
        val resolved = GlazeDialerPresentationPolicy.resolve(
            requestedMaterial = GlazeDialerMaterialRole.SOLID,
            context = GlazeDialerPresentationContext(
                largeText = true,
                keyboardFirst = true,
            ),
        )

        assertTrue(resolved.densityMayYieldToReflow)
        assertTrue(resolved.strongVisibleFocusRequired)
    }

    @Test
    fun neutralContextDoesNotInventAccessibilityOrPerformanceState() {
        val context = GlazeDialerPresentationContext()

        assertFalse(context.reducedMotion)
        assertFalse(context.reducedTransparency)
        assertFalse(context.increasedContrast)
        assertFalse(context.touchAssistance)
        assertEquals(GlazeDialerPerformanceLevel.FULL, context.performanceLevel)
    }
}
