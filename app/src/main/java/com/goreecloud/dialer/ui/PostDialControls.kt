package com.goreecloud.dialer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goreecloud.dialer.telephony.CallRuntimeSummary
import com.goreecloud.dialer.telephony.InCallRuntimeStore
import com.goreecloud.dialer.telephony.PostDialControlAction
import com.goreecloud.dialer.telephony.PostDialControlResult

@Composable
internal fun DevelopmentPostDialControls(
    call: CallRuntimeSummary,
    onResult: (String) -> Unit,
) {
    if (!call.postDialWaitPending) return

    Spacer(Modifier.height(12.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Post-dial confirmation", style = MaterialTheme.typography.titleSmall)
        Text(
            "Android paused the outgoing post-dial sequence. ${call.postDialRemainingCharacterCount} character(s) remain; the sequence itself is not projected into this Development state.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = {
                    onResult(
                        InCallRuntimeStore.executePostDial(
                            call.sessionId,
                            PostDialControlAction.Continue,
                        ).message("Continue post-dial"),
                    )
                },
            ) {
                Text("Continue")
            }
            Button(
                onClick = {
                    onResult(
                        InCallRuntimeStore.executePostDial(
                            call.sessionId,
                            PostDialControlAction.Cancel,
                        ).message("Cancel post-dial"),
                    )
                },
            ) {
                Text("Cancel")
            }
        }
    }
}

private fun PostDialControlResult.message(label: String): String = when (this) {
    PostDialControlResult.Submitted -> "$label: request submitted"
    is PostDialControlResult.Rejected -> "$label: rejected — $reason"
    is PostDialControlResult.Failed -> "$label: failed — $reason"
}
