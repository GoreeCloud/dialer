package com.goreecloud.dialer

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.goreecloud.dialer.telephony.AndroidDialRequestReader
import com.goreecloud.dialer.telephony.DialRequest
import com.goreecloud.dialer.telephony.PreCallRouteReadinessCoordinator
import com.goreecloud.dialer.telephony.SubscriptionInventoryGateway
import com.goreecloud.dialer.telephony.SubscriptionInventoryResult
import com.goreecloud.dialer.ui.DialerApp
import com.goreecloud.dialer.ui.GlazeDialerTheme

class MainActivity : ComponentActivity() {
    private var dialRequest by mutableStateOf<DialRequest?>(null)
    private var subscriptionInventory by mutableStateOf<SubscriptionInventoryResult?>(null)

    private val readPhoneStatePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshSubscriptionInventory()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialRequest = AndroidDialRequestReader.read(intent)
        refreshSubscriptionInventory()
        setContent {
            GlazeDialerTheme {
                DialerApp(
                    initialDialRequest = dialRequest,
                    subscriptionInventory = subscriptionInventory,
                    onRequestSubscriptionPermission = {
                        readPhoneStatePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                    },
                    onRefreshSubscriptionInventory = ::refreshSubscriptionInventory,
                    onEvaluatePreCallRoute = { selectedSubscriptionId ->
                        PreCallRouteReadinessCoordinator(applicationContext).evaluate(
                            explicitlySelectedSubscriptionId = selectedSubscriptionId,
                            isEmergencyCall = false,
                        )
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSubscriptionInventory()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dialRequest = AndroidDialRequestReader.read(intent)
    }

    private fun refreshSubscriptionInventory() {
        subscriptionInventory = SubscriptionInventoryGateway(applicationContext).read()
    }
}
