package com.goreecloud.dialer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.goreecloud.dialer.telephony.AndroidDialRequestReader
import com.goreecloud.dialer.telephony.DialRequest
import com.goreecloud.dialer.ui.DialerApp

class MainActivity : ComponentActivity() {
    private var dialRequest by mutableStateOf<DialRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialRequest = AndroidDialRequestReader.read(intent)
        setContent { DialerApp(initialDialRequest = dialRequest) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dialRequest = AndroidDialRequestReader.read(intent)
    }
}
