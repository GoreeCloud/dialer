package com.goreecloud.dialer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.goreecloud.dialer.core.capability.CapabilityState
import com.goreecloud.dialer.telephony.AndroidTelephonyCapabilityProbe
import com.goreecloud.dialer.telephony.TelephonyCapabilitySnapshot

@Composable
fun DialerApp() {
    val applicationContext = LocalContext.current.applicationContext
    val capabilitySnapshot = remember(applicationContext) {
        AndroidTelephonyCapabilityProbe(applicationContext).snapshot()
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            DevelopmentHome(
                capabilitySnapshot = capabilitySnapshot,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DevelopmentHome(
    capabilitySnapshot: TelephonyCapabilitySnapshot,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("GoreeCloud Dialer", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Active Development / pre-Stable", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            "Native application foundation is active. Carrier telephony and intelligent calling are not yet connected.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))
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
