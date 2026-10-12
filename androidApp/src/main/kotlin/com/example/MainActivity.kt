package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Process incoming wapi://login-callback deep link when launched from a closed state
        SupabaseConnectivityClient.handleIncomingDeepLink(intent)

        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Process incoming wapi://login-callback deep link when Wapi Rides is already running
        SupabaseConnectivityClient.handleIncomingDeepLink(intent)
    }
}
