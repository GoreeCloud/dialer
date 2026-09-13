package com.goreecloud.dialer.telephony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Minimal incoming-call surface used to validate the Telecom presentation boundary.
 * It intentionally exposes no caller identity or call content.
 */
class IncomingCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val sessionId = intent.getLongExtra(AndroidIncomingCallPresenter.EXTRA_SESSION_ID, -1L)
        setContent {
            IncomingCallScreen(
                sessionId = sessionId,
                onFinished = ::finish,
            )
        }
    }
}

@Composable
private fun IncomingCallScreen(
    sessionId: Long,
    onFinished: () -> Unit,
) {
    val snapshot by InCallRuntimeStore.snapshots.collectAsState()
    val call = snapshot.calls.firstOrNull { it.sessionId == sessionId }
    var operationStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(call?.state) {
        if (call == null || call.state != CallLifecycleState.RINGING) {
            onFinished()
        }
    }

    MaterialTheme {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Incoming call", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Caller identity is intentionally hidden in this Development presentation boundary.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Session $sessionId",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        enabled = call?.state == CallLifecycleState.RINGING,
                        onClick = {
                            operationStatus = InCallRuntimeStore.execute(
                                sessionId,
                                CallControlAction.AnswerAudio,
                            ).message("Answer")
                        },
                    ) {
                        Text("Answer")
                    }
                    Button(
                        enabled = call?.state == CallLifecycleState.RINGING,
                        onClick = {
                            operationStatus = InCallRuntimeStore.execute(
                                sessionId,
                                CallControlAction.Decline,
                            ).message("Decline")
                        },
                    ) {
                        Text("Decline")
                    }
                }

                operationStatus?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun CallControlResult.message(action: String): String = when (this) {
    CallControlResult.Succeeded -> "$action: succeeded"
    is CallControlResult.Rejected -> "$action: rejected — $reason"
    is CallControlResult.Failed -> "$action: failed — $reason"
}
