package com.goreecloud.dialer.ui

/**
 * Presentation-only GLAZE UI V1.6 mapping for GoreeCloud Dialer.
 *
 * Inputs must come from the caller/platform. This resolver never inspects phone numbers, call
 * content, carrier/SIM identity, Telecom capability state, permission/role state, emergency
 * classification, or any other telephony authority source.
 */
enum class GlazeDialerMaterialRole {
    SOLID,
    RAISED,
    FUNCTIONAL_GLASS,
    CLEAR_GLASS,
}

enum class GlazeDialerPerformanceLevel {
    FULL,
    BALANCED,
    EFFICIENT,
    ESSENTIAL,
}

enum class GlazeDialerMotionMode {
    STANDARD,
    REDUCED,
    MINIMAL,
}

data class GlazeDialerPresentationContext(
    val reducedMotion: Boolean = false,
    val reducedTransparency: Boolean = false,
    val increasedContrast: Boolean = false,
    val largeText: Boolean = false,
    val extraLargeText: Boolean = false,
    val touchAssistance: Boolean = false,
    val strongFocus: Boolean = false,
    val keyboardFirst: Boolean = false,
    val screenReaderOptimized: Boolean = false,
    val performanceLevel: GlazeDialerPerformanceLevel = GlazeDialerPerformanceLevel.FULL,
)

data class GlazeDialerResolvedPresentation(
    val materialRole: GlazeDialerMaterialRole,
    val motionMode: GlazeDialerMotionMode,
    val minimumInteractionTargetDp: Int,
    val focusRingWidthDp: Int,
    val focusRingOffsetDp: Int,
    val densityMayYieldToReflow: Boolean,
    val strongVisibleFocusRequired: Boolean,
)

object GlazeDialerPresentationPolicy {
    const val StableVersion = "1.6.0"
    const val StableSourceRevision = "a7180679ea851389e0f3004515f9a25f420e716d"

    const val InheritedCoarseInteractionFloorDp = 44
    const val InheritedPointerCompactFloorDp = 32
    const val InheritedFocusRingWidthDp = 2
    const val InheritedFocusRingOffsetDp = 2

    // Dialer keeps a stricter touch-oriented floor than the inherited V1.6 coarse target.
    const val InteractionFloorDp = 48
    const val TouchAssistanceFloorDp = 56

    const val TelephonyAuthorityMayBeDerivedFromPresentation = false
    const val CarrierAcceptanceMayBeDerivedFromPresentation = false
    const val EmergencyAuthorityMayBeDerivedFromPresentation = false

    fun resolve(
        requestedMaterial: GlazeDialerMaterialRole,
        context: GlazeDialerPresentationContext,
    ): GlazeDialerResolvedPresentation {
        val material = when {
            context.reducedTransparency &&
                requestedMaterial in setOf(
                    GlazeDialerMaterialRole.FUNCTIONAL_GLASS,
                    GlazeDialerMaterialRole.CLEAR_GLASS,
                ) -> GlazeDialerMaterialRole.SOLID

            context.performanceLevel == GlazeDialerPerformanceLevel.ESSENTIAL &&
                requestedMaterial != GlazeDialerMaterialRole.SOLID ->
                GlazeDialerMaterialRole.SOLID

            context.performanceLevel == GlazeDialerPerformanceLevel.EFFICIENT &&
                requestedMaterial in setOf(
                    GlazeDialerMaterialRole.FUNCTIONAL_GLASS,
                    GlazeDialerMaterialRole.CLEAR_GLASS,
                ) -> GlazeDialerMaterialRole.RAISED

            else -> requestedMaterial
        }

        val motion = when {
            context.reducedMotion ||
                context.performanceLevel == GlazeDialerPerformanceLevel.ESSENTIAL ->
                GlazeDialerMotionMode.MINIMAL

            context.performanceLevel == GlazeDialerPerformanceLevel.EFFICIENT ->
                GlazeDialerMotionMode.REDUCED

            else -> GlazeDialerMotionMode.STANDARD
        }

        return GlazeDialerResolvedPresentation(
            materialRole = material,
            motionMode = motion,
            minimumInteractionTargetDp = if (context.touchAssistance) {
                TouchAssistanceFloorDp
            } else {
                InteractionFloorDp
            },
            focusRingWidthDp = InheritedFocusRingWidthDp,
            focusRingOffsetDp = InheritedFocusRingOffsetDp,
            densityMayYieldToReflow = context.largeText || context.extraLargeText,
            strongVisibleFocusRequired =
                context.strongFocus ||
                    context.keyboardFirst ||
                    context.screenReaderOptimized ||
                    context.increasedContrast,
        )
    }
}
