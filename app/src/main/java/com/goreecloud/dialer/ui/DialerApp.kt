package com.goreecloud.dialer.ui

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
import androidx.compose.material3.OutlinedButton
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
import com.goreecloud.dialer.telephony.ActiveSubscription
import com.goreecloud.dialer.telephony.AndroidTelephonyCapabilityProbe
import com.goreecloud.dialer.telephony.DialRequest
import com.goreecloud.dialer.telephony.PreCallRouteReadiness
import com.goreecloud.dialer.telephony.SubscriptionInventoryResult
import com.goreecloud.dialer.telephony.SubscriptionRouteDecision
import com.goreecloud.dialer.telephony.SubscriptionRoutePolicy
import com.goreecloud.dialer.telephony.TelephonyCapabilitySnapshot

@Composable
fun DialerApp(
    initialDialRequest: DialRequest? = null,
    subscriptionInventory: SubscriptionInventoryResult? = null,
    onRequestSubscriptionPermission: () -> Unit = {},
    onRefreshSubscriptionInventory: () -> Unit = {},
    onEvaluatePreCallRoute: (Int?) -> PreCallRouteReadiness = {
        PreCallRouteReadiness.Unavailable("Telecom route evaluator is not connected")
    },
) {
    val applicationContext = LocalContext.current.applicationContext
    val capabilitySnapshot = remember(applicationContext) {
        AndroidTelephonyCapabilityProbe(applicationContext).snapshot()
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            DevelopmentHome(
                capabilitySnapshot = capabilitySnapshot,
                subscriptionInventory = subscriptionInventory,
                initialNumber = initialDialRequest?.number.orEmpty(),
                onRequestSubscriptionPermission = onRequestSubscriptionPermission,
                onRefreshSubscriptionInventory = onRefreshSubscriptionInventory,
                onEvaluatePreCallRoute = onEvaluatePreCallRoute,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DevelopmentHome(
    capabilitySnapshot: TelephonyCapabilitySnapshot,
    subscriptionInventory: SubscriptionInventoryResult?,
    initialNumber: String,
    onRequestSubscriptionPermission: () -> Unit,
    onRefreshSubscriptionInventory: () -> Unit,
    onEvaluatePreCallRoute: (Int?) -> PreCallRouteReadiness,
    modifier: Modifier = Modifier,
) {
    var number by rememberSaveable(initialNumber) { mutableStateOf(initialNumber) }
    var selectedSubscriptionId by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("GoreeCloud Dialer", style = MaterialTheme.typography.headlineMedium)
        Text("Active Development / pre-Stable", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))

        Text(
            text = number.ifEmpty { "Enter a number" },
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(Modifier.height(4.dp))

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

        Spacer(Modifier.height(12.dp))
        SubscriptionInventoryStatus(
            result = subscriptionInventory,
            selectedSubscriptionId = selectedSubscriptionId,
            onSelectSubscription = { selectedSubscriptionId = it },
            onClearSelection = { selectedSubscriptionId = null },
            onRequestPermission = onRequestSubscriptionPermission,
            onRefresh = onRefreshSubscriptionInventory,
            onEvaluatePreCallRoute = onEvaluatePreCallRoute,
        )

        Spacer(Modifier.height(4.dp))
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
private fun SubscriptionInventoryStatus(
    result: SubscriptionInventoryResult?,
    selectedSubscriptionId: Int?,
    onSelectSubscription: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onEvaluatePreCallRoute: (Int?) -> PreCallRouteReadiness,
) {
    Text("SIM inventory", style = MaterialTheme.typography.titleSmall)
    when (result) {
        null -> {
            Text("Inventory has not been read yet.", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onRefresh) { Text("Refresh") }
        }
        SubscriptionInventoryResult.PermissionRequired -> {
            Text(
                "Phone-state permission is required only to discover active carrier subscriptions for explicit multi-SIM routing.",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                "Permission does not place calls, choose a SIM, or change Android defaults.",
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onRequestPermission) { Text("Allow SIM discovery") }
        }
        SubscriptionInventoryResult.Unsupported -> {
            Text("This device cannot provide an accepted SIM inventory.", style = MaterialTheme.typography.bodySmall)
        }
        is SubscriptionInventoryResult.Unavailable -> {
            Text(
                "SIM inventory unavailable — ${result.reason}",
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = onRefresh) { Text("Retry") }
        }
        is SubscriptionInventoryResult.Available -> {
            SubscriptionSelectionStatus(
                subscriptions = result.subscriptions,
                selectedSubscriptionId = selectedSubscriptionId,
                onSelectSubscription = onSelectSubscription,
                onClearSelection = onClearSelection,
                onEvaluatePreCallRoute = onEvaluatePreCallRoute,
            )
            TextButton(onClick = onRefresh) { Text("Refresh SIM inventory") }
        }
    }
}

@Composable
private fun SubscriptionSelectionStatus(
    subscriptions: List<ActiveSubscription>,
    selectedSubscriptionId: Int?,
    onSelectSubscription: (Int) -> Unit,
    onClearSelection: () -> Unit,
    onEvaluatePreCallRoute: (Int?) -> PreCallRouteReadiness,
) {
    val orderedSubscriptions = subscriptions
        .distinctBy { it.subscriptionId }
        .sortedBy { it.subscriptionId }
    val decision = SubscriptionRoutePolicy.decide(
        activeSubscriptions = orderedSubscriptions,
        explicitlySelectedSubscriptionId = selectedSubscriptionId,
        isEmergencyCall = false,
    )
    var telecomReadiness by remember(
        selectedSubscriptionId,
        orderedSubscriptions.map { it.subscriptionId },
    ) {
        mutableStateOf<PreCallRouteReadiness?>(null)
    }

    Text(
        when (orderedSubscriptions.size) {
            0 -> "No active carrier subscriptions were reported."
            1 -> "1 active carrier subscription is available."
            else -> "${orderedSubscriptions.size} active carrier subscriptions are available."
        },
        style = MaterialTheme.typography.bodySmall,
    )

    if (orderedSubscriptions.size > 1) {
        Text(
            "Choose a SIM explicitly for future non-emergency route evaluation.",
            style = MaterialTheme.typography.bodySmall,
        )
        orderedSubscriptions.forEachIndexed { index, subscription ->
            val label = "SIM ${index + 1}"
            if (subscription.subscriptionId == selectedSubscriptionId) {
                Button(onClick = { onSelectSubscription(subscription.subscriptionId) }) {
                    Text("$label selected")
                }
            } else {
                OutlinedButton(onClick = { onSelectSubscription(subscription.subscriptionId) }) {
                    Text("Use $label")
                }
            }
        }
    }

    if (selectedSubscriptionId != null) {
        TextButton(onClick = onClearSelection) { Text("Clear SIM selection") }
    }

    Text(
        text = routePreviewText(decision, orderedSubscriptions),
        style = MaterialTheme.typography.bodySmall,
    )
    Text(
        "This is a non-emergency policy preview only. A future call action must re-read active SIMs and re-run routing policy; emergency routing always defers to Android Telecom.",
        style = MaterialTheme.typography.bodySmall,
    )

    OutlinedButton(
        onClick = {
            telecomReadiness = onEvaluatePreCallRoute(selectedSubscriptionId)
        },
    ) {
        Text("Check Telecom route")
    }
    telecomReadiness?.let { readiness ->
        Text(
            text = preCallReadinessText(readiness, orderedSubscriptions),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            "This check independently re-reads active SIMs and requires one exact enabled Telecom account. It does not authorize or place a call, and account-handle details are not displayed or persisted.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun routePreviewText(
    decision: SubscriptionRouteDecision,
    subscriptions: List<ActiveSubscription>,
): String = when (decision) {
    SubscriptionRouteDecision.DeferEmergencyToPlatform ->
        "Emergency routing is delegated to Android Telecom."
    is SubscriptionRouteDecision.RequiresUserSelection ->
        "Route preview: explicit SIM selection is required."
    is SubscriptionRouteDecision.Unavailable ->
        "Route preview unavailable — ${decision.reason}"
    is SubscriptionRouteDecision.UseSubscription -> {
        val ordinal = subscriptions.indexOfFirst { it.subscriptionId == decision.subscriptionId }
        if (ordinal >= 0) {
            "Route preview: SIM ${ordinal + 1} is currently accepted by the non-emergency policy."
        } else {
            "Route preview unavailable — selected SIM is not in the current inventory."
        }
    }
}

private fun preCallReadinessText(
    readiness: PreCallRouteReadiness,
    subscriptions: List<ActiveSubscription>,
): String = when (readiness) {
    PreCallRouteReadiness.DeferEmergencyToPlatform ->
        "Telecom readiness: emergency routing is delegated to Android Telecom."
    PreCallRouteReadiness.PermissionRequired ->
        "Telecom readiness blocked — phone-state permission is not currently accepted."
    PreCallRouteReadiness.Unsupported ->
        "Telecom readiness blocked — this runtime cannot provide the accepted subscription/account mapping."
    is PreCallRouteReadiness.RequiresUserSelection ->
        "Telecom readiness blocked — choose a SIM explicitly."
    is PreCallRouteReadiness.Unavailable ->
        "Telecom readiness blocked — ${readiness.reason}"
    is PreCallRouteReadiness.Ready -> {
        val ordinal = subscriptions.indexOfFirst { it.subscriptionId == readiness.subscriptionId }
        if (ordinal >= 0) {
            "Telecom readiness: SIM ${ordinal + 1} maps to exactly one enabled call-capable account."
        } else {
            "Telecom readiness blocked — the resolved SIM is not in the displayed inventory."
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
