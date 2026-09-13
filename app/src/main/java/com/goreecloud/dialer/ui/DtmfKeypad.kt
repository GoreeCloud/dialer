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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.goreecloud.dialer.telephony.CallControlAction
import com.goreecloud.dialer.telephony.CallControlResult
import com.goreecloud.dialer.telephony.CallRuntimeSummary
import com.goreecloud.dialer.telephony.InCallRuntimeStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DTMF_PULSE_MILLIS = 150L

/**
 * Development DTMF surface for an explicitly selected live call session.
 *
 * Each button press creates one short user-driven tone request. No number, IVR response, contact,
 * transcript, or assistant state can trigger this surface automatically. Non-throwing Android
 * calls are treated as submitted requests rather than proof that remote signaling succeeded.
 */
@Composable
internal fun DevelopmentDtmfKeypad(
    call: CallRuntimeSummary,
    onResult: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var busy by remember(call.sessionId) { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Keypad", style = MaterialTheme.typography.titleSmall)
        Text(
            "DTMF tones are requested only from an explicit key press.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(8.dp))

        listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
            listOf('*', '0', '#'),
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { digit ->
                    Button(
                        enabled = !busy,
                        onClick = {
                            if (busy) return@Button
                            busy = true
                            scope.launch {
                                var toneStartSubmitted = false
                                try {
                                    when (
                                        val startResult = InCallRuntimeStore.execute(
                                            call.sessionId,
                                            CallControlAction.StartDtmf(digit),
                                        )
                                    ) {
                                        CallControlResult.Submitted -> {
                                            toneStartSubmitted = true
                                            onResult("DTMF $digit: start request submitted")
                                            delay(DTMF_PULSE_MILLIS)
                                        }

                                        is CallControlResult.Rejected -> {
                                            onResult(
                                                "DTMF $digit: rejected — ${startResult.reason}",
                                            )
                                        }

                                        is CallControlResult.Failed -> {
                                            onResult(
                                                "DTMF $digit: failed — ${startResult.reason}",
                                            )
                                        }
                                    }
                                } finally {
                                    if (toneStartSubmitted) {
                                        when (
                                            val stopResult = InCallRuntimeStore.execute(
                                                call.sessionId,
                                                CallControlAction.StopDtmf,
                                            )
                                        ) {
                                            CallControlResult.Submitted ->
                                                onResult("DTMF $digit: stop request submitted")

                                            is CallControlResult.Rejected ->
                                                onResult(
                                                    "DTMF $digit: stop rejected — ${stopResult.reason}",
                                                )

                                            is CallControlResult.Failed ->
                                                onResult(
                                                    "DTMF $digit: stop failed — ${stopResult.reason}",
                                                )
                                        }
                                    }
                                    busy = false
                                }
                            }
                        },
                    ) {
                        Text(digit.toString())
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
