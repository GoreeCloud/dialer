package com.goreecloud.dialer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.goreecloud.dialer.core.capability.CapabilityState
import com.goreecloud.dialer.telephony.AndroidTelephonyCapabilityProbe
import com.goreecloud.dialer.telephony.CallControlPresentationPolicy
import com.goreecloud.dialer.telephony.CallControlResult
import com.goreecloud.dialer.telephony.CallRuntimeSummary
import com.goreecloud.dialer.telephony.DialRequest
import com.goreecloud.dialer.telephony.InCallRuntimeSnapshot
import com.goreecloud.dialer.telephony.InCallRuntimeStore
import com.goreecloud.dialer.telephony.TelephonyCapabilitySnapshot
import com.goreecloud.dialer.telephony.presentationLabel

@Composable
fun DialerApp(initialDialRequest: DialRequest? = null) {
    val applicationContext = LocalContext.current.applicationContext
    val capabilitySnapshot = remember(applicationContext) {
        AndroidTelephonyCapabilityProbe(applicationContext).snapshot()
    }
    val inCallRuntime by InCallRuntimeStore.snapshots.collectAsState()

    MaterialTheme {
        Scaffold { innerPadding ->
            DevelopmentHome(
                capabilitySnapshot = capabilitySnapshot,
                inCallRuntime = inCallRuntime,
                initialNumber = initialDialRequest?.number.orEmpty(),
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DevelopmentHome(
    capabilitySnapshot: TelephonyCapabilitySnapshot,
    inCallRuntime: InCallRuntimeSnapshot,
    initialNumber: String,
    modifier: Modifier = Modifier,
) {
    var number by rememberSaveable(initialNumber) { mutableStateOf(initialNumber) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("GoreeCloud Dialer", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Active Development / pre-Stable", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp))

        if (inCallRuntime.trackedCallCount > 0) {
            DevelopmentInCallPanel(inCallRuntime)
            Spacer(Modifier.height(20.dp))
        }

        Text(
            text = number.ifEmpty { "Enter a number" },
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(12.dp))

        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#"),
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { digit ->
                    Button(onClick = { number += digit }) {
                        Text(digit)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        TextButton(
            onClick = { if (number.isNotEmpty()) number = number.dropLast(1) },
            enabled = number.isNotEmpty(),
        ) {
            Text("Delete")
        }

        Button(onClick = {}, enabled = false) {
            Text("Call")
        }
        Text(
            "Carrier call placement is not active in this Development build.",
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(Modifier.height(20.dp))
        Text(
            "ACTION_DIAL: ${capabilitySnapshot.dialIntentHandling.describe()}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Default dialer role: ${capabilitySnapshot.defaultDialerRole.describe()}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DevelopmentInCallPanel(snapshot: InCallRuntimeSnapshot) {
    var operationStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Live Telecom sessions", style = MaterialTheme.typography.titleMedium)
        Text(
            "Development control surface — no caller identity or call content is projected.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))

        snapshot.calls.forEach { call ->
            DevelopmentCallControls(
                call = call,
                onResult = { operationStatus = it },
            )
            Spacer(Modifier.height(8.dp))
        }

        operationStatus?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DevelopmentCallControls(
    call: CallRuntimeSummary,
    onResult: (String) -> Unit,
) {
    val actions = CallControlPresentationPolicy.actionsFor(call.state)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Session ${call.sessionId}: ${call.state}")
        if (actions.isEmpty()) {
            Text("No accepted control for this lifecycle state", style = MaterialTheme.typography.bodySmall)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                actions.forEach { action ->
                    Button(
                        onClick = {
                            val result = InCallRuntimeStore.execute(call.sessionId, action)
                            onResult(
                                when (result) {
                                    CallControlResult.Succeeded -> "${action.presentationLabel()}: succeeded"
                                    is CallControlResult.Rejected -> "${action.presentationLabel()}: rejected — ${result.reason}"
                                    is CallControlResult.Failed -> "${action.presentationLabel()}: failed — ${result.reason}"
                                },
                            )
                        },
                    ) {
                        Text(action.presentationLabel())
                    }
                }
            }
        }
    }
}

private fun CapabilityState.describe(): String = when (this) {
    CapabilityState.Unsupported -> "unsupported"
    is CapabilityState.Unavailable -> "unavailable — $reason"
    is CapabilityState.PermissionRequired -> "permission required — $permission"
    is CapabilityState.RoleRequired -> "role required — $role"
    CapabilityState.Available -> "available"
    CapabilityState.Active -> "active"
    is CapabilityState.Failed -> "failed — $reason"
    CapabilityState.Succeeded -> "succeeded"
}
