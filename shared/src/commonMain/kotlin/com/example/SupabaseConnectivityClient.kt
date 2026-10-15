package com.example

import com.example.platform.authRedirectUrl
import com.example.platform.configureAuthPlatform
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.Storage
import io.ktor.http.decodeURLQueryComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionTestRow(
    @SerialName("message")
    val message: String
)

object SupabaseConnectivityClient {

    const val AUTH_DEEP_LINK_SCHEME = "wapi"
    const val AUTH_DEEP_LINK_HOST = "login-callback"

    /** Android: wapi://login-callback. Web: the page the app is served from. */
    val AUTH_REDIRECT_URL: String
        get() = authRedirectUrl()

    // Tolerate the two settings being entered the wrong way round (URL in the key slot and vice versa).
    private val configUrl: String
        get() {
            val url = AppConfig.SUPABASE_URL.trim()
            val key = AppConfig.SUPABASE_PUBLISHABLE_KEY.trim()
            return if (!url.startsWith("http") && key.startsWith("http")) key else url
        }

    private val configKey: String
        get() {
            val url = AppConfig.SUPABASE_URL.trim()
            val key = AppConfig.SUPABASE_PUBLISHABLE_KEY.trim()
            return if (!url.startsWith("http") && key.startsWith("http")) url else key
        }

    private val _deepLinkError = MutableStateFlow<String?>(null)
    val deepLinkError: StateFlow<String?> = _deepLinkError.asStateFlow()

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = configUrl,
            supabaseKey = configKey
        ) {
            install(Postgrest)
            install(Auth) {
                configureAuthPlatform()
            }
            install(Storage)
        }
    }

    fun reportDeepLinkError(message: String?) {
        _deepLinkError.value = message
    }

    fun clearDeepLinkError() {
        _deepLinkError.value = null
    }

    /**
     * Handles the parameters of an email-verification / auth callback URL (query + fragment).
     * Used by the web build; the Android build uses its own deep link handling.
     * Does nothing when the parameters contain no auth data.
     */
    suspend fun handleCallbackParams(params: Map<String, String>) {
        val errorParam = params["error_description"]?.takeIf { it.isNotBlank() }
            ?: params["error"]?.takeIf { it.isNotBlank() }
        val accessToken = params["access_token"]?.takeIf { it.isNotBlank() }
        val refreshToken = params["refresh_token"].orEmpty()

        if (errorParam == null && accessToken == null) return

        if (errorParam != null) {
            _deepLinkError.value = toUserFriendlyCallbackError(errorParam)
            return
        }
        if (!isConfigured()) {
            _deepLinkError.value = "Unable to verify email link because Wapi servers are not configured."
            return
        }
        runCatching {
            client.auth.importAuthToken(
                accessToken = accessToken.orEmpty(),
                refreshToken = refreshToken,
                retrieveUser = true,
                autoRefresh = true
            )
            _deepLinkError.value = null
        }.onFailure { throwable ->
            _deepLinkError.value = toUserFriendlyMessage(
                throwable,
                "We couldn't complete email verification from this link. Please sign in or request a new link."
            )
        }
    }

    /** Parses "a=b&c=d" into a map, URL-decoding values. */
    fun parseParams(raw: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        raw.trimStart('#', '?').split("&").forEach { pair ->
            if (pair.isBlank()) return@forEach
            val key = pair.substringBefore("=").decodeURLQueryComponent(plusIsSpace = true)
            val value = pair.substringAfter("=", "").decodeURLQueryComponent(plusIsSpace = true)
            result[key] = value
        }
        return result
    }

    fun toUserFriendlyCallbackError(rawError: String): String {
        val lower = rawError.lowercase()
        return when {
            lower.contains("expired") || lower.contains("otp_expired") ->
                "This email verification link has expired. Please sign in or request a new link."
            lower.contains("invalid") || lower.contains("access_denied") ->
                "This verification link is invalid or has already been used. Please sign in to continue."
            else ->
                "We couldn't verify this email link. Please sign in with your email and password."
        }
    }

    fun isConfigured(): Boolean {
        val url = configUrl
        val key = configKey
        return url.isNotBlank() &&
            url != "YOUR_SUPABASE_URL" &&
            key.isNotBlank() &&
            key != "YOUR_SUPABASE_PUBLISHABLE_KEY"
    }

    fun ensureConfigured() {
        val url = configUrl
        val key = configKey

        if (url.isBlank() || url == "YOUR_SUPABASE_URL") {
            throw IllegalStateException("SUPABASE_URL secret is not configured in AppConfig.")
        }
        if (key.isBlank() || key == "YOUR_SUPABASE_PUBLISHABLE_KEY") {
            throw IllegalStateException("SUPABASE_PUBLISHABLE_KEY secret is not configured in AppConfig.")
        }
    }

    suspend fun fetchSingleConnectionMessage(): String {
        ensureConfigured()

        val rows = client
            .from("connection_test")
            .select(columns = Columns.list("message")) {
                limit(count = 1)
            }
            .decodeList<ConnectionTestRow>()

        val firstRow = rows.firstOrNull()
            ?: throw NoSuchElementException("Query succeeded, but public.connection_test returned 0 rows.")

        return firstRow.message
    }

    fun sanitizeErrorMessage(throwable: Throwable): String {
        val raw = throwable.message?.takeIf { it.isNotBlank() }
            ?: throwable::class.simpleName
            ?: "Unknown error"

        var sanitized = raw
        val url = configUrl
        val key = configKey

        if (url.isNotEmpty() && url != "YOUR_SUPABASE_URL") {
            sanitized = sanitized.replace(url, "[REDACTED_SUPABASE_URL]", ignoreCase = true)
            val host = url.removePrefix("https://").removePrefix("http://").substringBefore("/")
            if (host.isNotEmpty()) {
                sanitized = sanitized.replace(host, "[REDACTED_SUPABASE_HOST]", ignoreCase = true)
            }
        }
        if (key.isNotEmpty() && key != "YOUR_SUPABASE_PUBLISHABLE_KEY") {
            sanitized = sanitized.replace(key, "[REDACTED_KEY]", ignoreCase = true)
        }

        return "${throwable::class.simpleName ?: "Exception"}: $sanitized"
    }

    fun toUserFriendlyMessage(throwable: Throwable, fallback: String): String {
        val sanitized = sanitizeErrorMessage(throwable).lowercase()
        return when {
            sanitized.contains("already registered") ||
                sanitized.contains("user_already_exists") ||
                sanitized.contains("already exists") ->
                "An account with this email address is already registered. Please sign in instead."

            sanitized.contains("invalid login credentials") ||
                sanitized.contains("invalid_credentials") ||
                sanitized.contains("invalid grant") ->
                "Incorrect email or password. Please verify your details and try again."

            sanitized.contains("email not confirmed") ||
                sanitized.contains("email_not_confirmed") ->
                "Please confirm your email address using the verification link sent to your inbox before signing in."

            sanitized.contains("weak_password") ||
                sanitized.contains("password should be at least") ->
                "Please choose a stronger password (at least 6 characters)."

            sanitized.contains("invalid email") ||
                sanitized.contains("unable to validate email") ->
                "Please enter a valid email address."

            sanitized.contains("rate limit") ||
                sanitized.contains("over_email_send_rate_limit") ||
                sanitized.contains("too many requests") ->
                "Too many attempts right now. Please wait a moment and try again."

            sanitized.contains("not enough seats available") ||
                sanitized.contains("no longer available") ||
                sanitized.contains("insufficient seats") ->
                "Those seats are no longer available."

            sanitized.contains("own trip") ||
                sanitized.contains("own ride") ||
                sanitized.contains("self-booking") ||
                sanitized.contains("cannot book your own") ||
                sanitized.contains("cannot request your own") ->
                "You cannot request a seat on your own ride."

            sanitized.contains("already submitted") ||
                sanitized.contains("duplicate") ->
                "Request already submitted."

            sanitized.contains("already rated") ||
                sanitized.contains("unique_reviewer_booking") ->
                "You have already rated this trip."

            sanitized.contains("booking is not completed") ||
                sanitized.contains("not completed") ||
                sanitized.contains("only completed bookings") ->
                "Ratings can only be submitted after the trip is completed."

            sanitized.contains("rating must be between 1 and 5") ||
                sanitized.contains("between 1 and 5") ->
                "Please select a rating between 1 and 5 stars."

            sanitized.contains("not a participant") ||
                sanitized.contains("reviewer_cannot_equal_reviewee") ||
                sanitized.contains("cannot rate yourself") ||
                sanitized.contains("self-rating") ->
                "Only confirmed trip participants can rate this journey."

            sanitized.contains("authentication required") ||
                sanitized.contains("no authenticated user") ->
                "Please sign in to continue."

            sanitized.contains("not allowed") ||
                sanitized.contains("unauthorized") ||
                sanitized.contains("only the trip driver") ||
                sanitized.contains("only the driver") ||
                sanitized.contains("permission denied") ||
                sanitized.contains("not authorized") ->
                "You are not allowed to perform this action."

            sanitized.contains("already been completed") ||
                sanitized.contains("already completed") ||
                sanitized.contains("trip is completed") ->
                "This trip has already been completed."

            sanitized.contains("already in progress") ||
                sanitized.contains("already started") ->
                "This trip is already in progress."

            sanitized.contains("can no longer be cancelled") ||
                sanitized.contains("cannot cancel") ||
                sanitized.contains("cannot be cancelled") ||
                sanitized.contains("cannot be completed") ||
                sanitized.contains("cannot complete") ||
                sanitized.contains("invalid status transition") ||
                sanitized.contains("invalid state transition") ->
                "This booking can no longer be cancelled."

            sanitized.contains("booking not found") ||
                sanitized.contains("booking could not be found") ||
                sanitized.contains("booking does not exist") ->
                "Booking could not be found."

            sanitized.contains("unable to resolve host") ||
                sanitized.contains("timeout") ||
                sanitized.contains("network") ||
                sanitized.contains("connect") ->
                "Unable to reach Wapi servers. Please check your internet connection and try again."

            else -> fallback
        }
    }
}
