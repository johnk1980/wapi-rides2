package com.example.data

import com.example.SupabaseConnectivityClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Typed representation and constants layer for Wapi Rides notification types (Phase 4.7A).
 * Uses stable lowercase snake_case wire identifiers in PostgreSQL / JSON.
 */
enum class WapiNotificationType(val wireValue: String) {
    TRIP_REQUEST_RECEIVED("trip_request_received"),
    TRIP_REQUEST_ACCEPTED("trip_request_accepted"),
    TRIP_REQUEST_DECLINED("trip_request_declined"),
    TRIP_REQUEST_CANCELLED("trip_request_cancelled"),
    BOOKING_CANCELLED("booking_cancelled"),
    TRIP_STARTED("trip_started"),
    TRIP_COMPLETED("trip_completed"),
    RATING_AVAILABLE("rating_available"),
    UNKNOWN("unknown");

    companion object {
        const val WIRE_TRIP_REQUEST_RECEIVED = "trip_request_received"
        const val WIRE_TRIP_REQUEST_ACCEPTED = "trip_request_accepted"
        const val WIRE_TRIP_REQUEST_DECLINED = "trip_request_declined"
        const val WIRE_TRIP_REQUEST_CANCELLED = "trip_request_cancelled"
        const val WIRE_BOOKING_CANCELLED = "booking_cancelled"
        const val WIRE_TRIP_STARTED = "trip_started"
        const val WIRE_TRIP_COMPLETED = "trip_completed"
        const val WIRE_RATING_AVAILABLE = "rating_available"

        fun fromWireValue(raw: String?): WapiNotificationType {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.wireValue == normalized } ?: UNKNOWN
        }
    }
}

/**
 * Navigation-related metadata stored inside `public.notifications.data` (jsonb).
 * Contains only safe IDs (`trip_id`, `trip_request_id`, `booking_id`).
 * Never stores sensitive personal information (phone numbers, emails, or tokens).
 */
@Serializable
data class WapiNotificationData(
    @SerialName("trip_id")
    val tripId: String? = null,
    @SerialName("trip_request_id")
    val tripRequestId: String? = null,
    @SerialName("booking_id")
    val bookingId: String? = null
) {
    val cleanTripId: String?
        get() = tripId?.trim()?.takeIf { it.isNotEmpty() }

    val cleanTripRequestId: String?
        get() = tripRequestId?.trim()?.takeIf { it.isNotEmpty() }

    val cleanBookingId: String?
        get() = bookingId?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * Focused payload for marking one or all notifications as read (`is_read = true`).
 */
@Serializable
data class NotificationMarkReadPayload(
    @SerialName("is_read")
    val isRead: Boolean = true
)

/**
 * Represents a row retrieved from `public.notifications`.
 * Created exclusively by PostgreSQL triggers and security definer RPCs.
 */
@Serializable
data class WapiNotification(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("type")
    val type: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("body")
    val body: String = "",
    @SerialName("data")
    val data: WapiNotificationData? = null,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String = ""
) {
    val notificationType: WapiNotificationType
        get() = WapiNotificationType.fromWireValue(type)

    val tripId: String?
        get() = data?.cleanTripId

    val tripRequestId: String?
        get() = data?.cleanTripRequestId

    val bookingId: String?
        get() = data?.cleanBookingId

    val formattedTimestamp: String
        get() {
            val raw = createdAt.trim()
            if (raw.length >= 16) {
                return raw.substring(0, 10) + " · " + raw.substring(11, 16)
            }
            return if (raw.isNotEmpty()) raw.take(10) else "Just now"
        }
}

interface WapiNotificationRepository {
    suspend fun getMyNotifications(): Result<List<WapiNotification>>
    suspend fun getUnreadNotificationCount(): Result<Int>
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit>
    suspend fun markAllNotificationsAsRead(): Result<Unit>
}

class SupabaseWapiNotificationRepository : WapiNotificationRepository {

    override suspend fun getMyNotifications(): Result<List<WapiNotification>> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to view notifications.")

        val userId = currentUser.id.trim()
        if (userId.isEmpty()) {
            throw IllegalStateException("Please sign in to view notifications.")
        }

        val rows = client.from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<WapiNotification>()

        rows.sortedByDescending { it.createdAt }
    }

    override suspend fun getUnreadNotificationCount(): Result<Int> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: return@runCatching 0

        val userId = currentUser.id.trim()
        if (userId.isEmpty()) return@runCatching 0

        val unreadRows = client.from("notifications")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
            .decodeList<WapiNotification>()

        unreadRows.count { !it.isRead }
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = runCatching {
        val cleanId = notificationId.trim()
        if (cleanId.isEmpty()) return@runCatching

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to update notifications.")

        val userId = currentUser.id.trim()
        if (userId.isEmpty()) {
            throw IllegalStateException("Please sign in to update notifications.")
        }

        val payload = NotificationMarkReadPayload(isRead = true)
        client.from("notifications")
            .update(payload) {
                filter {
                    eq("id", cleanId)
                    eq("user_id", userId)
                }
            }
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to update notifications.")

        val userId = currentUser.id.trim()
        if (userId.isEmpty()) {
            throw IllegalStateException("Please sign in to update notifications.")
        }

        val payload = NotificationMarkReadPayload(isRead = true)
        client.from("notifications")
            .update(payload) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
    }
}
