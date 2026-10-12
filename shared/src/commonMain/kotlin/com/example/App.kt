package com.example

import androidx.compose.runtime.Composable
import com.example.ui.navigation.WapiAppNavHost
import com.example.ui.theme.WapiRidesTheme

/** Root composable shared by the Android app and the web app. */
@Composable
fun App() {
    WapiRidesTheme {
        WapiAppNavHost()
    }
}
