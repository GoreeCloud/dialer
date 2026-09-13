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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.goreecloud.dialer.telephony.DialRequest
import com.goreecloud.dialer.telephony.SubscriptionInventoryGateway
import com.goreecloud.dialer.telephony.SubscriptionInventoryResult
import com.goreecloud.dialer.telephony.SubscriptionRouteCoordinator
import com.goreecloud.dialer.telephony.SubscriptionRouteDecision
import com.goreecloud.dialer.telephony.SubscriptionRouteReadiness
import com.goreecloud.dialer.telephony.TelephonyCapabilitySnapshot

@Composable
fun DialerApp(initialDialRequest: DialRequest? = null) {
    val applicationContext = LocalContext.current.applicationContext
    val capabilitySnapshot = remember(applicationContext) {
        AndroidTelephonyCapabilityProbe(applicationContext).snapshot()
    }
    val subscriptionGateway = remember(applicationContext) {
        SubscriptionInventoryGateway(applicationContext)
    }
    var subscriptionInventory by remember(subscriptionGateway) {
        mutableStateOf(subscriptionGateway.read())
    }
    var selectedSubscriptionId by rememberSaveable { mutableStateOf<Int?>(null) }
    val requestPhoneStatePermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        subscriptionInventory = if (granted) {
            subscriptionGateway.read()
        } else {
            SubscriptionInventoryResult.PermissionRequired
        }
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            DevelopmentHome(
                capabilitySnapshot = capabilitySnapshot,
                initialNumber = initialDialRequest?.number.orEmpty(),
                subscriptionInventory = subscriptionInventory,
                selectedSubscriptionId = selectedSubscriptionId,
                onRequestSubscriptionPermission = {
                    requestPhoneStatePermission.launch(Manifest.permission.READ_PHONE_STATE)
                },
                onRefreshSubscriptionInventory = {
                    subscriptionInventory = subscriptionGateway.read()
                },
                onSelectSubscription = { selectedSubscriptionId = it },
                onClearSubscriptionSelection = { selectedSubscriptionId = null },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DevelopmentHome(
    capabilitySnapshot: TelephonyCapabilitySnapshot,
    initialNumber: String,
    subscriptionInventory: SubscriptionInventoryResult,
    selectedSubscriptionId: Int?,
    onRequestSubscriptionPermission: () -> Unit,
    onRefreshSubscriptionInventory: () -> Unit,
    onSelectSubscription: (Int) -> Unit,
    onClearSubscriptionSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var number by rememberSaveable(initialNumber) { mutableStateOf(initialNumber) }
    val nonEmergencyReadiness = SubscriptionRouteCoordinator.evaluate(
        inventory = subscriptionInventory,
        explicitlySelectedSubscriptionId = selectedSubscriptionId,
        isEmergencyCall = false,
    )

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("GoreeCloud Dialer", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text("Active Development / pre-Stable", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp))

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
        SubscriptionReadinessPanel(
            inventory = subscriptionInventory,
            selectedSubscriptionId = selectedSubscriptionId,
            readiness = nonEmergencyReadiness,
            onRequestPermission = onRequestSubscriptionPermission,
            onRefresh = onRefreshSubscriptionInventory,
            onSelectSubscription = onSelectSubscription,
            onClearSelection = onClearSubscriptionSelection,
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
private fun SubscriptionReadinessPanel(
    inventory: SubscriptionInventoryResult,
    selectedSubscriptionId: Int?,
    readiness: SubscriptionRouteReadiness,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onSelectSubscription: (Int) -> Unit,
    onClearSelection: () -> Unit,
) {
    Text("Carrier route readiness", style = MaterialTheme.typography.titleMedium)
    Text(
        "SIM access is requested only after explicit user action. This is a non-emergency route preview; it cannot place calls.",
        style = MaterialTheme.typography.bodySmall,
    )

    when (inventory) {
        SubscriptionInventoryResult.PermissionRequired -> {
            Button(onClick = onRequestPermission) { Text("Allow SIM access") }
        }
        SubscriptionInventoryResult.Unsupported -> {
            Text("Active subscription inventory is unsupported on this device/runtime.")
            TextButton(onClick = onRefresh) { Text("Refresh SIM state") }
        }
        is SubscriptionInventoryResult.Unavailable -> {
            Text("Subscription inventory unavailable — ${inventory.reason}")
            TextButton(onClick = onRefresh) { Text("Refresh SIM state") }
        }
        is SubscriptionInventoryResult.Available -> {
            val ids = inventory.subscriptions.map { it.subscriptionId }
            Text(
                if (ids.isEmpty()) "No active carrier subscriptions reported."
                else "Active subscription IDs: ${ids.joinToString()}",
            )
            if (ids.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ids.forEach { id ->
                        TextButton(onClick = { onSelectSubscription(id) }) {
                            Text(if (selectedSubscriptionId == id) "SIM $id selected" else "Select SIM $id")
                        }
                    }
                }
                if (selectedSubscriptionId != null) {
                    TextButton(onClick = onClearSelection) { Text("Clear SIM selection") }
                }
            }
            TextButton(onClick = onRefresh) { Text("Refresh SIM state") }
        }
    }

    Text(
        "Non-emergency route preview: ${readiness.describe()}",
        style = MaterialTheme.typography.bodyMedium,
    )
}

private fun SubscriptionRouteReadiness.describe(): String = when (this) {
    SubscriptionRouteReadiness.PermissionRequired -> "permission required"
    SubscriptionRouteReadiness.Unsupported -> "unsupported"
    is SubscriptionRouteReadiness.Unavailable -> "unavailable — $reason"
    is SubscriptionRouteReadiness.Decision -> when (val route = decision) {
        SubscriptionRouteDecision.DeferEmergencyToPlatform -> "Android Telecom must route emergency calls"
        is SubscriptionRouteDecision.UseSubscription -> "subscription ${route.subscriptionId} is eligible"
        is SubscriptionRouteDecision.RequiresUserSelection ->
            "explicit SIM selection required from ${route.subscriptionIds.joinToString()}"
        is SubscriptionRouteDecision.Unavailable -> "unavailable — ${route.reason}"
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
