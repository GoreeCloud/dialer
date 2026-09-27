package com.goreecloud.dialer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goreecloud.dialer.guidance.DialerGuidanceState

internal const val DIALER_CAPABILITY_HINT_ID = "telephony-capability-boundary"

@Composable
internal fun DialerFirstUseWizard(
    state: DialerGuidanceState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onHintsEnabledChanged: (Boolean) -> Unit,
    onComplete: () -> Unit,
) {
    val step = state.setupStep

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                "GoreeCloud Dialer",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Step " + (step + 1) + " of " + (DialerGuidanceState.LAST_SETUP_STEP + 1),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (step) {
                        0 -> {
                            Text(
                                "Welcome to GoreeCloud Dialer",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Dialer is being built to provide a private, accessible phone experience while Android Telecom remains the authority for roles, call routing, and system-level call behavior.",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "This Development build exposes capability evidence and safe setup controls before carrier call placement is enabled.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        1 -> {
                            Text(
                                "Permissions and phone roles stay explicit",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "GoreeCloud Dialer does not grant itself phone, notification, or default-dialer authority. Android owns those decisions and shows the applicable system consent surfaces.",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "Phone-account discovery, incoming-call presentation, multi-SIM routing, and carrier calling remain unavailable or degraded whenever the required Android capability is absent or denied.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        else -> {
                            Text(
                                "Choose helpful guidance",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Short contextual tips can explain telephony capability boundaries as features become relevant. You can disable all ordinary tips, re-enable them, reset dismissed tips, or replay this setup later.",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Text(
                                        "Contextual tips",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        if (state.hintsEnabled) "On" else "Off",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = state.hintsEnabled,
                                    onCheckedChange = onHintsEnabledChanged,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onPrevious,
                    enabled = step > 0,
                ) {
                    Text("Back")
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = if (step == DialerGuidanceState.LAST_SETUP_STEP) onComplete else onNext,
                ) {
                    Text(
                        if (step == DialerGuidanceState.LAST_SETUP_STEP) {
                            "Finish setup"
                        } else {
                            "Continue"
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun DialerCapabilityGuidanceHint(
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "Telephony tip",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                "Capability rows report what Android currently allows. A permission, role, SIM route, or carrier function is not treated as available until Android reports that authority.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
internal fun DialerGuidanceControls(
    state: DialerGuidanceState,
    onHintsEnabledChanged: (Boolean) -> Unit,
    onResetDismissedHints: () -> Unit,
    onReplaySetup: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider()
        Text(
            "Guidance & setup",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            "These controls change guidance only. They never change Android permissions, phone roles, Telecom state, SIM routing, or carrier authority.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "Contextual tips",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    if (state.hintsEnabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = state.hintsEnabled,
                onCheckedChange = onHintsEnabledChanged,
            )
        }
        TextButton(onClick = onResetDismissedHints) {
            Text("Reset dismissed tips")
        }
        TextButton(onClick = onReplaySetup) {
            Text("Replay startup guide")
        }
    }
}
