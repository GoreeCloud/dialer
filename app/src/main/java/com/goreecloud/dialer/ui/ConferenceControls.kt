package com.goreecloud.dialer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goreecloud.dialer.telephony.CallRuntimeSummary
import com.goreecloud.dialer.telephony.ConferenceControlAction
import com.goreecloud.dialer.telephony.ConferenceControlResult
import com.goreecloud.dialer.telephony.InCallRuntimeStore

@Composable
internal fun DevelopmentConferenceControls(
    call: CallRuntimeSummary,
    onResult: (String) -> Unit,
) {
    val hasAction = call.conferenceableSessionIds.isNotEmpty() ||
        call.mergeConferenceAvailable ||
        call.swapConferenceAvailable ||
        (call.separateFromConferenceAvailable && call.parentSessionId != null)

    if (!hasAction && !call.manageConferenceSupported && call.childSessionIds.isEmpty()) return

    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Conference", style = MaterialTheme.typography.titleSmall)

        if (call.manageConferenceSupported) {
            Text(
                "Android Telecom reports conference management support for this session.",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (call.childSessionIds.isNotEmpty()) {
            Text(
                "Tracked conference children: ${call.childSessionIds.joinToString()}",
                style = MaterialTheme.typography.bodySmall,
            )
        }

        call.conferenceableSessionIds.forEach { otherSessionId ->
            Button(
                onClick = {
                    onResult(
                        InCallRuntimeStore.executeConference(
                            call.sessionId,
                            ConferenceControlAction.ConferenceWith(otherSessionId),
                        ).message("Conference with session $otherSessionId"),
                    )
                },
            ) {
                Text("Conference with session $otherSessionId")
            }
        }

        if (call.mergeConferenceAvailable) {
            Button(
                onClick = {
                    onResult(
                        InCallRuntimeStore.executeConference(
                            call.sessionId,
                            ConferenceControlAction.MergeConference,
                        ).message("Merge conference"),
                    )
                },
            ) {
                Text("Merge conference")
            }
        }

        if (call.swapConferenceAvailable) {
            Button(
                onClick = {
                    onResult(
                        InCallRuntimeStore.executeConference(
                            call.sessionId,
                            ConferenceControlAction.SwapConference,
                        ).message("Swap conference"),
                    )
                },
            ) {
                Text("Swap conference")
            }
        }

        if (call.separateFromConferenceAvailable && call.parentSessionId != null) {
            Button(
                onClick = {
                    onResult(
                        InCallRuntimeStore.executeConference(
                            call.sessionId,
                            ConferenceControlAction.SeparateFromConference,
                        ).message("Separate from conference"),
                    )
                },
            ) {
                Text("Separate from conference")
            }
        }
    }
}

private fun ConferenceControlResult.message(label: String): String = when (this) {
    ConferenceControlResult.Submitted -> "$label: request submitted"
    is ConferenceControlResult.Rejected -> "$label: rejected — $reason"
    is ConferenceControlResult.Failed -> "$label: failed — $reason"
}
