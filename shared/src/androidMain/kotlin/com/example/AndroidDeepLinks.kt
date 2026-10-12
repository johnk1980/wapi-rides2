package com.example

import android.content.Intent
import android.net.Uri
import io.github.jan.supabase.auth.handleDeeplinks

/**
 * Processes an incoming wapi://login-callback intent (cold start or onNewIntent).
 * Safely captures malformed callback parameters or Supabase errors without crashing.
 */
fun SupabaseConnectivityClient.handleIncomingDeepLink(intent: Intent?) {
    val data: Uri = intent?.data ?: return
    val isWapiAuthCallback =
        data.scheme.equals(SupabaseConnectivityClient.AUTH_DEEP_LINK_SCHEME, ignoreCase = true) &&
            data.host.equals(SupabaseConnectivityClient.AUTH_DEEP_LINK_HOST, ignoreCase = true)
    if (!isWapiAuthCallback) return

    val errorParam = extractCallbackParam(data, "error_description")
        ?: extractCallbackParam(data, "error")
    if (!errorParam.isNullOrBlank()) {
        val decodedError = Uri.decode(errorParam).replace('+', ' ')
        reportDeepLinkError(toUserFriendlyCallbackError(decodedError))
        return
    }

    if (!isConfigured()) {
        reportDeepLinkError("Unable to verify email link because Wapi servers are not configured.")
        return
    }

    val rawQueryAndFragment = "${data.encodedQuery.orEmpty()}&${data.encodedFragment.orEmpty()}"
    val hasAuthTokensOrCode = rawQueryAndFragment.contains("access_token=") ||
        rawQueryAndFragment.contains("refresh_token=") ||
        rawQueryAndFragment.contains("code=") ||
        rawQueryAndFragment.contains("token_hash=")
    if (!hasAuthTokensOrCode) {
        reportDeepLinkError(
            "This verification link is incomplete or has already been used. Please sign in with your email and password."
        )
        return
    }

    runCatching {
        client.handleDeeplinks(intent) {
            clearDeepLinkError()
        }
    }.onFailure { throwable ->
        reportDeepLinkError(
            toUserFriendlyMessage(
                throwable,
                "We couldn't complete email verification from this link. Please sign in or request a new link."
            )
        )
    }
}

private fun extractCallbackParam(uri: Uri, key: String): String? {
    runCatching {
        uri.getQueryParameter(key)?.takeIf { it.isNotBlank() }?.let { return it }
    }
    val fragment = uri.encodedFragment ?: return null
    val prefix = "$key="
    val match = fragment.split("&").firstOrNull { it.startsWith(prefix, ignoreCase = true) }
    return match?.substringAfter("=")?.takeIf { it.isNotBlank() }
}
