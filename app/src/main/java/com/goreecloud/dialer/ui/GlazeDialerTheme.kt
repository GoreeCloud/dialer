package com.goreecloud.dialer.ui

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Repository-local source boundary for GoreeCloud Dialer's current Stable GLAZE UI target.
 *
 * This is a bounded native mapping, not application acceptance. Presentation must never manufacture
 * call authorization, emergency classification, carrier route, role, permission, privacy, security,
 * recording, or account truth.
 */
object GlazeDialerContract {
    const val VERSION = "1.4.1"
    const val STABLE_SOURCE_REVISION = "4fab9da0fad2e5c974e0e66ec88632c61745751c"
    const val ADOPTION_STATE = "ADOPTION_IN_PROGRESS"
    const val ROLLBACK_BASELINE_VERSION = "1.4.0"

    const val ORDINARY_INTERACTION_FLOOR_DP = 48
    const val TOUCH_ASSISTANCE_FLOOR_DP = 56

    const val OPTICAL_ENGINE_ACCEPTED = false
    const val REDUCED_TRANSPARENCY_ACCEPTED = false
    const val INCREASED_CONTRAST_ACCEPTED = false
    const val PHYSICAL_DEVICE_ACCEPTED = false
    const val MANUAL_ASSISTIVE_TECH_ACCEPTED = false
    const val HUMAN_VISUAL_ACCEPTED = false
    const val REPRESENTATIVE_PERFORMANCE_ACCEPTED = false
}

/**
 * Dialer-specific V1.4.1 optical policy.
 *
 * Call/content identity is too sensitive to become visual-environment input. This resolver accepts
 * accessibility state only; it reads no number, contact, subscription, carrier, call, recording,
 * security, privacy, Identity, Sync, network, wallpaper, camera, telemetry, or remote context.
 */
object GlazeDialerOptics {
    const val MAX_ENVIRONMENTAL_COLOR_MEMORY_INFLUENCE = 0f
    const val PHONE_NUMBER_MAY_DRIVE_OPTICS = false
    const val CONTACT_IDENTITY_MAY_DRIVE_OPTICS = false
    const val SUBSCRIPTION_OR_CARRIER_MAY_DRIVE_OPTICS = false
    const val CALL_STATE_MAY_DRIVE_OPTICS = false
    const val SECURITY_OR_PRIVACY_STATE_MAY_DRIVE_OPTICS = false
    const val REMOTE_CONTEXT_ALLOWED = false
    const val TELEMETRY_REQUIRED = false

    data class Accessibility(
        val reducedTransparency: Boolean = false,
        val increasedContrast: Boolean = false,
        val forcedColors: Boolean = false,
    )

    data class State(
        val mode: Mode,
        val blurScale: Float,
        val semanticProtection: Float,
        val decorativeTintAllowed: Boolean,
        val environmentalColorMemoryInfluence: Float,
    ) {
        enum class Mode { NEUTRAL_OPTICAL, SOLID_ACCESSIBLE }
    }

    fun resolve(accessibility: Accessibility = Accessibility()): State {
        if (accessibility.reducedTransparency || accessibility.forcedColors) {
            return State(
                mode = State.Mode.SOLID_ACCESSIBLE,
                blurScale = 0f,
                semanticProtection = 1f,
                decorativeTintAllowed = false,
                environmentalColorMemoryInfluence = 0f,
            )
        }

        return State(
            mode = State.Mode.NEUTRAL_OPTICAL,
            blurScale = if (accessibility.increasedContrast) 0.42f else 0.58f,
            semanticProtection = 1f,
            decorativeTintAllowed = false,
            environmentalColorMemoryInfluence = 0f,
        )
    }
}

private val LightDialerColors = lightColorScheme(
    primary = Color(0xFF356A84),
    onPrimary = Color.White,
    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF151A23),
    surface = Color(0xFFFDFEFF),
    onSurface = Color(0xFF151A23),
)

private val DarkDialerColors = darkColorScheme(
    primary = Color(0xFF8FC4DE),
    onPrimary = Color(0xFF08212D),
    background = Color(0xFF0B0D11),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF15191E),
    onSurface = Color(0xFFF5F7FA),
)

@Composable
fun GlazeDialerTheme(content: @Composable () -> Unit) {
    val dark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES

    MaterialTheme(
        colorScheme = if (dark) DarkDialerColors else LightDialerColors,
        content = content,
    )
}
