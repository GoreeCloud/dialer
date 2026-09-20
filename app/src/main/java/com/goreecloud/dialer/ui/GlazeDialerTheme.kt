package com.goreecloud.dialer.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * First-party Dialer theme boundary for the exact GLAZE UI V1.6 Stable source.
 *
 * The current Dialer Development shell is intentionally certainty-first and uses solid Material3
 * surfaces. Product-specific rendered color/material tuning remains separately acceptance-gated.
 * This theme provides presentation context only and never owns telephony or carrier authority.
 */
val LocalGlazeDialerPresentationContext = staticCompositionLocalOf {
    GlazeDialerPresentationContext()
}

@Composable
fun GlazeDialerTheme(
    presentationContext: GlazeDialerPresentationContext = GlazeDialerPresentationContext(),
    content: @Composable () -> Unit,
) {
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    CompositionLocalProvider(
        LocalGlazeDialerPresentationContext provides presentationContext,
    ) {
        MaterialTheme(
            colorScheme = colors,
            content = content,
        )
    }
}
