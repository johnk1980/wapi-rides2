package com.example

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@JsFun("() => window.location.hash || ''")
private external fun jsHash(): String

@JsFun("() => window.location.search || ''")
private external fun jsSearch(): String

@JsFun("() => { try { history.replaceState(null, '', window.location.pathname); } catch (e) {} }")
private external fun jsClearUrl()

@JsFun("() => { const el = document.getElementById('loading'); if (el && el.parentNode) { el.parentNode.removeChild(el); } }")
private external fun jsHideLoading()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Email-verification links come back to this page with the session in the URL.
    val params = SupabaseConnectivityClient.parseParams(
        jsHash().removePrefix("#") + "&" + jsSearch().removePrefix("?")
    )
    val hasAuthCallback = params.containsKey("access_token") ||
        params.containsKey("error") ||
        params.containsKey("error_description")
    if (hasAuthCallback) {
        jsClearUrl()
        MainScope().launch {
            SupabaseConnectivityClient.handleCallbackParams(params)
        }
    }

    ComposeViewport(document.body!!) {
        App()
    }
    jsHideLoading()
}
