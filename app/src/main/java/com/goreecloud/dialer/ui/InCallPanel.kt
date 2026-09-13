package com.goreecloud.dialer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goreecloud.dialer.telephony.CallAudioControlAction
import com.goreecloud.dialer.telephony.CallAudioControlResult
import com.goreecloud.dialer.telephony.CallControlPresentationPolicy
import com.goreecloud.dialer.telephony.CallControlResult
import com.goreecloud.dialer.telephony.CallEndpointRequestState
import com.goreecloud.dialer.telephony.CallEndpointRoutingResult
import com.goreecloud.dialer.telephony.CallEndpointRuntimeSummary
import com.goreecloud.dialer.telephony.CallLifecycleState
import com.goreecloud.dialer.telephony.CallRuntimeSummary
import com.goreecloud.dialer.telephony.InCallAudioControlRuntime
import com.goreecloud.dialer.telephony.InCallEndpointRoutingRuntime
import com.goreecloud.dialer.telephony.InCallRuntimeSnapshot
import com.goreecloud.dialer.telephony.InCallRuntimeStore
import com.goreecloud.dialer.telephony.presentationLabel

@Composable
internal fun DevelopmentInCallPanel(snapshot: InCallRuntimeSnapshot) {
    var operationStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Live Telecom sessions", style = MaterialTheme.typography.titleMedium)
        Text(
            "Development control surface — no caller identity, endpoint device name, phone-account identity, or call content is projected.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))

        snapshot.calls.forEach { call ->
            DevelopmentCallControls(
                call = call,
                onResult = { operationStatus = it },
            )
            Spacer(Modifier.height(12.dp))
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
    val muteSupported = snapshot.calls.any {
        (it.state == CallLifecycleState.ACTIVE || it.state == CallLifecycleState.HOLDING) &&
            it.muteSupported
    }

    if (!muteSupported) {
        Text(
            "Android Telecom does not currently report mute support for the active call set.",
            style = MaterialTheme.typography.bodySmall,
        )
    } else {
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
                    CallEndpointRoutingResult.Submitted ->
                        "${endpoint.kind}: route request submitted"

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
    val actions = CallControlPresentationPolicy.actionsFor(
        state = call.state,
        holdCurrentlyAvailable = call.holdCurrentlyAvailable,
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Session ${call.sessionId}: ${call.state}")
        if (actions.isEmpty()) {
            Text(
                "No accepted control for this lifecycle state",
                style = MaterialTheme.typography.bodySmall,
            )
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
                                    CallControlResult.Submitted ->
                                        "${action.presentationLabel()}: request submitted"

                                    is CallControlResult.Rejected ->
                                        "${action.presentationLabel()}: rejected — ${result.reason}"

                                    is CallControlResult.Failed ->
                                        "${action.presentationLabel()}: failed — ${result.reason}"
                                },
                            )
                        },
                    ) {
                        Text(action.presentationLabel())
                    }
                }
            }
        }

        if (
            (call.state == CallLifecycleState.ACTIVE ||
                call.state == CallLifecycleState.HOLDING) &&
            !call.holdCurrentlyAvailable
        ) {
            Text(
                if (call.holdSupported) {
                    "Hold is supported but temporarily unavailable for this call."
                } else {
                    "Hold is not supported for this call."
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }

        DevelopmentPostDialControls(
            call = call,
            onResult = onResult,
        )

        DevelopmentConferenceControls(
            call = call,
            onResult = onResult,
        )

        if (
            call.state == CallLifecycleState.ACTIVE ||
            call.state == CallLifecycleState.HOLDING
        ) {
            Spacer(Modifier.height(12.dp))
            DevelopmentDtmfKeypad(
                call = call,
                onResult = onResult,
            )
        }
    }
}

private fun CallAudioControlResult.message(action: CallAudioControlAction): String {
    val label = if (action == CallAudioControlAction.Mute) "Mute" else "Unmute"
    return when (this) {
        CallAudioControlResult.Submitted -> "$label: request submitted"
        is CallAudioControlResult.Rejected -> "$label: rejected — $reason"
        is CallAudioControlResult.Failed -> "$label: failed — $reason"
    }
}
