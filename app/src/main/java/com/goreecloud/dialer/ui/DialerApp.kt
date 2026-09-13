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
import com.goreecloud.dialer.telephony.TelephonyCapabilitySnapshot

@Composable
fun DialerApp(initialDialRequest: DialRequest? = null) {
    val applicationContext = LocalContext.current.applicationContext
    val capabilitySnapshot = remember(applicationContext) {
        AndroidTelephonyCapabilityProbe(applicationContext).snapshot()
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            DevelopmentHome(
                capabilitySnapshot = capabilitySnapshot,
                initialNumber = initialDialRequest?.number.orEmpty(),
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DevelopmentHome(
    capabilitySnapshot: TelephonyCapabilitySnapshot,
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
