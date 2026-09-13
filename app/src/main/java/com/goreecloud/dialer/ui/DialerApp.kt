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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.goreecloud.dialer.core.capability.CapabilityState
import com.goreecloud.dialer.telephony.AndroidDefaultDialerRoleRequestPreparer
import com.goreecloud.dialer.telephony.AndroidTelephonyCapabilityProbe
import com.goreecloud.dialer.telephony.DefaultDialerRoleRequestPreparation
import com.goreecloud.dialer.telephony.DialRequest
import com.goreecloud.dialer.telephony.InCallRuntimeStore
import com.goreecloud.dialer.telephony.PhoneAccountDiscoveryState
import com.goreecloud.dialer.telephony.PhoneAccountRoutingRuntime
import com.goreecloud.dialer.telephony.TelephonyCapabilitySnapshot

@Composable
fun DialerApp(initialDialRequest: DialRequest? = null) {
    val applicationContext = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    var runtimeRefresh by remember { mutableIntStateOf(0) }
    var roleRequestMessage by remember { mutableStateOf<String?>(null) }
    val capabilitySnapshot = remember(applicationContext, runtimeRefresh) {
        AndroidTelephonyCapabilityProbe(applicationContext).snapshot()
    }
    val inCallRuntime by InCallRuntimeStore.snapshots.collectAsState()
    var selectedPhoneAccountRouteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val phoneAccountDiscovery = remember(applicationContext, runtimeRefresh) {
        PhoneAccountRoutingRuntime.discover(applicationContext)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                runtimeRefresh += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val phoneStatePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        runtimeRefresh += 1
    }
    val defaultDialerRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        runtimeRefresh += 1
        roleRequestMessage = "Android role request returned; capability evidence refreshed."
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            DevelopmentHome(
                capabilitySnapshot = capabilitySnapshot,
                inCallRuntime = inCallRuntime,
                phoneAccountDiscovery = phoneAccountDiscovery,
                selectedPhoneAccountRouteId = selectedPhoneAccountRouteId,
                roleRequestMessage = roleRequestMessage,
                onPhoneAccountSelected = { selectedPhoneAccountRouteId = it },
                onRequestPhoneStatePermission = {
                    phoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                },
                onRequestDefaultDialerRole = {
                    when (
                        val preparation = AndroidDefaultDialerRoleRequestPreparer(
                            applicationContext,
                        ).prepare()
                    ) {
                        DefaultDialerRoleRequestPreparation.AlreadyHeld -> {
                            roleRequestMessage = "Default dialer role is already active."
                            runtimeRefresh += 1
                        }

                        is DefaultDialerRoleRequestPreparation.Prepared -> {
                            roleRequestMessage = "Opening Android's explicit role-consent prompt."
                            defaultDialerRoleLauncher.launch(preparation.intent)
                        }

                        is DefaultDialerRoleRequestPreparation.Rejected -> {
                            roleRequestMessage = "Role request blocked — ${preparation.reason}"
                        }
                    }
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
    inCallRuntime: com.goreecloud.dialer.telephony.InCallRuntimeSnapshot,
    phoneAccountDiscovery: PhoneAccountDiscoveryState,
    selectedPhoneAccountRouteId: Long?,
    roleRequestMessage: String?,
    onPhoneAccountSelected: (Long?) -> Unit,
    onRequestPhoneStatePermission: () -> Unit,
    onRequestDefaultDialerRole: () -> Unit,
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
        DevelopmentDefaultDialerRoleRequest(
            state = capabilitySnapshot.defaultDialerRole,
            message = roleRequestMessage,
            onRequest = onRequestDefaultDialerRole,
        )

        Spacer(Modifier.height(20.dp))
        Text("Telephony capability evidence", style = MaterialTheme.typography.titleSmall)
        CapabilityEvidenceRow("ACTION_DIAL", capabilitySnapshot.dialIntentHandling)
        CapabilityEvidenceRow("Default dialer role", capabilitySnapshot.defaultDialerRole)
        CapabilityEvidenceRow("Outgoing calls", capabilitySnapshot.outgoingCalls)
        CapabilityEvidenceRow("Multi-SIM routing", capabilitySnapshot.multiSimRouting)
        CapabilityEvidenceRow("Wi-Fi Calling state", capabilitySnapshot.wifiCallingState)
        CapabilityEvidenceRow("Supplementary services", capabilitySnapshot.supplementaryServices)
    }
}

@Composable
private fun DevelopmentDefaultDialerRoleRequest(
    state: CapabilityState,
    message: String?,
    onRequest: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Default phone role", style = MaterialTheme.typography.titleSmall)
        when (state) {
            CapabilityState.Active -> Text(
                "Android reports GoreeCloud Dialer as the active default dialer.",
                style = MaterialTheme.typography.bodySmall,
            )

            is CapabilityState.RoleRequired -> {
                Button(onClick = onRequest) {
                    Text("Request default dialer role")
                }
                Text(
                    "Android will display its explicit user-consent prompt before the role can change.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            else -> {
                Button(onClick = {}, enabled = false) {
                    Text("Default dialer request blocked")
                }
                Text(
                    state.describe(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        message?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CapabilityEvidenceRow(
    label: String,
    state: CapabilityState,
) {
    Text(
        "$label: ${state.describe()}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
    )
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
