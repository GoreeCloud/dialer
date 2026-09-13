package com.goreecloud.dialer.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.goreecloud.dialer.telephony.CallAudioControlAction
import com.goreecloud.dialer.telephony.CallAudioControlResult
import com.goreecloud.dialer.telephony.CallControlPresentationPolicy
import com.goreecloud.dialer.telephony.CallControlResult
import com.goreecloud.dialer.telephony.CallEndpointRequestState
import com.goreecloud.dialer.telephony.CallEndpointRoutingResult
import com.goreecloud.dialer.telephony.CallEndpointRuntimeSummary
import com.goreecloud.dialer.telephony.CallLifecycleState
import com.goreecloud.dialer.telephony.CallRuntimeSummary
import com.goreecloud.dialer.telephony.DialRequest
import com.goreecloud.dialer.telephony.InCallAudioControlRuntime
import com.goreecloud.dialer.telephony.InCallEndpointRoutingRuntime
import com.goreecloud.dialer.telephony.InCallRuntimeSnapshot
import com.goreecloud.dialer.telephony.InCallRuntimeStore
import com.goreecloud.dialer.telephony.PhoneAccountDiscoveryState
import com.goreecloud.dialer.telephony.PhoneAccountRoutingRuntime
import com.goreecloud.dialer.telephony.TelephonyCapabilitySnapshot
import com.goreecloud.dialer.telephony.presentationLabel

@Composable
fun DialerApp(initialDialRequest: DialRequest? = null) {
    val applicationContext = LocalContext.current.applicationContext
    val capabilitySnapshot = remember(applicationContext) {
        AndroidTelephonyCapabilityProbe(applicationContext).snapshot()
    }
    val inCallRuntime by InCallRuntimeStore.snapshots.collectAsState()
    var phoneAccountRefresh by remember { mutableStateOf(0) }
    var selectedPhoneAccountRouteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val phoneAccountDiscovery = remember(applicationContext, phoneAccountRefresh) {
        PhoneAccountRoutingRuntime.discover(applicationContext)
    }
    val phoneStatePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        phoneAccountRefresh += 1
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            DevelopmentHome(
                capabilitySnapshot = capabilitySnapshot,
                inCallRuntime = inCallRuntime,
                phoneAccountDiscovery = phoneAccountDiscovery,
                selectedPhoneAccountRouteId = selectedPhoneAccountRouteId,
                onPhoneAccountSelected = { selectedPhoneAccountRouteId = it },
                onRequestPhoneStatePermission = {
                    phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                },
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
    phoneAccountDiscovery: PhoneAccountDiscoveryState,
    selectedPhoneAccountRouteId: Long?,
    onPhoneAccountSelected: (Long?) -> Unit,
    onRequestPhoneStatePermission: () -> Unit,
    initialNumber: String,
    modifier: Modifier = Modifier,
) {
    var number by rememberSaveable(initialNumber) { mutableStateOf(initialNumber) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
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

        DevelopmentPhoneAccountRouting(
            discovery = phoneAccountDiscovery,
            selectedRouteId = selectedPhoneAccountRouteId,
            onSelected = onPhoneAccountSelected,
            onRequestPermission = onRequestPhoneStatePermission,
        )
        Spacer(Modifier.height(12.dp))

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
private fun DevelopmentPhoneAccountRouting(
    discovery: PhoneAccountDiscoveryState,
    selectedRouteId: Long?,
    onSelected: (Long?) -> Unit,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Calling account", style = MaterialTheme.typography.titleSmall)
        Text(
            "Phone numbers, carrier labels, SIM identifiers, and account names are not projected.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))

        when (discovery) {
            PhoneAccountDiscoveryState.Unsupported -> Text(
                "Telephony account routing is unsupported on this device.",
                style = MaterialTheme.typography.bodySmall,
            )

            PhoneAccountDiscoveryState.TelecomUnavailable -> Text(
                "Android Telecom is unavailable.",
                style = MaterialTheme.typography.bodySmall,
            )

            is PhoneAccountDiscoveryState.PermissionRequired -> {
                Text(
                    "READ_PHONE_STATE is required to enumerate call-capable phone accounts.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Button(onClick = onRequestPermission) {
                    Text("Allow phone account discovery")
                }
            }

            is PhoneAccountDiscoveryState.Failed -> Text(
                "Phone account discovery failed — ${discovery.reason}",
                style = MaterialTheme.typography.bodySmall,
            )

            is PhoneAccountDiscoveryState.Available -> {
                Button(
                    onClick = { onSelected(null) },
                    enabled = selectedRouteId != null,
                ) {
                    Text(if (selectedRouteId == null) "System default ✓" else "System default")
                }

                if (discovery.routes.isEmpty()) {
                    Text(
                        "No enabled call-capable phone accounts were reported by Android Telecom.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    discovery.routes.forEachIndexed { index, route ->
                        val selected = route.routeId == selectedRouteId
                        val defaultSuffix = if (route.isSystemDefault) " · Android default" else ""
                        Button(
                            onClick = { onSelected(route.routeId) },
                            enabled = !selected,
                        ) {
                            Text(
                                if (selected) {
                                    "Phone account ${index + 1}$defaultSuffix ✓"
                                } else {
                                    "Phone account ${index + 1}$defaultSuffix"
                                },
                            )
                        }
                    }
                }

                if (
                    selectedRouteId != null &&
                    discovery.routes.none { it.routeId == selectedRouteId }
                ) {
                    Text(
                        "The selected phone account is no longer available; placement would fail closed.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Emergency or indeterminate-emergency calls always delegate phone-account routing to Android Telecom.",
            style = MaterialTheme.typography.bodySmall,
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
            "Development control surface — no caller identity, endpoint device name, or call content is projected.",
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

        val audioEligible = snapshot.calls.any {
            it.state == CallLifecycleState.ACTIVE || it.state == CallLifecycleState.HOLDING
        }
        if (audioEligible) {
            DevelopmentAudioControls(
                snapshot = snapshot,
                onResult = { operationStatus = it },
            )
        }

        operationStatus?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DevelopmentAudioControls(
    snapshot: InCallRuntimeSnapshot,
    onResult: (String) -> Unit,
) {
    when (val muted = snapshot.isMuted) {
        null -> Text(
            "Mute state is awaiting Telecom evidence",
            style = MaterialTheme.typography.bodySmall,
        )
        else -> Button(
            onClick = {
                val action = if (muted) {
                    CallAudioControlAction.Unmute
                } else {
                    CallAudioControlAction.Mute
                }
                onResult(InCallAudioControlRuntime.execute(action).message(action))
            },
        ) {
            Text(if (muted) "Unmute" else "Mute")
        }
    }

    Spacer(Modifier.height(8.dp))
    if (!snapshot.endpointRoutingSupported) {
        Text(
            "Endpoint routing requires Android 14+ CallEndpoint support",
            style = MaterialTheme.typography.bodySmall,
        )
    } else if (snapshot.availableEndpoints.isEmpty()) {
        Text(
            "Waiting for Telecom endpoint evidence",
            style = MaterialTheme.typography.bodySmall,
        )
    } else {
        Text("Audio endpoint", style = MaterialTheme.typography.titleSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            snapshot.availableEndpoints.forEach { endpoint ->
                EndpointButton(
                    endpoint = endpoint,
                    selected = endpoint.routeId == snapshot.currentEndpointId,
                    onResult = onResult,
                )
            }
        }
        snapshot.lastEndpointRequest?.let { request ->
            val text = when (request.state) {
                CallEndpointRequestState.SUBMITTED -> "Endpoint request submitted"
                CallEndpointRequestState.SUCCEEDED -> "Endpoint request succeeded"
                CallEndpointRequestState.FAILED ->
                    "Endpoint request failed — ${request.reason ?: "unknown reason"}"
            }
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EndpointButton(
    endpoint: CallEndpointRuntimeSummary,
    selected: Boolean,
    onResult: (String) -> Unit,
) {
    Button(
        enabled = !selected,
        onClick = {
            onResult(
                when (val result = InCallEndpointRoutingRuntime.request(endpoint.routeId)) {
                    CallEndpointRoutingResult.Submitted -> "${endpoint.kind}: route request submitted"
                    is CallEndpointRoutingResult.Rejected ->
                        "${endpoint.kind}: rejected — ${result.reason}"
                    is CallEndpointRoutingResult.Failed ->
                        "${endpoint.kind}: failed — ${result.reason}"
                },
            )
        },
    ) {
        Text(if (selected) "${endpoint.kind} ✓" else endpoint.kind.toString())
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

private fun CallAudioControlResult.message(action: CallAudioControlAction): String {
    val label = if (action == CallAudioControlAction.Mute) "Mute" else "Unmute"
    return when (this) {
        CallAudioControlResult.Succeeded -> "$label: succeeded"
        is CallAudioControlResult.Rejected -> "$label: rejected — $reason"
        is CallAudioControlResult.Failed -> "$label: failed — $reason"
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
