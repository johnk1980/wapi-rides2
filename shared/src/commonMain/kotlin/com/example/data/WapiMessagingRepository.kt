package com.example.data

import com.example.SupabaseConnectivityClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Represents a row from `public.conversation_members`.
 */
@Serializable
data class WapiConversationMember(
    @SerialName("id")
    val id: String = "",
    @SerialName("conversation_id")
    val conversationId: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("role")
    val role: String? = null,
    @SerialName("joined_at")
    val joinedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    // Client-side hydrated profile summary from public.profiles
    val profileSummary: DriverSummary? = null
) {
    val resolvedProfileSummary: DriverSummary
        get() = profileSummary ?: DriverSummary(id = userId, fullName = "Wapi Member")
}

/**
 * Represents a row from `public.messages`.
 * Uses the verified live database column `message` as the primary text field
 * and `read_at` as the authoritative read timestamp.
 */
@Serializable
data class WapiMessage(
    @SerialName("id")
    val id: String = "",
    @SerialName("conversation_id")
    val conversationId: String = "",
    @SerialName("sender_id")
    val senderId: String = "",
    @SerialName("message")
    val message: String = "",
    @SerialName("content")
    val content: String = "",
    @SerialName("body")
    val body: String? = null,
    @SerialName("message_text")
    val messageText: String? = null,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("read_at")
    val readAt: String? = null,
    @SerialName("created_at")
    val createdAt: String = ""
) {
    val textContent: String
        get() = message.trim().ifEmpty {
            content.trim().ifEmpty {
                body?.trim()?.takeIf { it.isNotEmpty() }
                    ?: messageText?.trim().orEmpty()
            }
        }

    val isEffectivelyRead: Boolean
        get() = !readAt.isNullOrBlank() || isRead

    val formattedTimestamp: String
        get() {
            val raw = createdAt.trim()
            if (raw.length >= 16) {
                return raw.substring(0, 10) + " · " + raw.substring(11, 16)
            }
            return if (raw.isNotEmpty()) raw.take(10) else "Just now"
        }

    val formattedTimeShort: String
        get() {
            val raw = createdAt.trim()
            if (raw.length >= 16) {
                return raw.substring(11, 16)
            }
            return if (raw.isNotEmpty()) raw else "Just now"
        }
}

/**
 * Payload sent when inserting a new text message into `public.messages`.
 */
@Serializable
data class WapiMessageInsertPayload(
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("message")
    val message: String
)

/**
 * Focused payload for marking messages as read (`read_at`).
 */
@Serializable
data class WapiMessageMarkReadPayload(
    @SerialName("read_at")
    val readAt: String
)

/**
 * Represents a row from `public.conversations` hydrated with members, other participant,
 * latest message preview, trip summary (if linked), and unread count.
 */
@Serializable
data class WapiConversation(
    @SerialName("id")
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String? = null,
    @SerialName("booking_id")
    val bookingId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    // Client-side hydrated fields (ignored when absent from Postgrest JSON)
    val members: List<WapiConversationMember> = emptyList(),
    val otherParticipant: DriverSummary? = null,
    val latestMessage: WapiMessage? = null,
    val unreadCount: Int = 0,
    val tripSummary: Trip? = null
) {
    val resolvedOtherParticipant: DriverSummary
        get() = otherParticipant ?: DriverSummary(id = "", fullName = "Wapi Member")

    val latestMessagePreview: String
        get() = latestMessage?.textContent?.takeIf { it.isNotBlank() }
            ?: "No messages yet. Say hello to coordinate your trip."

    val latestMessageTimestamp: String
        get() = latestMessage?.formattedTimestamp
            ?: updatedAt?.trim()?.takeIf { it.length >= 16 }?.let {
                it.substring(0, 10) + " · " + it.substring(11, 16)
            }
            ?: createdAt?.trim()?.takeIf { it.length >= 16 }?.let {
                it.substring(0, 10) + " · " + it.substring(11, 16)
            }
            ?: ""

    val sortTimestamp: String
        get() = latestMessage?.createdAt?.takeIf { it.isNotBlank() }
            ?: updatedAt?.takeIf { it.isNotBlank() }
            ?: createdAt.orEmpty()

    val hasUnread: Boolean
        get() = unreadCount > 0

    val routeLabel: String?
        get() = tripSummary?.let { "${it.origin} → ${it.destination}" }
}

/**
 * Repository interface for Phase 4.7B Wapi Rides Messaging.
 *
 * Security guarantees:
 * - Uses the authenticated Supabase user ID (`client.auth.currentUserOrNull()`).
 * - Never exposes `service_role` credentials.
 * - Never assumes client-side conversation access; all queries go directly through
 *   Supabase PostgREST so PostgreSQL RLS policies determine access.
 */
interface WapiMessagingRepository {
    /**
     * A. Get the authenticated user's conversations.
     */
    suspend fun getMyConversations(): Result<List<WapiConversation>>

    /**
     * B. Get the members of a conversation.
     */
    suspend fun getConversationMembers(conversationId: String): Result<List<WapiConversationMember>>

    /**
     * C. Get messages for a conversation ordered chronologically (oldest to newest).
     */
    suspend fun getMessagesForConversation(conversationId: String): Result<List<WapiMessage>>

    /**
     * D. Send a text message in a conversation as the authenticated user.
     */
    suspend fun sendMessage(conversationId: String, content: String): Result<WapiMessage>

    /**
     * E. Mark unread messages in a conversation (sent by other members) as read.
     */
    suspend fun markConversationMessagesAsRead(conversationId: String): Result<Unit>

    /**
     * F. Get the authenticated user's total unread message count across their conversations.
     */
    suspend fun getUnreadMessageCount(): Result<Int>

    /**
     * Resolves an existing conversation for a booking/trip or provisions one via RPC if available.
     */
    suspend fun resolveConversationForBookingOrTrip(
        bookingId: String? = null,
        tripId: String? = null,
        otherUserId: String? = null
    ): Result<WapiConversation?>
}

class SupabaseWapiMessagingRepository : WapiMessagingRepository {

    override suspend fun getMyConversations(): Result<List<WapiConversation>> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to view your messages.")

        val currentUserId = currentUser.id.trim()
        if (currentUserId.isEmpty()) {
            throw IllegalStateException("Please sign in to view your messages.")
        }

        // Let RLS on `public.conversations` determine which conversations the user can SELECT
        val rawConversations = client.from("conversations")
            .select {
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<WapiConversation>()

        if (rawConversations.isEmpty()) {
            return@runCatching emptyList()
        }

        val conversationIds = rawConversations.map { it.id.trim() }.filter { it.isNotEmpty() }.distinct()

        // Fetch membership records via RLS on `public.conversation_members`
        val rawMembers = if (conversationIds.isNotEmpty()) {
            runCatching {
                client.from("conversation_members")
                    .select {
                        filter {
                            isIn("conversation_id", conversationIds)
                        }
                    }
                    .decodeList<WapiConversationMember>()
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

        // Hydrate member profiles from `public.profiles`
        val memberUserIds = rawMembers.map { it.userId.trim() }.filter { it.isNotEmpty() }.distinct()
        val profilesById: Map<String, DriverSummary> = if (memberUserIds.isNotEmpty()) {
            runCatching {
                client.from("profiles")
                    .select {
                        filter {
                            isIn("id", memberUserIds)
                        }
                    }
                    .decodeList<DriverSummary>()
                    .associateBy { it.id }
            }.getOrElse { emptyMap() }
        } else {
            emptyMap()
        }

        val hydratedMembersByConvId: Map<String, List<WapiConversationMember>> = rawMembers
            .map { member ->
                member.copy(profileSummary = profilesById[member.userId])
            }
            .groupBy { it.conversationId }

        // Fetch messages for these conversations via RLS on `public.messages`
        val rawMessages = if (conversationIds.isNotEmpty()) {
            runCatching {
                client.from("messages")
                    .select {
                        filter {
                            isIn("conversation_id", conversationIds)
                        }
                        order(column = "created_at", order = Order.DESCENDING)
                    }
                    .decodeList<WapiMessage>()
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

        val messagesByConvId: Map<String, List<WapiMessage>> = rawMessages.groupBy { it.conversationId }

        // Optionally hydrate associated trip summaries for route context
        val tripIds = rawConversations.mapNotNull { it.tripId?.trim()?.takeIf { id -> id.isNotEmpty() } }.distinct()
        val tripsById: Map<String, Trip> = if (tripIds.isNotEmpty()) {
            runCatching {
                client.from("trips")
                    .select {
                        filter {
                            isIn("id", tripIds)
                        }
                    }
                    .decodeList<Trip>()
                    .associateBy { it.id }
            }.getOrElse { emptyMap() }
        } else {
            emptyMap()
        }

        rawConversations.map { conv ->
            val members = hydratedMembersByConvId[conv.id].orEmpty()
            val otherMember = members.firstOrNull { it.userId != currentUserId }
                ?: members.firstOrNull()
            val convMessages = messagesByConvId[conv.id].orEmpty()
            val latestMsg = convMessages.maxByOrNull { it.createdAt }
            val unreadForUser = convMessages.count { msg ->
                msg.senderId != currentUserId && !msg.isEffectivelyRead
            }

            conv.copy(
                members = members,
                otherParticipant = otherMember?.resolvedProfileSummary,
                latestMessage = latestMsg,
                unreadCount = unreadForUser,
                tripSummary = conv.tripId?.let { tripsById[it] }
            )
        }.sortedByDescending { it.sortTimestamp }
    }

    override suspend fun getConversationMembers(conversationId: String): Result<List<WapiConversationMember>> = runCatching {
        val cleanConversationId = conversationId.trim()
        if (cleanConversationId.isEmpty()) {
            throw IllegalArgumentException("Conversation ID is required.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to view conversation members.")

        val rawMembers = client.from("conversation_members")
            .select {
                filter {
                    eq("conversation_id", cleanConversationId)
                }
            }
            .decodeList<WapiConversationMember>()

        if (rawMembers.isEmpty()) return@runCatching emptyList()

        val userIds = rawMembers.map { it.userId.trim() }.filter { it.isNotEmpty() }.distinct()
        val profilesById: Map<String, DriverSummary> = if (userIds.isNotEmpty()) {
            runCatching {
                client.from("profiles")
                    .select {
                        filter {
                            isIn("id", userIds)
                        }
                    }
                    .decodeList<DriverSummary>()
                    .associateBy { it.id }
            }.getOrElse { emptyMap() }
        } else {
            emptyMap()
        }

        rawMembers.map { member ->
            member.copy(profileSummary = profilesById[member.userId])
        }
    }

    override suspend fun getMessagesForConversation(conversationId: String): Result<List<WapiMessage>> = runCatching {
        val cleanConversationId = conversationId.trim()
        if (cleanConversationId.isEmpty()) {
            throw IllegalArgumentException("Conversation ID is required.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to view messages.")

        // Query `public.messages` directly; PostgreSQL RLS enforces membership access
        val rows = client.from("messages")
            .select {
                filter {
                    eq("conversation_id", cleanConversationId)
                }
                order(column = "created_at", order = Order.ASCENDING)
            }
            .decodeList<WapiMessage>()

        rows.sortedBy { it.createdAt }
    }

    override suspend fun sendMessage(conversationId: String, content: String): Result<WapiMessage> = runCatching {
        val cleanConversationId = conversationId.trim()
        if (cleanConversationId.isEmpty()) {
            throw IllegalArgumentException("Conversation could not be found.")
        }

        val cleanContent = content.trim()
        if (cleanContent.isEmpty()) {
            throw IllegalArgumentException("Please enter a message before sending.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to send a message.")

        val senderId = currentUser.id.trim()
        if (senderId.isEmpty()) {
            throw IllegalStateException("Please sign in to send a message.")
        }

        val payload = WapiMessageInsertPayload(
            conversationId = cleanConversationId,
            senderId = senderId,
            message = cleanContent
        )

        // Rely on PostgreSQL RLS to permit or reject the INSERT into `public.messages`
        val insertedRows = client.from("messages")
            .insert(payload) {
                select()
            }
            .decodeList<WapiMessage>()

        insertedRows.firstOrNull() ?: WapiMessage(
            id = "",
            conversationId = cleanConversationId,
            senderId = senderId,
            message = cleanContent,
            content = cleanContent,
            readAt = null,
            createdAt = ""
        )
    }

    override suspend fun markConversationMessagesAsRead(conversationId: String): Result<Unit> = runCatching {
        val cleanConversationId = conversationId.trim()
        if (cleanConversationId.isEmpty()) return@runCatching

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to update message status.")

        val currentUserId = currentUser.id.trim()
        if (currentUserId.isEmpty()) {
            throw IllegalStateException("Please sign in to update message status.")
        }

        val payload = WapiMessageMarkReadPayload(
            readAt = com.example.platform.nowIsoString()
        )
        client.from("messages")
            .update(payload) {
                filter {
                    eq("conversation_id", cleanConversationId)
                    neq("sender_id", currentUserId)
                    exact("read_at", null)
                }
            }
    }

    override suspend fun getUnreadMessageCount(): Result<Int> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: return@runCatching 0

        val currentUserId = currentUser.id.trim()
        if (currentUserId.isEmpty()) return@runCatching 0

        // RLS on `public.messages` restricts SELECT to conversations the user is a member of
        val unreadRows = client.from("messages")
            .select {
                filter {
                    neq("sender_id", currentUserId)
                    exact("read_at", null)
                }
            }
            .decodeList<WapiMessage>()

        unreadRows.count { it.senderId != currentUserId && !it.isEffectivelyRead }
    }

    override suspend fun resolveConversationForBookingOrTrip(
        bookingId: String?,
        tripId: String?,
        otherUserId: String?
    ): Result<WapiConversation?> = runCatching {
        val cleanBookingId = bookingId?.trim()?.takeIf { it.isNotEmpty() }
        val cleanTripId = tripId?.trim()?.takeIf { it.isNotEmpty() }
        val cleanOtherUserId = otherUserId?.trim()?.takeIf { it.isNotEmpty() }

        // 1. Check already-visible conversations via RLS
        val currentConversations = getMyConversations().getOrDefault(emptyList())

        if (cleanBookingId != null) {
            val byBooking = currentConversations.firstOrNull { it.bookingId == cleanBookingId }
            if (byBooking != null) return@runCatching byBooking
        }

        if (cleanTripId != null) {
            val byTripAndMember = currentConversations.firstOrNull { conv ->
                conv.tripId == cleanTripId &&
                    (cleanOtherUserId == null ||
                        conv.otherParticipant?.id == cleanOtherUserId ||
                        conv.members.any { it.userId == cleanOtherUserId })
            }
            if (byTripAndMember != null) return@runCatching byTripAndMember
        }

        if (cleanOtherUserId != null) {
            val byParticipant = currentConversations.firstOrNull { conv ->
                conv.otherParticipant?.id == cleanOtherUserId ||
                    conv.members.any { it.userId == cleanOtherUserId }
            }
            if (byParticipant != null && cleanTripId == null) return@runCatching byParticipant
        }

        // 2. If a bookingId is provided, call the security-definer RPC and capture its returned conversation UUID
        if (cleanBookingId != null) {
            SupabaseConnectivityClient.ensureConfigured()
            val client = SupabaseConnectivityClient.client
            val rpcConversationId: String? = runCatching {
                val params = buildJsonObject {
                    put("p_booking_id", JsonPrimitive(cleanBookingId))
                }
                val rpcResult = client.postgrest.rpc(
                    "get_or_create_booking_conversation",
                    params
                )
                runCatching { rpcResult.decodeAs<String>().trim().removeSurrounding("\"") }
                    .getOrNull()
                    ?.takeIf { it.isNotEmpty() }
            }.getOrNull()

            val refreshed = getMyConversations().getOrDefault(emptyList())
            val foundAfterRpc = refreshed.firstOrNull { conv ->
                (!rpcConversationId.isNullOrBlank() && conv.id == rpcConversationId) ||
                    conv.bookingId == cleanBookingId
            } ?: refreshed.firstOrNull { conv ->
                cleanTripId != null && conv.tripId == cleanTripId &&
                    (cleanOtherUserId == null ||
                        conv.otherParticipant?.id == cleanOtherUserId ||
                        conv.members.any { it.userId == cleanOtherUserId })
            }
            if (foundAfterRpc != null) return@runCatching foundAfterRpc
        }

        null
    }
}
