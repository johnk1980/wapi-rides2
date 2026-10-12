package com.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.data.AvatarImageProcessor
import com.example.data.Booking
import com.example.data.DriverSummary
import com.example.data.Rating
import com.example.data.SessionState
import com.example.data.SignUpOutcome
import com.example.data.SupabaseWapiTripRepository
import com.example.data.Trip
import com.example.data.TripRequest
import com.example.data.TripStop
import com.example.data.UserProfile
import com.example.data.WapiAuthRepository
import com.example.data.WapiConversation
import com.example.data.WapiConversationMember
import com.example.data.WapiEmergencyContact
import com.example.data.WapiMessage
import com.example.data.WapiMessageInsertPayload
import com.example.data.WapiMessageMarkReadPayload
import com.example.data.WapiMessagingRepository
import com.example.data.WapiNotification
import com.example.data.WapiNotificationData
import com.example.data.WapiNotificationRepository
import com.example.data.WapiNotificationType
import com.example.data.WapiSafetyTrustRepository
import com.example.data.WapiTripRepository
import com.example.data.WapiTripShare
import com.example.data.WapiVehicle
import com.example.data.WapiVerificationRecord
import com.example.ui.navigation.WapiAppNavHost
import com.example.ui.theme.WapiRidesTheme
import com.example.ui.viewmodel.WapiAppViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class FakeWapiAuthRepository(
    initialSession: SessionState = SessionState.Unauthenticated,
    var profile: UserProfile = UserProfile(
        id = "user-123",
        fullName = "James Mwangi",
        phone = "+254712345678",
        email = "james@wapirides.co.ke"
    )
) : WapiAuthRepository {
    val sessionFlow = MutableStateFlow(initialSession)
    val deepLinkErrorFlow = MutableStateFlow<String?>(null)

    override val sessionState: Flow<SessionState> = sessionFlow
    override val deepLinkError: Flow<String?> = deepLinkErrorFlow

    override fun clearDeepLinkError() {
        deepLinkErrorFlow.value = null
    }

    override suspend fun signUp(
        fullName: String,
        phone: String,
        email: String,
        password: String
    ): Result<SignUpOutcome> {
        val created = UserProfile(
            id = "user-new",
            fullName = fullName,
            phone = phone,
            email = email
        )
        profile = created
        sessionFlow.value = SessionState.Authenticated(created.id, created.email)
        return Result.success(SignUpOutcome.Authenticated(created))
    }

    override suspend fun signIn(email: String, password: String): Result<UserProfile> {
        sessionFlow.value = SessionState.Authenticated(profile.id, email)
        return Result.success(profile)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun signOut(): Result<Unit> {
        sessionFlow.value = SessionState.Unauthenticated
        return Result.success(Unit)
    }

    override suspend fun fetchCurrentUserProfile(): Result<UserProfile> {
        return Result.success(profile)
    }

    var shouldFailAvatarUpload: Boolean = false
    var lastUploadedAvatarStoragePath: String? = null
    var lastUploadedAvatarBytes: ByteArray? = null
    var lastUploadedAvatarContentType: String? = null
    var lastUploadedAvatarUpsert: Boolean = false

    override suspend fun updateCurrentUserProfile(
        fullName: String,
        phone: String,
        bio: String?
    ): Result<UserProfile> {
        val session = sessionFlow.value
        val uid = (session as? SessionState.Authenticated)?.userId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Please sign in to update your profile."))
        if (profile.id != uid) {
            return Result.failure(IllegalStateException("RLS violation: Can only update own profile."))
        }
        profile = profile.copy(
            fullName = fullName.trim(),
            phone = phone.trim(),
            bio = bio?.trim()?.takeIf { it.isNotEmpty() }
        )
        return Result.success(profile)
    }

    override suspend fun uploadAndSaveCurrentUserAvatar(
        imageBytes: ByteArray
    ): Result<UserProfile> {
        val session = sessionFlow.value
        val uid = (session as? SessionState.Authenticated)?.userId?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Authentication required. Please sign in to update your profile photo."))
        if (profile.id != uid) {
            return Result.failure(IllegalStateException("RLS violation: Can only update own profile."))
        }
        if (imageBytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Processed avatar image cannot be empty."))
        }
        if (imageBytes.size > AvatarImageProcessor.MAX_OUTPUT_SIZE_BYTES) {
            return Result.failure(IllegalArgumentException("Processed avatar image exceeds maximum allowed size."))
        }
        if (shouldFailAvatarUpload) {
            return Result.failure(IllegalStateException("Network error while uploading avatar to storage."))
        }

        val storagePath = "$uid/avatar.jpg"
        lastUploadedAvatarStoragePath = storagePath
        lastUploadedAvatarBytes = imageBytes.copyOf()
        lastUploadedAvatarContentType = "image/jpeg"
        lastUploadedAvatarUpsert = true

        val publicUrl = "https://example.supabase.co/storage/v1/object/public/avatars/$storagePath?t=${System.currentTimeMillis()}"
        profile = profile.copy(avatarUrl = publicUrl)
        return Result.success(profile)
    }
}

private class FakeWapiSafetyTrustRepository(
    private val authRepository: FakeWapiAuthRepository
) : WapiSafetyTrustRepository {
    val savedVehicles = mutableListOf<WapiVehicle>()
    val savedVerificationRecords = mutableListOf<WapiVerificationRecord>()
    val savedEmergencyContacts = mutableListOf<WapiEmergencyContact>()
    val savedTripShares = mutableListOf<WapiTripShare>()

    private fun currentUserIdOrThrow(): String {
        val session = authRepository.sessionFlow.value
        return (session as? SessionState.Authenticated)?.userId?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Please sign in to manage your safety and trust settings.")
    }

    override suspend fun getMyVehicles(): Result<List<WapiVehicle>> = runCatching {
        val uid = currentUserIdOrThrow()
        savedVehicles.filter { it.ownerId == uid }
    }

    override suspend fun getVehicleById(vehicleId: String): Result<WapiVehicle?> = runCatching {
        val uid = currentUserIdOrThrow()
        savedVehicles.firstOrNull { it.id == vehicleId && it.ownerId == uid }
    }

    override suspend fun addVehicle(
        make: String,
        model: String,
        year: Int?,
        colour: String,
        registration: String,
        seatCapacity: Int,
        photo: String?,
        isActive: Boolean
    ): Result<WapiVehicle> = runCatching {
        val uid = currentUserIdOrThrow()
        val vehicle = WapiVehicle(
            id = "veh-${savedVehicles.size + 1}",
            ownerId = uid,
            make = make.trim(),
            model = model.trim(),
            year = year,
            colour = colour.trim(),
            colorAlias = colour.trim(),
            registration = registration.trim().uppercase(),
            registrationNumberAlias = registration.trim().uppercase(),
            plateNumberAlias = registration.trim().uppercase(),
            seatCapacity = seatCapacity,
            photo = photo?.trim()?.takeIf { it.isNotEmpty() },
            photoUrlAlias = photo?.trim()?.takeIf { it.isNotEmpty() },
            isActive = isActive,
            createdAt = "2026-11-01T10:00:00Z"
        )
        savedVehicles.add(0, vehicle)
        vehicle
    }

    override suspend fun updateVehicle(
        vehicleId: String,
        make: String,
        model: String,
        year: Int?,
        colour: String,
        registration: String,
        seatCapacity: Int,
        photo: String?,
        isActive: Boolean
    ): Result<WapiVehicle> = runCatching {
        val uid = currentUserIdOrThrow()
        val idx = savedVehicles.indexOfFirst { it.id == vehicleId && it.ownerId == uid }
        if (idx < 0) throw IllegalStateException("Vehicle not found or unauthorized.")
        val updated = savedVehicles[idx].copy(
            make = make.trim(),
            model = model.trim(),
            year = year,
            colour = colour.trim(),
            colorAlias = colour.trim(),
            registration = registration.trim().uppercase(),
            registrationNumberAlias = registration.trim().uppercase(),
            plateNumberAlias = registration.trim().uppercase(),
            seatCapacity = seatCapacity,
            photo = photo?.trim()?.takeIf { it.isNotEmpty() },
            photoUrlAlias = photo?.trim()?.takeIf { it.isNotEmpty() },
            isActive = isActive
        )
        savedVehicles[idx] = updated
        updated
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> = runCatching {
        val uid = currentUserIdOrThrow()
        val removed = savedVehicles.removeAll { it.id == vehicleId && it.ownerId == uid }
        if (!removed) throw IllegalStateException("Vehicle not found or unauthorized.")
    }

    override suspend fun getMyVerificationRecords(): Result<List<WapiVerificationRecord>> = runCatching {
        val uid = currentUserIdOrThrow()
        savedVerificationRecords.filter { it.userId == uid }
    }

    override suspend fun getMyEmergencyContacts(): Result<List<WapiEmergencyContact>> = runCatching {
        val uid = currentUserIdOrThrow()
        savedEmergencyContacts
            .filter { it.userId == uid }
            .sortedWith(compareByDescending<WapiEmergencyContact> { it.isPrimary }.thenBy { it.effectiveName })
    }

    override suspend fun addEmergencyContact(
        name: String,
        phone: String,
        relationship: String?,
        isPrimary: Boolean
    ): Result<WapiEmergencyContact> = runCatching {
        val uid = currentUserIdOrThrow()
        if (isPrimary) {
            for (i in savedEmergencyContacts.indices) {
                if (savedEmergencyContacts[i].userId == uid && savedEmergencyContacts[i].isPrimary) {
                    savedEmergencyContacts[i] = savedEmergencyContacts[i].copy(isPrimary = false)
                }
            }
        }
        val contact = WapiEmergencyContact(
            id = "ec-${savedEmergencyContacts.size + 1}",
            userId = uid,
            name = name.trim(),
            contactNameAlias = name.trim(),
            phone = phone.trim(),
            phoneNumberAlias = phone.trim(),
            relationship = relationship?.trim()?.takeIf { it.isNotEmpty() },
            isPrimary = isPrimary,
            createdAt = "2026-11-01T10:05:00Z"
        )
        savedEmergencyContacts.add(0, contact)
        contact
    }

    override suspend fun updateEmergencyContact(
        contactId: String,
        name: String,
        phone: String,
        relationship: String?,
        isPrimary: Boolean
    ): Result<WapiEmergencyContact> = runCatching {
        val uid = currentUserIdOrThrow()
        val idx = savedEmergencyContacts.indexOfFirst { it.id == contactId && it.userId == uid }
        if (idx < 0) throw IllegalStateException("Emergency contact not found or unauthorized.")
        if (isPrimary) {
            for (i in savedEmergencyContacts.indices) {
                if (savedEmergencyContacts[i].userId == uid && savedEmergencyContacts[i].id != contactId && savedEmergencyContacts[i].isPrimary) {
                    savedEmergencyContacts[i] = savedEmergencyContacts[i].copy(isPrimary = false)
                }
            }
        }
        val updated = savedEmergencyContacts[idx].copy(
            name = name.trim(),
            contactNameAlias = name.trim(),
            phone = phone.trim(),
            phoneNumberAlias = phone.trim(),
            relationship = relationship?.trim()?.takeIf { it.isNotEmpty() },
            isPrimary = isPrimary
        )
        savedEmergencyContacts[idx] = updated
        updated
    }

    override suspend fun deleteEmergencyContact(contactId: String): Result<Unit> = runCatching {
        val uid = currentUserIdOrThrow()
        val removed = savedEmergencyContacts.removeAll { it.id == contactId && it.userId == uid }
        if (!removed) throw IllegalStateException("Emergency contact not found or unauthorized.")
    }

    override suspend fun getMyTripSharesForTrip(tripId: String): Result<List<WapiTripShare>> = runCatching {
        val uid = currentUserIdOrThrow()
        savedTripShares.filter { it.tripId == tripId && it.effectiveOwnerUserId == uid && it.isCurrentlyActive }
    }

    override suspend fun createTripShare(
        tripId: String,
        bookingId: String?,
        emergencyContact: WapiEmergencyContact?,
        recipientName: String?,
        recipientPhone: String?
    ): Result<WapiTripShare> = runCatching {
        val uid = currentUserIdOrThrow()
        val resolvedName = emergencyContact?.effectiveName ?: recipientName?.trim()
        val resolvedPhone = emergencyContact?.effectivePhone ?: recipientPhone?.trim()
        val share = WapiTripShare(
            id = "share-${savedTripShares.size + 1}",
            tripId = tripId,
            bookingId = bookingId,
            userId = uid,
            sharedByUserIdAlias = uid,
            emergencyContactId = emergencyContact?.id,
            recipientName = resolvedName,
            recipientPhone = resolvedPhone,
            shareCode = "wapi-share-${savedTripShares.size + 100}",
            shareTokenAlias = "wapi-share-${savedTripShares.size + 100}",
            status = "active",
            isActive = true,
            createdAt = "2026-11-01T10:15:00Z"
        )
        savedTripShares.add(0, share)
        share
    }

    override suspend fun revokeTripShare(shareId: String): Result<Unit> = runCatching {
        val uid = currentUserIdOrThrow()
        val idx = savedTripShares.indexOfFirst { it.id == shareId && it.effectiveOwnerUserId == uid }
        if (idx >= 0) {
            savedTripShares[idx] = savedTripShares[idx].copy(status = "revoked", isActive = false)
        }
    }
}

private class FakeWapiNotificationRepository(
    private val authRepository: FakeWapiAuthRepository
) : WapiNotificationRepository {
    val savedNotifications = mutableListOf<WapiNotification>()
    private var nextClockTick = 10

    private fun currentUserIdOrNull(): String? {
        val session = authRepository.sessionFlow.value
        return (session as? SessionState.Authenticated)?.userId?.takeIf { it.isNotBlank() }
    }

    fun emitBackendNotification(
        userId: String,
        type: WapiNotificationType,
        title: String,
        body: String,
        data: WapiNotificationData
    ) {
        nextClockTick += 1
        val createdAt = String.format(java.util.Locale.US, "2026-10-22T09:%02d:00Z", nextClockTick.coerceAtMost(59))
        savedNotifications.add(
            0,
            WapiNotification(
                id = "notif-${savedNotifications.size + 1}",
                userId = userId,
                type = type.wireValue,
                title = title,
                body = body,
                data = data,
                isRead = false,
                createdAt = createdAt
            )
        )
    }

    override suspend fun getMyNotifications(): Result<List<WapiNotification>> = runCatching {
        val uid = currentUserIdOrNull() ?: return@runCatching emptyList()
        savedNotifications
            .filter { it.userId == uid }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun getUnreadNotificationCount(): Result<Int> = runCatching {
        val uid = currentUserIdOrNull() ?: return@runCatching 0
        savedNotifications.count { it.userId == uid && !it.isRead }
    }

    override suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = runCatching {
        val uid = currentUserIdOrNull() ?: return@runCatching
        val idx = savedNotifications.indexOfFirst { it.id == notificationId && it.userId == uid }
        if (idx >= 0) {
            savedNotifications[idx] = savedNotifications[idx].copy(isRead = true)
        }
    }

    override suspend fun markAllNotificationsAsRead(): Result<Unit> = runCatching {
        val uid = currentUserIdOrNull() ?: return@runCatching
        for (i in savedNotifications.indices) {
            val n = savedNotifications[i]
            if (n.userId == uid && !n.isRead) {
                savedNotifications[i] = n.copy(isRead = true)
            }
        }
    }
}

private class FakeWapiMessagingRepository(
    private val authRepository: FakeWapiAuthRepository
) : WapiMessagingRepository {

    val savedConversations = mutableListOf<WapiConversation>()
    val savedMembers = mutableListOf<WapiConversationMember>()
    val savedMessages = mutableListOf<WapiMessage>()
    val knownProfiles = mutableMapOf<String, DriverSummary>()

    private fun currentUserIdOrNull(): String? {
        return (authRepository.sessionFlow.value as? SessionState.Authenticated)?.userId
    }

    private fun isMember(conversationId: String, userId: String): Boolean {
        return savedMembers.any { it.conversationId == conversationId && it.userId == userId }
    }

    override suspend fun getMyConversations(): Result<List<WapiConversation>> {
        val currentUserId = currentUserIdOrNull()
            ?: return Result.failure(IllegalStateException("Please sign in to view your messages."))
        val myConvIds = savedMembers
            .filter { it.userId == currentUserId }
            .map { it.conversationId }
            .toSet()

        val result = savedConversations
            .filter { it.id in myConvIds }
            .map { conv ->
                val members = savedMembers
                    .filter { it.conversationId == conv.id }
                    .map { m -> m.copy(profileSummary = m.profileSummary ?: knownProfiles[m.userId]) }
                val otherParticipant = members.firstOrNull { it.userId != currentUserId }?.profileSummary
                val convMessages = savedMessages
                    .filter { it.conversationId == conv.id }
                    .sortedBy { it.createdAt }
                val latest = convMessages.lastOrNull()
                val unread = convMessages.count { it.senderId != currentUserId && !it.isEffectivelyRead }
                conv.copy(
                    members = members,
                    otherParticipant = otherParticipant ?: conv.otherParticipant,
                    latestMessage = latest,
                    unreadCount = unread
                )
            }
            .sortedByDescending { it.sortTimestamp }

        return Result.success(result)
    }

    override suspend fun getConversationMembers(conversationId: String): Result<List<WapiConversationMember>> {
        val currentUserId = currentUserIdOrNull()
            ?: return Result.failure(IllegalStateException("Please sign in to view conversation members."))
        if (!isMember(conversationId, currentUserId)) {
            return Result.success(emptyList())
        }
        return Result.success(
            savedMembers
                .filter { it.conversationId == conversationId }
                .map { m -> m.copy(profileSummary = m.profileSummary ?: knownProfiles[m.userId]) }
        )
    }

    override suspend fun getMessagesForConversation(conversationId: String): Result<List<WapiMessage>> {
        val currentUserId = currentUserIdOrNull()
            ?: return Result.failure(IllegalStateException("Please sign in to view messages."))
        if (!isMember(conversationId, currentUserId)) {
            return Result.success(emptyList())
        }
        return Result.success(
            savedMessages
                .filter { it.conversationId == conversationId }
                .sortedBy { it.createdAt }
        )
    }

    override suspend fun sendMessage(conversationId: String, content: String): Result<WapiMessage> {
        val currentUserId = currentUserIdOrNull()
            ?: return Result.failure(IllegalStateException("Please sign in to send messages."))
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty."))
        }
        if (!isMember(conversationId, currentUserId)) {
            return Result.failure(IllegalStateException("RLS violation: User is not a member of this conversation."))
        }
        val msg = WapiMessage(
            id = "msg-${savedMessages.size + 1}",
            conversationId = conversationId,
            senderId = currentUserId,
            message = trimmed,
            content = trimmed,
            readAt = null,
            isRead = false,
            createdAt = "2026-11-01T10:${(10 + savedMessages.size).toString().padStart(2, '0')}:00Z"
        )
        savedMessages.add(msg)
        return Result.success(msg)
    }

    override suspend fun markConversationMessagesAsRead(conversationId: String): Result<Unit> {
        val currentUserId = currentUserIdOrNull()
            ?: return Result.failure(IllegalStateException("Please sign in to update messages."))
        if (!isMember(conversationId, currentUserId)) {
            return Result.success(Unit)
        }
        for (i in savedMessages.indices) {
            val m = savedMessages[i]
            if (m.conversationId == conversationId && m.senderId != currentUserId && m.readAt == null) {
                savedMessages[i] = m.copy(
                    readAt = "2026-10-08T12:00:00Z",
                    isRead = true
                )
            }
        }
        return Result.success(Unit)
    }

    override suspend fun getUnreadMessageCount(): Result<Int> {
        val currentUserId = currentUserIdOrNull() ?: return Result.success(0)
        val myConvIds = savedMembers
            .filter { it.userId == currentUserId }
            .map { it.conversationId }
            .toSet()
        val count = savedMessages.count { m ->
            m.conversationId in myConvIds && m.senderId != currentUserId && !m.isEffectivelyRead
        }
        return Result.success(count)
    }

    override suspend fun resolveConversationForBookingOrTrip(
        bookingId: String?,
        tripId: String?,
        otherUserId: String?
    ): Result<WapiConversation?> {
        val currentUserId = currentUserIdOrNull()
            ?: return Result.failure(IllegalStateException("Please sign in to message."))
        val existing = savedConversations.firstOrNull { conv ->
            val memberIds = savedMembers.filter { it.conversationId == conv.id }.map { it.userId }.toSet()
            currentUserId in memberIds && (
                (!bookingId.isNullOrBlank() && conv.bookingId == bookingId) ||
                    (!tripId.isNullOrBlank() && conv.tripId == tripId && (otherUserId.isNullOrBlank() || otherUserId in memberIds))
                )
        }
        if (existing != null) {
            return getMyConversations().mapCatching { list ->
                list.firstOrNull { it.id == existing.id } ?: existing
            }
        }
        return Result.success(null)
    }
}

private class FakeWapiTripRepository(
    private val authRepository: FakeWapiAuthRepository,
    val notificationRepository: FakeWapiNotificationRepository = FakeWapiNotificationRepository(authRepository)
) : WapiTripRepository {
    val savedTrips = mutableListOf<Trip>()
    val savedStops = mutableListOf<TripStop>()
    val savedRequests = mutableListOf<TripRequest>()
    val savedBookings = mutableListOf<Booking>()
    val savedRatings = mutableListOf<Rating>()
    val profilesById = mutableMapOf<String, UserProfile>()
    val confirmTripRequestRpcCalls = mutableListOf<String>()
    val startBookingRpcCalls = mutableListOf<String>()
    val completeBookingRpcCalls = mutableListOf<String>()
    val cancelBookingRpcCalls = mutableListOf<String>()
    val submitRatingRpcCalls = mutableListOf<Triple<String, Int, String?>>()

    private fun currentUserId(): String {
        val session = authRepository.sessionFlow.value
        return (session as? SessionState.Authenticated)?.userId
            ?: throw IllegalStateException("No authenticated user.")
    }

    override suspend fun createTrip(
        origin: String,
        destination: String,
        departureDate: String,
        departureTime: String,
        seats: Int,
        contributionAmount: Double?,
        notes: String?,
        vehicleId: String?
    ): Result<Trip> = runCatching {
        val uid = currentUserId()
        val trip = Trip(
            id = "trip-${savedTrips.size + 1}",
            driverId = uid,
            vehicleId = vehicleId?.trim()?.takeIf { it.isNotEmpty() },
            origin = origin,
            destination = destination,
            departureDate = departureDate,
            departureTime = SupabaseWapiTripRepository.normalizePostgresTime(departureTime),
            totalSeats = seats,
            availableSeats = seats,
            contributionAmount = contributionAmount,
            tripStatus = "published",
            notes = notes,
            driverSummary = DriverSummary(
                id = uid,
                fullName = authRepository.profile.fullName,
                ratingAvg = 4.9,
                completedTripsCount = 12
            )
        )
        savedTrips.add(0, trip)
        trip
    }

    override suspend fun fetchMyDriverTrips(): Result<List<Trip>> = runCatching {
        val uid = currentUserId()
        savedTrips.filter { it.driverId == uid }
    }

    override suspend fun searchTrips(
        origin: String,
        destination: String,
        departureDate: String,
        preferredTime: String?
    ): Result<List<Trip>> = runCatching {
        val cleanOrigin = origin.trim()
        val cleanDestination = destination.trim()
        val cleanDate = departureDate.trim()
        val cleanPreferredTime = preferredTime?.trim()?.takeIf { it.isNotEmpty() }

        val dateCandidates = savedTrips.filter { trip ->
            trip.departureDate == cleanDate &&
                (trip.tripStatus.equals("published", ignoreCase = true) ||
                    trip.tripStatus.equals("full", ignoreCase = true))
        }.map { trip ->
            val stops = savedStops.filter { it.tripId == trip.id }.sortedBy { it.sequence }
            trip.copy(tripStops = stops)
        }

        val compatible = dateCandidates.filter { trip ->
            val match = com.example.data.WapiMatchScorer.scoreTrip(
                trip = trip,
                searchOrigin = cleanOrigin,
                searchDestination = cleanDestination,
                preferredTime = cleanPreferredTime,
                stops = trip.tripStops
            )
            match.breakdown.originScore > 0 && match.breakdown.destinationScore > 0
        }

        com.example.data.WapiMatchScorer.rankTrips(
            trips = compatible,
            searchOrigin = cleanOrigin,
            searchDestination = cleanDestination,
            preferredTime = cleanPreferredTime
        )
    }

    override suspend fun fetchTripById(tripId: String): Result<Trip> = runCatching {
        savedTrips.firstOrNull { it.id == tripId }
            ?: throw NoSuchElementException("Trip not found.")
    }

    override suspend fun getTripStops(tripId: String): Result<List<TripStop>> = runCatching {
        savedStops.filter { it.tripId == tripId }.sortedBy { it.sequence }
    }

    override suspend fun getPassengerRequestForTrip(tripId: String): Result<TripRequest?> = runCatching {
        val uid = currentUserId()
        val requests = savedRequests.filter { it.tripId == tripId && it.passengerId == uid }
        val chosen = requests.firstOrNull {
            it.status.equals("accepted", ignoreCase = true) ||
                it.status.equals("pending", ignoreCase = true)
        } ?: requests.firstOrNull()

        if (chosen != null) {
            val booking = savedBookings.firstOrNull { it.tripRequestId == chosen.id }?.let { b ->
                b.copy(existingRating = savedRatings.firstOrNull { it.bookingId == b.id && it.reviewerId == uid })
            }
            return@runCatching chosen.copy(booking = booking)
        }

        // Also check if a direct booking exists for this passenger on this trip
        val directBooking = savedBookings.firstOrNull { it.tripId == tripId && it.passengerId == uid }?.let { b ->
            b.copy(existingRating = savedRatings.firstOrNull { it.bookingId == b.id && it.reviewerId == uid })
        }
        if (directBooking != null) {
            return@runCatching TripRequest(
                id = directBooking.tripRequestId ?: "req-direct-${directBooking.id}",
                tripId = tripId,
                passengerId = uid,
                requestedSeats = directBooking.seatsBooked,
                status = "accepted",
                booking = directBooking
            )
        }

        null
    }

    override suspend fun createTripRequest(
        trip: Trip,
        requestedSeats: Int,
        pickupStopId: String?,
        dropoffStopId: String?,
        message: String?
    ): Result<TripRequest> = runCatching {
        val uid = currentUserId()
        val liveTrip = savedTrips.firstOrNull { it.id == trip.id }
            ?: throw IllegalArgumentException("Trip does not exist.")

        if (liveTrip.driverId == uid) {
            throw IllegalArgumentException("You cannot request a seat on your own ride.")
        }
        if (liveTrip.availableSeats <= 0 || !liveTrip.tripStatus.equals("published", ignoreCase = true)) {
            throw IllegalStateException("Those seats are no longer available.")
        }
        if (requestedSeats < 1 || requestedSeats > liveTrip.availableSeats) {
            throw IllegalArgumentException("Invalid requested seats.")
        }

        val existingActive = savedRequests.firstOrNull {
            it.tripId == liveTrip.id &&
                it.passengerId == uid &&
                (it.status.equals("pending", ignoreCase = true) ||
                    it.status.equals("accepted", ignoreCase = true))
        }
        if (existingActive != null) {
            return@runCatching existingActive
        }

        val pickupName = pickupStopId?.let { id -> savedStops.firstOrNull { it.id == id }?.name }
        val dropoffName = dropoffStopId?.let { id -> savedStops.firstOrNull { it.id == id }?.name }

        val newReq = TripRequest(
            id = "req-${savedRequests.size + 1}",
            tripId = liveTrip.id,
            passengerId = uid,
            requestedSeats = requestedSeats,
            pickupStopId = pickupStopId,
            dropoffStopId = dropoffStopId,
            status = "pending",
            message = message,
            createdAt = "2026-10-20T07:15:00Z",
            passengerSummary = DriverSummary(
                id = uid,
                fullName = authRepository.profile.fullName,
                ratingAvg = 4.8,
                completedTripsCount = 7
            ),
            tripSummary = liveTrip,
            pickupStopName = pickupName,
            dropoffStopName = dropoffName
        )
        savedRequests.add(0, newReq)
        // Simulates PostgreSQL trigger: trg_trip_request_created_notification -> trip_request_received
        val seatWord = if (requestedSeats == 1) "seat" else "seats"
        notificationRepository.emitBackendNotification(
            userId = liveTrip.driverId,
            type = WapiNotificationType.TRIP_REQUEST_RECEIVED,
            title = "New seat request",
            body = "A passenger requested $requestedSeats $seatWord on your ${liveTrip.origin} → ${liveTrip.destination} ride.",
            data = WapiNotificationData(
                tripId = liveTrip.id,
                tripRequestId = newReq.id
            )
        )
        newReq
    }

    override suspend fun cancelTripRequest(requestId: String): Result<TripRequest> = runCatching {
        val uid = currentUserId()
        val idx = savedRequests.indexOfFirst {
            it.id == requestId && it.passengerId == uid && it.status.equals("pending", ignoreCase = true)
        }
        if (idx < 0) throw IllegalStateException("Pending request not found.")
        val updated = savedRequests[idx].copy(status = "cancelled")
        savedRequests[idx] = updated
        // Simulates PostgreSQL trigger: trg_trip_request_status_notification -> trip_request_cancelled
        val trip = savedTrips.firstOrNull { it.id == updated.tripId }
        if (trip != null) {
            notificationRepository.emitBackendNotification(
                userId = trip.driverId,
                type = WapiNotificationType.TRIP_REQUEST_CANCELLED,
                title = "Seat request cancelled",
                body = "A passenger cancelled their seat request for ${trip.origin} → ${trip.destination}.",
                data = WapiNotificationData(
                    tripId = trip.id,
                    tripRequestId = updated.id
                )
            )
        }
        updated
    }

    override suspend fun getDriverTripRequests(): Result<List<TripRequest>> = runCatching {
        val uid = currentUserId()
        val myTripIds = savedTrips.filter { it.driverId == uid }.map { it.id }.toSet()
        savedRequests.filter { myTripIds.contains(it.tripId) }.map { req ->
            val trip = savedTrips.firstOrNull { it.id == req.tripId }
            val booking = savedBookings.firstOrNull { it.tripRequestId == req.id }
            req.copy(tripSummary = trip, booking = booking)
        }.sortedWith(
            compareBy<TripRequest> {
                when (it.status.lowercase()) {
                    "pending" -> 0
                    "accepted" -> 1
                    else -> 2
                }
            }.thenByDescending { it.createdAt }
        )
    }

    override suspend fun acceptTripRequest(requestId: String): Result<Booking?> = runCatching {
        // Simulates the authoritative PostgreSQL RPC `public.confirm_trip_request(p_trip_request_id)`
        confirmTripRequestRpcCalls.add(requestId)
        val uid = currentUserId()
        val reqIdx = savedRequests.indexOfFirst { it.id == requestId }
        if (reqIdx < 0) throw IllegalStateException("Request not found.")
        val req = savedRequests[reqIdx]
        if (!req.status.equals("pending", ignoreCase = true)) {
            throw IllegalStateException("Request is no longer pending.")
        }

        val tripIdx = savedTrips.indexOfFirst { it.id == req.tripId }
        if (tripIdx < 0) throw IllegalStateException("Trip not found.")
        val trip = savedTrips[tripIdx]

        if (trip.driverId != uid) {
            throw IllegalStateException("Only the trip driver can confirm this request.")
        }
        if (req.passengerId == trip.driverId) {
            throw IllegalStateException("Driver cannot book their own trip.")
        }
        if (trip.availableSeats < req.requestedSeats) {
            throw IllegalStateException("Not enough seats available")
        }

        val remainingSeats = trip.availableSeats - req.requestedSeats
        val updatedTripStatus = if (remainingSeats == 0) "full" else trip.tripStatus
        val updatedTrip = trip.copy(
            availableSeats = remainingSeats,
            tripStatus = updatedTripStatus
        )
        savedTrips[tripIdx] = updatedTrip

        val bookingRef = "WAPI-AB12CD${savedBookings.size + 10}"
        val booking = Booking(
            id = "booking-${savedBookings.size + 1}",
            tripId = trip.id,
            passengerId = req.passengerId,
            driverId = trip.driverId,
            tripRequestId = req.id,
            seatsBooked = req.requestedSeats,
            pickupStopId = req.pickupStopId,
            dropoffStopId = req.dropoffStopId,
            status = "confirmed",
            bookingReference = bookingRef,
            pickupStopName = req.pickupStopName,
            dropoffStopName = req.dropoffStopName,
            passengerSummary = req.passengerSummary
        )
        savedBookings.add(booking)

        savedRequests[reqIdx] = req.copy(
            status = "accepted",
            tripSummary = updatedTrip,
            booking = booking
        )

        // Simulates PostgreSQL RPC notification: trip_request_accepted
        notificationRepository.emitBackendNotification(
            userId = req.passengerId,
            type = WapiNotificationType.TRIP_REQUEST_ACCEPTED,
            title = "Seat request accepted",
            body = "Your seat on ${trip.origin} → ${trip.destination} is confirmed.",
            data = WapiNotificationData(
                tripId = trip.id,
                tripRequestId = req.id,
                bookingId = booking.id
            )
        )
        booking
    }

    override suspend fun declineTripRequest(requestId: String): Result<TripRequest> = runCatching {
        val reqIdx = savedRequests.indexOfFirst { it.id == requestId }
        if (reqIdx < 0) throw IllegalStateException("Request not found.")
        val updated = savedRequests[reqIdx].copy(status = "declined")
        savedRequests[reqIdx] = updated
        // Simulates PostgreSQL trigger: trg_trip_request_status_notification -> trip_request_declined
        val trip = savedTrips.firstOrNull { it.id == updated.tripId }
        if (trip != null) {
            notificationRepository.emitBackendNotification(
                userId = updated.passengerId,
                type = WapiNotificationType.TRIP_REQUEST_DECLINED,
                title = "Seat request declined",
                body = "Your seat request for ${trip.origin} → ${trip.destination} was declined.",
                data = WapiNotificationData(
                    tripId = trip.id,
                    tripRequestId = updated.id
                )
            )
        }
        updated
    }

    override suspend fun getBookingForRequest(
        requestId: String,
        tripId: String?,
        passengerId: String?
    ): Result<Booking?> = runCatching {
        if (requestId.isNotBlank()) {
            savedBookings.firstOrNull { it.tripRequestId == requestId }?.let { return@runCatching it }
        }
        if (!tripId.isNullOrBlank() && !passengerId.isNullOrBlank()) {
            return@runCatching savedBookings.firstOrNull {
                it.tripId == tripId && it.passengerId == passengerId
            }
        }
        null
    }

    override suspend fun getBookingsForTrip(tripId: String): Result<List<Booking>> = runCatching {
        val uid = runCatching { currentUserId() }.getOrDefault("")
        savedBookings.filter { it.tripId == tripId }.map { booking ->
            booking.copy(
                existingRating = savedRatings.firstOrNull { it.bookingId == booking.id && it.reviewerId == uid }
            )
        }
    }

    override suspend fun getMyPassengerBookings(): Result<List<Booking>> = runCatching {
        val uid = currentUserId()
        savedBookings.filter { it.passengerId == uid }.map { booking ->
            booking.copy(
                tripSummary = savedTrips.firstOrNull { it.id == booking.tripId } ?: booking.tripSummary,
                existingRating = savedRatings.firstOrNull { it.bookingId == booking.id && it.reviewerId == uid }
            )
        }
    }

    override suspend fun startBooking(bookingId: String): Result<Booking?> = runCatching {
        // Simulates PostgreSQL RPC: public.start_booking(p_booking_id)
        startBookingRpcCalls.add(bookingId)
        val uid = currentUserId()

        val bookingIdx = savedBookings.indexOfFirst { it.id == bookingId }
        if (bookingIdx < 0) throw IllegalStateException("Booking not found")
        val targetBooking = savedBookings[bookingIdx]

        if (targetBooking.driverId != uid) {
            throw IllegalStateException("Unauthorized: only the trip driver can start this trip")
        }

        val tripIdx = savedTrips.indexOfFirst { it.id == targetBooking.tripId }
        if (tripIdx < 0) throw IllegalStateException("Associated trip not found")
        val trip = savedTrips[tripIdx]

        if (trip.driverId != uid) {
            throw IllegalStateException("Unauthorized: only the trip driver can start this trip")
        }

        // Idempotency: if already in_progress, ensure trip and any remaining confirmed bookings are in_progress and return
        if (targetBooking.status.equals("in_progress", ignoreCase = true)) {
            if (trip.tripStatus.equals("published", ignoreCase = true) || trip.tripStatus.equals("full", ignoreCase = true)) {
                savedTrips[tripIdx] = trip.copy(tripStatus = "in_progress")
            }
            for (i in savedBookings.indices) {
                val b = savedBookings[i]
                if (b.tripId == trip.id && b.status.equals("confirmed", ignoreCase = true)) {
                    savedBookings[i] = b.copy(status = "in_progress")
                }
            }
            return@runCatching savedBookings[bookingIdx]
        }

        if (!targetBooking.status.equals("confirmed", ignoreCase = true)) {
            if (targetBooking.status.equals("completed", ignoreCase = true)) {
                throw IllegalStateException("This trip has already been completed")
            }
            throw IllegalStateException("Invalid status transition: cannot start booking with status ${targetBooking.status}")
        }

        if (trip.tripStatus.equals("cancelled", ignoreCase = true)) {
            throw IllegalStateException("Cannot start a cancelled trip")
        }
        if (trip.tripStatus.equals("completed", ignoreCase = true)) {
            throw IllegalStateException("This trip has already been completed")
        }

        // Move trip to in_progress
        val updatedTrip = trip.copy(tripStatus = "in_progress")
        savedTrips[tripIdx] = updatedTrip

        // Move all eligible confirmed bookings for this trip to in_progress (leave cancelled, completed, no_show, disputed untouched)
        for (i in savedBookings.indices) {
            val b = savedBookings[i]
            if (b.tripId == trip.id && b.status.equals("confirmed", ignoreCase = true)) {
                savedBookings[i] = b.copy(
                    status = "in_progress",
                    completedAt = null,
                    tripSummary = updatedTrip
                )
                // Simulates PostgreSQL RPC notification: trip_started
                notificationRepository.emitBackendNotification(
                    userId = b.passengerId,
                    type = WapiNotificationType.TRIP_STARTED,
                    title = "Trip started",
                    body = "Your ride from ${trip.origin} to ${trip.destination} is now in progress.",
                    data = WapiNotificationData(
                        tripId = trip.id,
                        bookingId = b.id,
                        tripRequestId = b.tripRequestId
                    )
                )
            }
        }

        savedBookings[bookingIdx]
    }

    override suspend fun completeBooking(bookingId: String): Result<Booking?> = runCatching {
        // Simulates PostgreSQL RPC: public.complete_booking(p_booking_id)
        completeBookingRpcCalls.add(bookingId)
        val uid = currentUserId()

        val bookingIdx = savedBookings.indexOfFirst { it.id == bookingId }
        if (bookingIdx < 0) throw IllegalStateException("Booking not found")
        val targetBooking = savedBookings[bookingIdx]

        if (targetBooking.driverId != uid) {
            throw IllegalStateException("Unauthorized: only the trip driver can complete this trip")
        }

        val tripIdx = savedTrips.indexOfFirst { it.id == targetBooking.tripId }
        if (tripIdx < 0) throw IllegalStateException("Associated trip not found")
        val trip = savedTrips[tripIdx]

        if (trip.driverId != uid) {
            throw IllegalStateException("Unauthorized: only the trip driver can complete this trip")
        }

        // Idempotency: if already completed, return without duplicating anything
        if (targetBooking.status.equals("completed", ignoreCase = true)) {
            return@runCatching targetBooking
        }

        if (!targetBooking.status.equals("in_progress", ignoreCase = true)) {
            throw IllegalStateException("Invalid status transition: booking must be in_progress to complete")
        }

        if (!trip.tripStatus.equals("in_progress", ignoreCase = true)) {
            if (trip.tripStatus.equals("completed", ignoreCase = true)) {
                throw IllegalStateException("This trip has already been completed")
            }
            throw IllegalStateException("Invalid status transition: trip must be in_progress to complete")
        }

        // Complete the trip (preserving availableSeats, totalSeats, contributionAmount, driverId)
        val updatedTrip = trip.copy(tripStatus = "completed")
        savedTrips[tripIdx] = updatedTrip

        // Complete all eligible in_progress bookings belonging to the trip (leave no_show, disputed, cancelled untouched)
        for (i in savedBookings.indices) {
            val b = savedBookings[i]
            if (b.tripId == trip.id && b.status.equals("in_progress", ignoreCase = true)) {
                savedBookings[i] = b.copy(
                    status = "completed",
                    completedAt = b.completedAt ?: "2026-10-22T11:30:00Z",
                    tripSummary = updatedTrip
                )
                // Also increment passenger completedTrips in profile if tracked
                val passengerProf = profilesById[b.passengerId]
                if (passengerProf != null) {
                    val updatedProf = passengerProf.copy(completedTrips = passengerProf.completedTrips + 1)
                    profilesById[b.passengerId] = updatedProf
                    if (authRepository.profile.id == b.passengerId) {
                        authRepository.profile = updatedProf
                    }
                }

                // Simulates PostgreSQL RPC notifications: trip_completed & rating_available
                notificationRepository.emitBackendNotification(
                    userId = b.passengerId,
                    type = WapiNotificationType.TRIP_COMPLETED,
                    title = "Trip completed",
                    body = "Your trip from ${trip.origin} to ${trip.destination} is complete. You can now leave a rating.",
                    data = WapiNotificationData(
                        tripId = trip.id,
                        bookingId = b.id,
                        tripRequestId = b.tripRequestId
                    )
                )
                notificationRepository.emitBackendNotification(
                    userId = b.passengerId,
                    type = WapiNotificationType.RATING_AVAILABLE,
                    title = "Rate your trip",
                    body = "How was your ${trip.origin} → ${trip.destination} trip? Tap to leave a rating.",
                    data = WapiNotificationData(
                        tripId = trip.id,
                        bookingId = b.id,
                        tripRequestId = b.tripRequestId
                    )
                )
                notificationRepository.emitBackendNotification(
                    userId = trip.driverId,
                    type = WapiNotificationType.RATING_AVAILABLE,
                    title = "Rate your passenger",
                    body = "Your ${trip.origin} → ${trip.destination} trip is complete. Tap to rate your passenger.",
                    data = WapiNotificationData(
                        tripId = trip.id,
                        bookingId = b.id,
                        tripRequestId = b.tripRequestId
                    )
                )
            }
        }

        // Increment driver completedTrips in profile if tracked
        val driverProf = profilesById[trip.driverId]
        if (driverProf != null) {
            val updatedDriverProf = driverProf.copy(completedTrips = driverProf.completedTrips + 1)
            profilesById[trip.driverId] = updatedDriverProf
            if (authRepository.profile.id == trip.driverId) {
                authRepository.profile = updatedDriverProf
            }
        }

        savedBookings[bookingIdx]
    }

    override suspend fun cancelBooking(bookingId: String): Result<Booking?> = runCatching {
        // Simulates PostgreSQL RPC: public.cancel_booking(p_booking_id)
        cancelBookingRpcCalls.add(bookingId)
        val uid = currentUserId()

        val bookingIdx = savedBookings.indexOfFirst { it.id == bookingId }
        if (bookingIdx < 0) throw IllegalStateException("Booking not found")
        val targetBooking = savedBookings[bookingIdx]

        if (uid != targetBooking.passengerId && uid != targetBooking.driverId) {
            throw IllegalStateException("Unauthorized: you are not allowed to cancel this booking")
        }

        if (!targetBooking.status.equals("confirmed", ignoreCase = true)) {
            throw IllegalStateException("This booking can no longer be cancelled")
        }

        val tripIdx = savedTrips.indexOfFirst { it.id == targetBooking.tripId }
        if (tripIdx < 0) throw IllegalStateException("Associated trip not found")
        val trip = savedTrips[tripIdx]

        if (!trip.tripStatus.equals("published", ignoreCase = true) &&
            !trip.tripStatus.equals("full", ignoreCase = true)
        ) {
            throw IllegalStateException("This booking can no longer be cancelled")
        }

        // Atomically restore seats without exceeding total_seats
        val restoredSeats = (trip.availableSeats + targetBooking.seatsBooked).coerceAtMost(trip.totalSeats)
        val nextTripStatus = if (trip.tripStatus.equals("full", ignoreCase = true) && restoredSeats > 0) {
            "published"
        } else {
            trip.tripStatus
        }

        val updatedTrip = trip.copy(
            availableSeats = restoredSeats,
            tripStatus = nextTripStatus
        )
        savedTrips[tripIdx] = updatedTrip

        val cancelledBooking = targetBooking.copy(
            status = "cancelled",
            cancelledAt = "2026-10-22T06:30:00Z",
            tripSummary = updatedTrip
        )
        savedBookings[bookingIdx] = cancelledBooking

        // Simulates PostgreSQL RPC notification: booking_cancelled to counterparty
        val recipientId = if (uid == targetBooking.passengerId) targetBooking.driverId else targetBooking.passengerId
        val body = if (uid == targetBooking.passengerId) {
            "A passenger cancelled their confirmed booking for ${trip.origin} → ${trip.destination}."
        } else {
            "Your confirmed booking for ${trip.origin} → ${trip.destination} was cancelled by the driver."
        }
        notificationRepository.emitBackendNotification(
            userId = recipientId,
            type = WapiNotificationType.BOOKING_CANCELLED,
            title = "Booking cancelled",
            body = body,
            data = WapiNotificationData(
                tripId = trip.id,
                bookingId = cancelledBooking.id,
                tripRequestId = cancelledBooking.tripRequestId
            )
        )
        cancelledBooking
    }

    override suspend fun submitRating(
        bookingId: String,
        rating: Int,
        review: String?
    ): Result<Rating> = runCatching {
        // Simulates PostgreSQL RPC: public.submit_rating(p_booking_id, p_rating, p_review)
        submitRatingRpcCalls.add(Triple(bookingId, rating, review))
        val uid = currentUserId()

        if (rating < 1 || rating > 5) {
            throw IllegalArgumentException("Please select a rating from 1 to 5 stars.")
        }

        val trimmedReview = review?.trim()?.takeIf { it.isNotEmpty() }
        if (trimmedReview != null && trimmedReview.length > 500) {
            throw IllegalArgumentException("Review must be 500 characters or fewer.")
        }

        val booking = savedBookings.firstOrNull { it.id == bookingId }
            ?: throw IllegalStateException("Booking not found")

        if (!booking.status.equals("completed", ignoreCase = true)) {
            throw IllegalStateException("Only completed bookings can be rated")
        }

        val revieweeId = when (uid) {
            booking.passengerId -> booking.driverId
            booking.driverId -> booking.passengerId
            else -> throw IllegalStateException("Unauthorized: user is not a participant in this booking")
        }

        if (revieweeId == uid) {
            throw IllegalStateException("Self-rating is not allowed")
        }

        val duplicate = savedRatings.firstOrNull {
            it.bookingId == bookingId && it.reviewerId == uid
        }
        if (duplicate != null) {
            throw IllegalStateException("You have already rated this trip")
        }

        val newRating = Rating(
            id = "rating-${savedRatings.size + 1}",
            bookingId = booking.id,
            tripId = booking.tripId,
            reviewerId = uid,
            revieweeId = revieweeId,
            rating = rating,
            review = trimmedReview,
            createdAt = "2026-10-22T12:00:00Z"
        )
        savedRatings.add(newRating)

        // Recalculate rating_average and rating_count on reviewee's profile from public.ratings
        val revieweeRatings = savedRatings.filter { it.revieweeId == revieweeId }
        val newCount = revieweeRatings.size
        val newAvg = if (newCount > 0) {
            Math.round((revieweeRatings.sumOf { it.rating }.toDouble() / newCount) * 100.0) / 100.0
        } else {
            0.0
        }

        val existingRevieweeProfile = profilesById[revieweeId] ?: if (authRepository.profile.id == revieweeId) {
            authRepository.profile
        } else {
            UserProfile(
                id = revieweeId,
                fullName = "User $revieweeId",
                phone = "+254700000000",
                email = "$revieweeId@wapirides.co.ke"
            )
        }

        val updatedRevieweeProfile = existingRevieweeProfile.copy(
            ratingAverage = newAvg,
            ratingCount = newCount
        )
        profilesById[revieweeId] = updatedRevieweeProfile
        if (authRepository.profile.id == revieweeId) {
            authRepository.profile = updatedRevieweeProfile
        }

        // Also update driverSummary on any trips belonging to revieweeId
        for (i in savedTrips.indices) {
            val t = savedTrips[i]
            if (t.driverId == revieweeId && t.driverSummary != null) {
                savedTrips[i] = t.copy(
                    driverSummary = t.driverSummary!!.copy(
                        ratingAvg = newAvg,
                        ratingCount = newCount,
                        completedTripsCount = updatedRevieweeProfile.completedTrips
                    )
                )
            }
        }

        newRating
    }

    override suspend fun getRatingForBooking(bookingId: String): Result<Rating?> = runCatching {
        val uid = currentUserId()
        savedRatings.firstOrNull { it.bookingId == bookingId && it.reviewerId == uid }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read app name and redirect url constants`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Wapi Rides", appName)
        assertEquals("wapi://login-callback", SupabaseConnectivityClient.AUTH_REDIRECT_URL)
    }

    @Test
    fun `welcome screen displays core brand messages and navigates to sign in and create account`() {
        val fakeRepo = FakeWapiAuthRepository(initialSession = SessionState.Unauthenticated)
        val viewModel = WapiAppViewModel(
            repository = fakeRepo,
            tripRepository = FakeWapiTripRepository(fakeRepo)
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("welcome_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wapi_brand_logo_slot").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wapi_brand_logo_slot_image", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Share the journey. Share the cost.").assertIsDisplayed()

        composeTestRule.onNodeWithTag("welcome_create_account_button").performClick()
        composeTestRule.onNodeWithTag("create_account_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wapi_brand_logo_slot").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wapi_brand_logo_slot_image", useUnmergedTree = true).assertIsDisplayed()

        composeTestRule.onNodeWithTag("create_account_back_button").performClick()
        composeTestRule.onNodeWithTag("welcome_sign_in_button").performClick()
        composeTestRule.onNodeWithTag("sign_in_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wapi_brand_logo_slot").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wapi_brand_logo_slot_image", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `create account validates kenyan phone and password matching`() {
        val fakeRepo = FakeWapiAuthRepository(initialSession = SessionState.Unauthenticated)
        val viewModel = WapiAppViewModel(
            repository = fakeRepo,
            tripRepository = FakeWapiTripRepository(fakeRepo)
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("welcome_create_account_button").performClick()

        composeTestRule.onNodeWithTag("create_account_full_name_input").performScrollTo().performTextInput("Amina Ochieng")
        composeTestRule.onNodeWithTag("create_account_phone_input").performScrollTo().performTextInput("12345")
        composeTestRule.onNodeWithTag("create_account_email_input").performScrollTo().performTextInput("amina@wapirides.co.ke")
        composeTestRule.onNodeWithTag("create_account_password_input").performScrollTo().performTextInput("secret123")
        composeTestRule.onNodeWithTag("create_account_confirm_password_input").performScrollTo().performTextInput("different")

        composeTestRule.onNodeWithTag("create_account_submit_button").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("create_account_phone_input_error").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_account_confirm_password_input_error").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `authenticated session lands on home screen and navigates across bottom tabs and offer ride`() {
        val fakeRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = "user-123",
                email = "james@wapirides.co.ke"
            ),
            profile = UserProfile(
                id = "user-123",
                fullName = "James Mwangi",
                phone = "+254712345678",
                email = "james@wapirides.co.ke"
            )
        )
        val viewModel = WapiAppViewModel(
            repository = fakeRepo,
            tripRepository = FakeWapiTripRepository(fakeRepo)
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_find_ride_action").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_offer_ride_action").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_offer_ride_action").performClick()
        composeTestRule.onNodeWithTag("offer_ride_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("offer_ride_back_button").performClick()

        composeTestRule.onNodeWithTag("bottom_nav_find").performClick()
        composeTestRule.onNodeWithTag("find_ride_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.onNodeWithTag("trips_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_nav_messages").performClick()
        composeTestRule.onNodeWithTag("messages_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_full_name_text").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_sign_out_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("welcome_screen").assertIsDisplayed()
    }

    @Test
    fun `find a ride validates inputs searches trips and opens trip details`() {
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = "passenger-uid-111",
                email = "amina@wapirides.co.ke"
            )
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)
        fakeTripRepo.savedTrips.add(
            Trip(
                id = "trip-101",
                driverId = "driver-uid-999",
                origin = "Westlands, Nairobi",
                destination = "Nakuru CBD",
                departureDate = "2026-10-18",
                departureTime = "08:00:00",
                totalSeats = 3,
                availableSeats = 2,
                contributionAmount = 800.0,
                tripStatus = "published",
                notes = "Pickup at ABC Place",
                driverSummary = DriverSummary(
                    id = "driver-uid-999",
                    fullName = "James Mwangi",
                    ratingAvg = 4.9,
                    completedTripsCount = 15
                )
            )
        )

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("home_find_ride_action").performClick()
        composeTestRule.onNodeWithTag("find_ride_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("find_ride_from_input").performScrollTo().performTextInput("Nairobi")
        composeTestRule.onNodeWithTag("find_ride_to_input").performScrollTo().performTextInput("nairobi")
        viewModel.onFindDateSelected("2026-10-18")

        composeTestRule.onNodeWithTag("find_rides_submit_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("find_ride_to_input_error").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("find_ride_to_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("find_ride_to_input").performScrollTo().performTextInput("Nakuru")
        composeTestRule.onNodeWithTag("find_rides_submit_button").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("find_ride_results_list").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("find_ride_result_item_0").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_origin_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_destination_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_contribution_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_driver_name").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_request_seat_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `offer a ride validates inputs and publishes trip to repository and updates home and trips screens`() {
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = "driver-uid-999",
                email = "james@wapirides.co.ke"
            ),
            profile = UserProfile(
                id = "driver-uid-999",
                fullName = "James Mwangi",
                phone = "+254712345678",
                email = "james@wapirides.co.ke"
            )
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)
        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("home_offer_ride_action").performClick()
        composeTestRule.onNodeWithTag("offer_ride_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("offer_ride_from_input").performScrollTo().performTextInput("Nairobi")
        composeTestRule.onNodeWithTag("offer_ride_to_input").performScrollTo().performTextInput("nairobi")
        composeTestRule.onNodeWithTag("offer_ride_seats_input").performScrollTo().performTextInput("0")
        viewModel.onOfferDateSelected("2026-10-15")
        viewModel.onOfferTimeSelected("07:30:00")

        composeTestRule.onNodeWithTag("offer_ride_publish_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("offer_ride_to_input_error").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("offer_ride_seats_input_error").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("offer_ride_to_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("offer_ride_to_input").performScrollTo().performTextInput("Nakuru")
        composeTestRule.onNodeWithTag("offer_ride_seats_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("offer_ride_seats_input").performScrollTo().performTextInput("3")
        composeTestRule.onNodeWithTag("offer_ride_notes_input").performScrollTo().performTextInput("Leaving from Westlands stage")

        composeTestRule.onNodeWithTag("offer_ride_publish_button").performScrollTo().performClick()

        assertEquals(1, fakeTripRepo.savedTrips.size)
        val savedTrip = fakeTripRepo.savedTrips.first()
        assertEquals("driver-uid-999", savedTrip.driverId)
        assertEquals("Nairobi", savedTrip.origin)
        assertEquals("Nakuru", savedTrip.destination)
        assertEquals("2026-10-15", savedTrip.departureDate)
        assertEquals("07:30:00", savedTrip.departureTime)
        assertEquals(3, savedTrip.totalSeats)
        assertEquals(3, savedTrip.availableSeats)
        assertNull(savedTrip.contributionAmount)
        assertEquals("published", savedTrip.tripStatus)
        assertEquals("Leaving from Westlands stage", savedTrip.notes)

        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_trip_published_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_upcoming_trip_card").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.onNodeWithTag("trips_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_item_upcoming_0").assertIsDisplayed()
    }

    @Test
    fun `passenger requests seat and cancels pending request while self-booking is prevented`() {
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = "passenger-uid-111",
                email = "amina@wapirides.co.ke"
            ),
            profile = UserProfile(
                id = "passenger-uid-111",
                fullName = "Amina Ochieng",
                phone = "+254712345678",
                email = "amina@wapirides.co.ke"
            )
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)
        fakeTripRepo.savedTrips.add(
            Trip(
                id = "trip-driver-1",
                driverId = "driver-uid-999",
                origin = "Westlands, Nairobi",
                destination = "Nakuru CBD",
                departureDate = "2026-10-20",
                departureTime = "08:00:00",
                totalSeats = 3,
                availableSeats = 2,
                contributionAmount = 800.0,
                tripStatus = "published",
                notes = "Pickup at ABC Place",
                driverSummary = DriverSummary(
                    id = "driver-uid-999",
                    fullName = "James Mwangi",
                    ratingAvg = 4.9,
                    completedTripsCount = 15
                )
            )
        )
        fakeTripRepo.savedTrips.add(
            Trip(
                id = "trip-own-1",
                driverId = "passenger-uid-111",
                origin = "Nairobi",
                destination = "Naivasha",
                departureDate = "2026-10-20",
                departureTime = "09:00:00",
                totalSeats = 4,
                availableSeats = 4,
                contributionAmount = 500.0,
                tripStatus = "published"
            )
        )

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // 1. Verify driver viewing their own ride sees "Your ride" / "Manage Ride" instead of "Request Seat"
        composeTestRule.onNodeWithTag("home_upcoming_trip_card").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_own_ride_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_manage_ride_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trips_screen").assertIsDisplayed()

        // 2. Passenger navigates to Find tab, searches for Nairobi -> Nakuru ride, and requests 2 seats
        composeTestRule.onNodeWithTag("bottom_nav_find").performClick()
        composeTestRule.onNodeWithTag("find_ride_from_input").performScrollTo().performTextInput("Nairobi")
        composeTestRule.onNodeWithTag("find_ride_to_input").performScrollTo().performTextInput("Nakuru")
        viewModel.onFindDateSelected("2026-10-20")
        composeTestRule.onNodeWithTag("find_rides_submit_button").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("find_ride_result_item_0").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()

        // Increment seat count to 2 and add optional message
        composeTestRule.onNodeWithTag("request_seat_increment_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("request_seat_message_input").performScrollTo()
            .performTextInput("Can I be picked up near ABC Place?")
        composeTestRule.onNodeWithTag("trip_details_request_seat_button").performScrollTo().performClick()

        // Verify pending request was created in trip_requests (and NO booking created yet)
        assertEquals(1, fakeTripRepo.savedRequests.size)
        assertEquals(0, fakeTripRepo.savedBookings.size)
        val createdReq = fakeTripRepo.savedRequests.first()
        assertEquals("pending", createdReq.status)
        assertEquals(2, createdReq.requestedSeats)
        assertEquals("Can I be picked up near ABC Place?", createdReq.message)

        // UI now displays Pending Request state ("Waiting for driver") and Cancel Request button
        composeTestRule.onNodeWithTag("trip_details_pending_request_card").performScrollTo().assertIsDisplayed()

        // Passenger cancels their pending request
        composeTestRule.onNodeWithTag("trip_details_cancel_request_button").performScrollTo().performClick()
        assertEquals("cancelled", fakeTripRepo.savedRequests.first().status)
        composeTestRule.onNodeWithTag("trip_details_historical_status_banner").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `driver accepts request via confirm_trip_request rpc creates booking reduces seats and handles race condition`() {
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = "driver-uid-999",
                email = "james@wapirides.co.ke"
            )
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)
        val limitedTrip = Trip(
            id = "trip-limited-seats",
            driverId = "driver-uid-999",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-22",
            departureTime = "07:00:00",
            totalSeats = 2,
            availableSeats = 1, // Only 1 seat remaining
            contributionAmount = 800.0,
            tripStatus = "published"
        )
        fakeTripRepo.savedTrips.add(limitedTrip)

        // Two competing pending requests for the last 1 seat
        val req1 = TripRequest(
            id = "req-101",
            tripId = "trip-limited-seats",
            passengerId = "passenger-1",
            requestedSeats = 1,
            status = "pending",
            message = "Ready at Westlands stage",
            createdAt = "2026-10-22T06:10:00Z",
            passengerSummary = DriverSummary(
                id = "passenger-1",
                fullName = "Amina Ochieng",
                ratingAvg = 4.9,
                completedTripsCount = 8
            ),
            tripSummary = limitedTrip
        )
        val req2 = TripRequest(
            id = "req-102",
            tripId = "trip-limited-seats",
            passengerId = "passenger-2",
            requestedSeats = 1,
            status = "pending",
            message = "Also heading to Nakuru",
            createdAt = "2026-10-22T06:05:00Z",
            passengerSummary = DriverSummary(
                id = "passenger-2",
                fullName = "David Kamau",
                ratingAvg = 4.7,
                completedTripsCount = 4
            ),
            tripSummary = limitedTrip
        )
        fakeTripRepo.savedRequests.add(req1)
        fakeTripRepo.savedRequests.add(req2)

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // Open Trips -> switch to Requests tab (index 1)
        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.onNodeWithTag("trips_tab_1").performClick()

        composeTestRule.onNodeWithTag("driver_request_item_0").performScrollTo().assertIsDisplayed()

        // Driver accepts req-101 -> invokes confirm_trip_request RPC
        composeTestRule.onNodeWithTag("driver_request_item_0_accept_button").performScrollTo().performClick()

        assertTrue(fakeTripRepo.confirmTripRequestRpcCalls.contains("req-101"))
        assertEquals(1, fakeTripRepo.savedBookings.size)
        val booking = fakeTripRepo.savedBookings.first()
        assertTrue(booking.bookingReference.startsWith("WAPI-"))
        assertEquals("confirmed", booking.status)

        // Trip available_seats reduced atomically to 0 and tripStatus became "full"
        val updatedTrip = fakeTripRepo.savedTrips.first()
        assertEquals(0, updatedTrip.availableSeats)
        assertEquals("full", updatedTrip.tripStatus)

        // Now req-102 is at index 0 (since pending requests sort first).
        // Attempting to accept req-102 when 0 seats remain triggers "Not enough seats available" -> "Those seats are no longer available."
        composeTestRule.onNodeWithTag("driver_request_item_0_accept_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("driver_requests_error_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Those seats are no longer available.").assertIsDisplayed()

        // Driver declines req-102 -> status becomes declined without creating a second booking
        composeTestRule.onNodeWithTag("driver_request_item_0_decline_button").performScrollTo().performClick()
        assertEquals(1, fakeTripRepo.savedBookings.size)
        assertEquals(
            "declined",
            fakeTripRepo.savedRequests.first { it.id == "req-102" }.status
        )
    }

    @Test
    fun `phase 4_4 driver starts and completes multi-passenger trip while preserving no_show and disputed bookings and idempotency`() {
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = "driver-uid-999",
                email = "james@wapirides.co.ke"
            )
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)

        val trip = Trip(
            id = "trip-multi-1",
            driverId = "driver-uid-999",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-25",
            departureTime = "08:00:00",
            totalSeats = 4,
            availableSeats = 0,
            contributionAmount = 800.0,
            tripStatus = "full"
        )
        fakeTripRepo.savedTrips.add(trip)

        // Multiple bookings on the same trip:
        // Passenger A = 1 (confirmed)
        // Passenger B = 2 (confirmed)
        // Passenger C = 1 (confirmed)
        // Plus existing no_show, disputed, and cancelled bookings that must NOT be altered
        val bookingA = Booking(
            id = "booking-a",
            tripId = "trip-multi-1",
            passengerId = "passenger-a",
            driverId = "driver-uid-999",
            seatsBooked = 1,
            status = "confirmed",
            bookingReference = "WAPI-AAA111",
            passengerSummary = DriverSummary(id = "passenger-a", fullName = "Amina Ochieng")
        )
        val bookingB = Booking(
            id = "booking-b",
            tripId = "trip-multi-1",
            passengerId = "passenger-b",
            driverId = "driver-uid-999",
            seatsBooked = 2,
            status = "confirmed",
            bookingReference = "WAPI-BBB222",
            passengerSummary = DriverSummary(id = "passenger-b", fullName = "Brian Kiprop")
        )
        val bookingC = Booking(
            id = "booking-c",
            tripId = "trip-multi-1",
            passengerId = "passenger-c",
            driverId = "driver-uid-999",
            seatsBooked = 1,
            status = "confirmed",
            bookingReference = "WAPI-CCC333",
            passengerSummary = DriverSummary(id = "passenger-c", fullName = "Carol Wanjiku")
        )
        val bookingNoShow = Booking(
            id = "booking-noshow",
            tripId = "trip-multi-1",
            passengerId = "passenger-ns",
            driverId = "driver-uid-999",
            seatsBooked = 1,
            status = "no_show",
            bookingReference = "WAPI-NS000"
        )
        val bookingDisputed = Booking(
            id = "booking-disputed",
            tripId = "trip-multi-1",
            passengerId = "passenger-dp",
            driverId = "driver-uid-999",
            seatsBooked = 1,
            status = "disputed",
            bookingReference = "WAPI-DP000"
        )
        val bookingCancelled = Booking(
            id = "booking-cancelled",
            tripId = "trip-multi-1",
            passengerId = "passenger-cx",
            driverId = "driver-uid-999",
            seatsBooked = 1,
            status = "cancelled",
            bookingReference = "WAPI-CX000"
        )

        fakeTripRepo.savedBookings.addAll(
            listOf(bookingA, bookingB, bookingC, bookingNoShow, bookingDisputed, bookingCancelled)
        )

        // Verify authorization guards directly at repository level:
        // Passenger cannot start or complete a booking
        fakeAuthRepo.sessionFlow.value = SessionState.Authenticated("passenger-a", "amina@wapirides.co.ke")
        runBlocking {
            val passengerStartAttempt = fakeTripRepo.startBooking("booking-a")
            assertTrue(passengerStartAttempt.isFailure)
            assertEquals(
                "You are not allowed to perform this action.",
                SupabaseConnectivityClient.toUserFriendlyMessage(
                    passengerStartAttempt.exceptionOrNull()!!,
                    "Fallback"
                )
            )

            val passengerCompleteAttempt = fakeTripRepo.completeBooking("booking-a")
            assertTrue(passengerCompleteAttempt.isFailure)
            assertEquals(
                "You are not allowed to perform this action.",
                SupabaseConnectivityClient.toUserFriendlyMessage(
                    passengerCompleteAttempt.exceptionOrNull()!!,
                    "Fallback"
                )
            )
        }

        // Switch back to Driver session and verify UI & RPC lifecycle transitions
        fakeAuthRepo.sessionFlow.value = SessionState.Authenticated("driver-uid-999", "james@wapirides.co.ke")

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // Open Trip Details from Home upcoming trip card
        composeTestRule.onNodeWithTag("home_upcoming_trip_card").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()

        // Driver sees "Ride ready" and "Start Trip"
        composeTestRule.onNodeWithTag("trip_details_own_ride_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Ride ready").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_start_trip_button").performScrollTo().assertIsDisplayed()

        // Driver taps "Start Trip"
        composeTestRule.onNodeWithTag("trip_details_start_trip_button").performScrollTo().performClick()

        // 1. start_booking RPC called
        assertTrue(fakeTripRepo.startBookingRpcCalls.isNotEmpty())
        // 2. Trip moved to in_progress
        assertEquals("in_progress", fakeTripRepo.savedTrips.first().tripStatus)
        // 3. All 3 eligible confirmed bookings moved to in_progress with completedAt = null
        assertEquals("in_progress", fakeTripRepo.savedBookings.first { it.id == "booking-a" }.status)
        assertEquals("in_progress", fakeTripRepo.savedBookings.first { it.id == "booking-b" }.status)
        assertEquals("in_progress", fakeTripRepo.savedBookings.first { it.id == "booking-c" }.status)
        assertNull(fakeTripRepo.savedBookings.first { it.id == "booking-a" }.completedAt)
        // 4. no_show, disputed, and cancelled bookings were NOT altered
        assertEquals("no_show", fakeTripRepo.savedBookings.first { it.id == "booking-noshow" }.status)
        assertEquals("disputed", fakeTripRepo.savedBookings.first { it.id == "booking-disputed" }.status)
        assertEquals("cancelled", fakeTripRepo.savedBookings.first { it.id == "booking-cancelled" }.status)

        // 5. Repeated start call is idempotent and safe
        runBlocking {
            val repeatedStart = fakeTripRepo.startBooking("booking-a")
            assertTrue(repeatedStart.isSuccess)
            assertEquals("in_progress", repeatedStart.getOrNull()?.status)
        }

        // UI now shows "Trip in progress" and "Complete Trip"
        composeTestRule.onNodeWithTag("trip_details_complete_trip_button").performScrollTo().assertIsDisplayed()

        // Driver taps "Complete Trip"
        composeTestRule.onNodeWithTag("trip_details_complete_trip_button").performScrollTo().performClick()

        // 6. complete_booking RPC called
        assertTrue(fakeTripRepo.completeBookingRpcCalls.isNotEmpty())
        // 7. Trip moved to completed without altering seats or contribution
        val completedTrip = fakeTripRepo.savedTrips.first()
        assertEquals("completed", completedTrip.tripStatus)
        assertEquals(0, completedTrip.availableSeats)
        assertEquals(4, completedTrip.totalSeats)
        assertEquals(800.0, completedTrip.contributionAmount!!, 0.01)

        // 8. All 3 eligible in_progress bookings moved to completed with completedAt populated
        val completedA = fakeTripRepo.savedBookings.first { it.id == "booking-a" }
        val completedB = fakeTripRepo.savedBookings.first { it.id == "booking-b" }
        val completedC = fakeTripRepo.savedBookings.first { it.id == "booking-c" }
        assertEquals("completed", completedA.status)
        assertEquals("completed", completedB.status)
        assertEquals("completed", completedC.status)
        assertNotNull(completedA.completedAt)

        // 9. no_show, disputed, and cancelled bookings remain untouched
        assertEquals("no_show", fakeTripRepo.savedBookings.first { it.id == "booking-noshow" }.status)
        assertEquals("disputed", fakeTripRepo.savedBookings.first { it.id == "booking-disputed" }.status)
        assertEquals("cancelled", fakeTripRepo.savedBookings.first { it.id == "booking-cancelled" }.status)

        // 10. Repeated completion is idempotent and safe
        runBlocking {
            val repeatedComplete = fakeTripRepo.completeBooking("booking-a")
            assertTrue(repeatedComplete.isSuccess)
            assertEquals("completed", repeatedComplete.getOrNull()?.status)

            // Cancelled booking cannot be completed
            val completeCancelledAttempt = fakeTripRepo.completeBooking("booking-cancelled")
            assertTrue(completeCancelledAttempt.isFailure)

            // Completed booking cannot be cancelled
            val cancelCompletedAttempt = fakeTripRepo.cancelBooking("booking-a")
            assertTrue(cancelCompletedAttempt.isFailure)
        }

        // UI now shows "Trip completed" and future-ready "Rate your trip" card
        composeTestRule.onNodeWithTag("trip_details_rate_trip_card").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `phase 4_4 passenger and driver can cancel confirmed booking restoring seats atomically and reopening full trip`() {
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = "passenger-1",
                email = "amina@wapirides.co.ke"
            ),
            profile = UserProfile(
                id = "passenger-1",
                fullName = "Amina Ochieng",
                phone = "+254712345678",
                email = "amina@wapirides.co.ke"
            )
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)

        // Trip starts as "full" with 0 of 3 seats available because Passenger 1 booked 2 seats and Passenger 2 booked 1 seat
        val fullTrip = Trip(
            id = "trip-cancel-test",
            driverId = "driver-uid-999",
            origin = "Nairobi",
            destination = "Eldoret",
            departureDate = "2026-10-28",
            departureTime = "07:30:00",
            totalSeats = 3,
            availableSeats = 0,
            contributionAmount = 1200.0,
            tripStatus = "full"
        )
        fakeTripRepo.savedTrips.add(fullTrip)

        val reqPassenger1 = TripRequest(
            id = "req-p1",
            tripId = "trip-cancel-test",
            passengerId = "passenger-1",
            requestedSeats = 2,
            status = "accepted"
        )
        val bookingPassenger1 = Booking(
            id = "booking-p1",
            tripId = "trip-cancel-test",
            passengerId = "passenger-1",
            driverId = "driver-uid-999",
            tripRequestId = "req-p1",
            seatsBooked = 2,
            status = "confirmed",
            bookingReference = "WAPI-P1BOOK"
        )
        val bookingPassenger2 = Booking(
            id = "booking-p2",
            tripId = "trip-cancel-test",
            passengerId = "passenger-2",
            driverId = "driver-uid-999",
            seatsBooked = 1,
            status = "confirmed",
            bookingReference = "WAPI-P2BOOK"
        )

        fakeTripRepo.savedRequests.add(reqPassenger1)
        fakeTripRepo.savedBookings.add(bookingPassenger1)
        fakeTripRepo.savedBookings.add(bookingPassenger2)

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // Passenger opens Trips -> Upcoming -> clicks their confirmed trip
        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.onNodeWithTag("trip_item_upcoming_0").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()

        // Passenger sees "Confirmed" booking card and "Cancel Booking" button (and does NOT see Start Trip)
        composeTestRule.onNodeWithTag("trip_details_confirmed_booking_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_cancel_booking_button").performScrollTo().performClick()

        // Confirmation dialog appears with "Cancel this ride?" and "Your reserved seats will become available again."
        composeTestRule.onNodeWithTag("cancel_booking_confirm_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel this ride?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your reserved seats will become available again.").assertIsDisplayed()

        // Passenger confirms cancellation
        composeTestRule.onNodeWithTag("cancel_booking_confirm_button").performClick()

        // Verify cancel_booking RPC was invoked
        assertTrue(fakeTripRepo.cancelBookingRpcCalls.contains("booking-p1"))

        // Booking status is now "cancelled"
        val updatedBookingP1 = fakeTripRepo.savedBookings.first { it.id == "booking-p1" }
        assertEquals("cancelled", updatedBookingP1.status)
        assertNotNull(updatedBookingP1.cancelledAt)

        // Seats restored atomically: 0 + 2 = 2 available seats, and full trip returned to "published"!
        val tripAfterPassengerCancel = fakeTripRepo.savedTrips.first()
        assertEquals(2, tripAfterNowAvailableSeats(tripAfterPassengerCancel))
        assertEquals("published", tripAfterPassengerCancel.tripStatus)

        // Driver can also cancel Passenger 2's confirmed booking before departure, restoring the remaining 1 seat (total 3, capped at totalSeats)
        fakeAuthRepo.sessionFlow.value = SessionState.Authenticated("driver-uid-999", "james@wapirides.co.ke")
        runBlocking {
            val driverCancelResult = fakeTripRepo.cancelBooking("booking-p2")
            assertTrue(driverCancelResult.isSuccess)
            assertEquals("cancelled", driverCancelResult.getOrNull()?.status)
        }

        val tripAfterDriverCancel = fakeTripRepo.savedTrips.first()
        assertEquals(3, tripAfterDriverCancel.availableSeats)
        assertEquals("published", tripAfterDriverCancel.tripStatus)
    }

    private fun tripAfterNowAvailableSeats(trip: Trip): Int = trip.availableSeats

    @Test
    fun `phase 4_5 passenger rates driver and driver rates passenger with security checks and profile trust updates`() {
        val driverProfile = UserProfile(
            id = "driver-uid-1",
            fullName = "James Mwangi",
            phone = "+254712345678",
            email = "james@wapirides.co.ke",
            ratingAverage = 0.0,
            ratingCount = 0,
            completedTrips = 1
        )
        val passengerProfile = UserProfile(
            id = "passenger-uid-1",
            fullName = "Amina Ochieng",
            phone = "+254722111222",
            email = "amina@wapirides.co.ke",
            ratingAverage = 0.0,
            ratingCount = 0,
            completedTrips = 1
        )

        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(
                userId = passengerProfile.id,
                email = passengerProfile.email
            ),
            profile = passengerProfile
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)
        fakeTripRepo.profilesById[driverProfile.id] = driverProfile
        fakeTripRepo.profilesById[passengerProfile.id] = passengerProfile

        val completedTrip = Trip(
            id = "trip-rate-1",
            driverId = driverProfile.id,
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-20",
            departureTime = "08:00:00",
            totalSeats = 3,
            availableSeats = 2,
            contributionAmount = 700.0,
            tripStatus = "completed",
            driverSummary = DriverSummary(
                id = driverProfile.id,
                fullName = driverProfile.fullName,
                ratingAvg = 0.0,
                ratingCount = 0,
                completedTripsCount = 1
            )
        )
        fakeTripRepo.savedTrips.add(completedTrip)

        val acceptedReq = TripRequest(
            id = "req-rate-1",
            tripId = completedTrip.id,
            passengerId = passengerProfile.id,
            requestedSeats = 1,
            status = "accepted"
        )
        val completedBooking = Booking(
            id = "booking-rate-1",
            tripId = completedTrip.id,
            passengerId = passengerProfile.id,
            driverId = driverProfile.id,
            tripRequestId = acceptedReq.id,
            seatsBooked = 1,
            status = "completed",
            bookingReference = "WAPI-RATE01",
            completedAt = "2026-10-20T11:00:00Z",
            passengerSummary = DriverSummary(
                id = passengerProfile.id,
                fullName = passengerProfile.fullName,
                ratingAvg = 0.0,
                ratingCount = 0,
                completedTripsCount = 1
            ),
            tripSummary = completedTrip
        )
        val nonCompletedBooking = Booking(
            id = "booking-not-completed",
            tripId = "trip-upcoming-1",
            passengerId = passengerProfile.id,
            driverId = driverProfile.id,
            seatsBooked = 1,
            status = "confirmed",
            bookingReference = "WAPI-CONF01"
        )
        fakeTripRepo.savedRequests.add(acceptedReq)
        fakeTripRepo.savedBookings.add(completedBooking)
        fakeTripRepo.savedBookings.add(nonCompletedBooking)

        // Security checks before UI interaction:
        runBlocking {
            // 1. Non-completed booking rating rejected
            val nonCompletedResult = fakeTripRepo.submitRating("booking-not-completed", 5, "Too early")
            assertTrue(nonCompletedResult.isFailure)
            assertEquals(
                "Ratings can only be submitted after the trip is completed.",
                SupabaseConnectivityClient.toUserFriendlyMessage(
                    nonCompletedResult.exceptionOrNull()!!,
                    "Unable to submit rating right now."
                )
            )

            // 2. Invalid star count rejected
            val invalidStarsResult = fakeTripRepo.submitRating("booking-rate-1", 6, "Out of range")
            assertTrue(invalidStarsResult.isFailure)

            // 3. Unauthorized outsider cannot rate booking
            fakeAuthRepo.sessionFlow.value = SessionState.Authenticated("outsider-uid-999", "outsider@wapirides.co.ke")
            val outsiderResult = fakeTripRepo.submitRating("booking-rate-1", 4, "Not my booking")
            assertTrue(outsiderResult.isFailure)
            assertEquals(
                "Only confirmed trip participants can rate this journey.",
                SupabaseConnectivityClient.toUserFriendlyMessage(
                    outsiderResult.exceptionOrNull()!!,
                    "Unable to submit rating right now."
                )
            )

            // Restore passenger session
            fakeAuthRepo.sessionFlow.value = SessionState.Authenticated(passengerProfile.id, passengerProfile.email)
            fakeAuthRepo.profile = passengerProfile
        }

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // Navigate to Trips -> Completed tab (index 3) and verify "Not rated" badge is shown initially
        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.onNodeWithTag("trips_tab_3").performClick()
        composeTestRule.onNodeWithText("Not rated").assertIsDisplayed()

        // Open Completed Trip Details as Passenger
        composeTestRule.onNodeWithTag("trip_item_completed_0").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()

        // Verify interactive rating form appears for passenger ("Rate your trip" / "Rate your driver")
        composeTestRule.onNodeWithTag("trip_details_rate_trip_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Rate your trip").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rate your driver").assertIsDisplayed()

        // Select 5 stars and enter optional review text
        composeTestRule.onNodeWithTag("rating_star_5").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("rating_review_input")
            .performScrollTo()
            .performTextInput("Safe driver and right on time!")

        // Tap Submit Rating
        composeTestRule.onNodeWithTag("rating_submit_button").performScrollTo().performClick()

        // Verify submit_rating RPC was invoked with passenger's bookingId, 5 stars, and review
        assertTrue(fakeTripRepo.submitRatingRpcCalls.any { it.first == "booking-rate-1" && it.second == 5 })

        // Verify read-only rated state is immediately displayed ("You rated this trip ★★★★★" and "Thank you for helping keep Wapi trusted.")
        composeTestRule.onNodeWithTag("rating_submitted_badge").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("You rated this trip ★★★★★").assertIsDisplayed()
        composeTestRule.onNodeWithTag("rating_feedback_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Thank you for helping keep Wapi trusted.").assertIsDisplayed()

        // Duplicate rating by passenger is rejected at the repository/RPC level
        runBlocking {
            val duplicateAttempt = fakeTripRepo.submitRating("booking-rate-1", 4, "Trying to rate twice")
            assertTrue(duplicateAttempt.isFailure)
            assertEquals(
                "You have already rated this trip.",
                SupabaseConnectivityClient.toUserFriendlyMessage(
                    duplicateAttempt.exceptionOrNull()!!,
                    "Unable to submit rating right now."
                )
            )
        }

        // Driver profile trust metrics were recalculated in FakeWapiTripRepository (avg = 5.0, count = 1)
        val updatedDriver = fakeTripRepo.profilesById[driverProfile.id]!!
        assertEquals(5.0, updatedDriver.ratingAverage, 0.01)
        assertEquals(1, updatedDriver.ratingCount)

        // Return to Trips -> Completed tab and verify badge updated to "Rated ✓"
        composeTestRule.onNodeWithTag("trip_details_back_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trips_tab_3").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Rated ✓").assertIsDisplayed()

        // Now switch authenticated session to Driver so Driver can rate Passenger (two-way rating)
        fakeAuthRepo.profile = updatedDriver
        fakeAuthRepo.sessionFlow.value = SessionState.Authenticated(updatedDriver.id, updatedDriver.email)
        viewModel.refreshMyTrips()
        viewModel.refreshMyPassengerBookings()
        viewModel.refreshDriverRequests()

        // Navigate to Profile tab to verify Driver's updated rating average ("5.0 ★" / "1 rating") and completed trips ("1 completed trip")
        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_rating_placeholder").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("5.0 ★").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 rating").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_completed_trips_placeholder").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 completed trip").assertIsDisplayed()

        // Driver navigates to Trips -> Completed and opens the completed trip to rate the passenger
        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.onNodeWithTag("trips_tab_3").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_item_completed_0").performScrollTo().performClick()

        // Driver sees "Rate your trip" and "Rate your passenger"
        composeTestRule.onNodeWithTag("trip_details_rate_trip_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Rate your passenger").assertIsDisplayed()

        // Driver selects 4 stars and submits without a review (empty review allowed)
        composeTestRule.onNodeWithTag("rating_star_4").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("rating_submit_button").performScrollTo().performClick()

        // Verify Driver's rating of Passenger was saved and UI switched to read-only ("You rated this trip ★★★★☆")
        composeTestRule.onNodeWithTag("rating_submitted_badge").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("You rated this trip ★★★★☆").assertIsDisplayed()

        // Verify Passenger's profile rating average and count were recalculated (4.0, count = 1)
        val updatedPassenger = fakeTripRepo.profilesById[passengerProfile.id]!!
        assertEquals(4.0, updatedPassenger.ratingAverage, 0.01)
        assertEquals(1, updatedPassenger.ratingCount)
        assertEquals(2, fakeTripRepo.savedRatings.size)
    }

    @Test
    fun `malformed or expired deep link callback shows wapi branded error on sign in screen without crashing`() {
        SupabaseConnectivityClient.clearDeepLinkError()
        val malformedIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("wapi://login-callback#error=access_denied&error_code=otp_expired&error_description=Email+link+is+invalid+or+has+expired")
        )
        SupabaseConnectivityClient.handleIncomingDeepLink(malformedIntent)
        val capturedError = SupabaseConnectivityClient.deepLinkError.value
        assertNotNull(capturedError)

        val fakeRepo = FakeWapiAuthRepository(initialSession = SessionState.Unauthenticated)
        fakeRepo.deepLinkErrorFlow.value = capturedError

        val viewModel = WapiAppViewModel(
            repository = fakeRepo,
            tripRepository = FakeWapiTripRepository(fakeRepo)
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithTag("sign_in_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_in_error_banner").assertIsDisplayed()
    }

    @Test
    fun `phase 4_6b find ride ranks results by deterministic wapi match score and displays match reasons and tier in cards and trip details`() {
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated("passenger-uid-1", "passenger@wapirides.co.ke")
        )
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo)

        // 1. Full ride with top-rated driver at exact time (must rank below bookable rides)
        val fullTrip = Trip(
            id = "trip-full-top",
            driverId = "driver-full",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-28",
            departureTime = "08:00:00",
            totalSeats = 4,
            availableSeats = 0,
            contributionAmount = 800.0,
            tripStatus = "full",
            driverSummary = DriverSummary(
                id = "driver-full",
                fullName = "Samuel Kiprop",
                ratingAverage = 5.0,
                ratingCount = 15,
                completedTrips = 20
            )
        )

        // 2. Bookable ride via intermediate pickup/dropoff stops (18 + 18 route, 1 seat, new driver)
        val stopMatchedTrip = Trip(
            id = "trip-stop-match",
            driverId = "driver-stop",
            origin = "Mombasa Road",
            destination = "Eldoret",
            departureDate = "2026-10-28",
            departureTime = "08:40:00",
            totalSeats = 4,
            availableSeats = 1,
            contributionAmount = 900.0,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = "driver-stop",
                fullName = "Peter Ochieng"
            )
        )
        fakeTripRepo.savedStops.add(
            TripStop(id = "stop-1", tripId = "trip-stop-match", stopType = "pickup", name = "Nairobi", sequence = 1)
        )
        fakeTripRepo.savedStops.add(
            TripStop(id = "stop-2", tripId = "trip-stop-match", stopType = "dropoff", name = "Nakuru", sequence = 2)
        )

        // 3. Bookable ride with exact route, within 15 mins, 3 seats, top-rated driver -> 100% Excellent Match
        val topMatchTrip = Trip(
            id = "trip-top-match",
            driverId = "driver-top",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-28",
            departureTime = "08:10:00",
            totalSeats = 4,
            availableSeats = 3,
            contributionAmount = 850.0,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = "driver-top",
                fullName = "Grace Wanjiku",
                ratingAverage = 5.0,
                ratingCount = 8,
                completedTrips = 12
            )
        )

        // Add in reverse order to prove WapiMatchScorer re-ranks them deterministically
        fakeTripRepo.savedTrips.addAll(listOf(fullTrip, stopMatchedTrip, topMatchTrip))

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // Navigate to Find Ride
        composeTestRule.onNodeWithTag("bottom_nav_find").performClick()
        composeTestRule.onNodeWithTag("find_ride_screen").assertIsDisplayed()

        // Enter search criteria with preferred time 08:00:00
        composeTestRule.onNodeWithTag("find_ride_from_input").performScrollTo().performTextInput("Nairobi")
        composeTestRule.onNodeWithTag("find_ride_to_input").performScrollTo().performTextInput("Nakuru")
        viewModel.onFindDateSelected("2026-10-28")
        viewModel.onFindPreferredTimeSelected("08:00:00")
        composeTestRule.onNodeWithTag("find_rides_submit_button").performScrollTo().performClick()

        // Verify deterministic ranking order in ViewModel state:
        // 1st: trip-top-match (100% Excellent Match)
        // 2nd: trip-stop-match (Bookable stop match)
        // 3rd: trip-full-top (Full ride always ranked after bookable rides)
        val rankedIds = viewModel.uiState.value.findRideSearch.results.map { it.id }
        assertEquals(listOf("trip-top-match", "trip-stop-match", "trip-full-top"), rankedIds)

        // Verify first result card displays "100% Wapi Match", "Excellent Match", and explainable reasons
        composeTestRule.onNodeWithTag("find_ride_result_item_0").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("find_ride_result_item_0_match_score", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("100% Wapi Match", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("find_ride_result_item_0_match_tier", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Excellent Match", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("find_ride_result_item_0_match_reasons", useUnmergedTree = true).assertIsDisplayed()

        // Tap first result card to open TripDetailsScreen and verify Wapi Match summary card is shown
        composeTestRule.onNodeWithTag("find_ride_result_item_0").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_wapi_match_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_match_score", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_match_tier", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_match_reasons", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `phase 4_7a notifications load unread count mark one and all as read and navigate to trip details and driver requests`() {
        val driverId = "driver-notif-1"
        val passengerId = "passenger-notif-1"

        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(passengerId, "passenger@wapirides.co.ke"),
            profile = UserProfile(
                id = passengerId,
                fullName = "Faith Njeri",
                phone = "+254711223344",
                email = "passenger@wapirides.co.ke"
            )
        )
        val fakeNotifRepo = FakeWapiNotificationRepository(fakeAuthRepo)
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo, fakeNotifRepo)

        val trip = Trip(
            id = "trip-notif-101",
            driverId = driverId,
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-30",
            departureTime = "08:30:00",
            totalSeats = 3,
            availableSeats = 2,
            contributionAmount = 800.0,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = driverId,
                fullName = "David Kamau",
                ratingAverage = 4.9,
                ratingCount = 10,
                completedTrips = 15
            )
        )
        fakeTripRepo.savedTrips.add(trip)

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo,
            notificationRepository = fakeNotifRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // 1. Initially passenger has 0 notifications -> open Notifications screen from Home and verify empty state
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_notifications_button").assertIsDisplayed()
        assertEquals(0, viewModel.uiState.value.notificationsState.unreadCount)

        composeTestRule.onNodeWithTag("home_notifications_button").performClick()
        composeTestRule.onNodeWithTag("notifications_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notifications_empty_state").assertIsDisplayed()

        // Navigate back to Home
        composeTestRule.onNodeWithTag("notifications_back_button").performClick()
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()

        // 2. Passenger requests a seat -> backend trigger creates TRIP_REQUEST_RECEIVED notification for driver
        runBlocking {
            fakeTripRepo.createTripRequest(
                trip = trip,
                requestedSeats = 1,
                pickupStopId = null,
                dropoffStopId = null,
                message = "Joining at Westlands"
            )
        }
        val createdRequest = fakeTripRepo.savedRequests.first()

        // Switch session to Driver -> verify Driver has 1 unread notification (trip_request_received)
        fakeAuthRepo.profile = UserProfile(
            id = driverId,
            fullName = "David Kamau",
            phone = "+254722334455",
            email = "driver@wapirides.co.ke"
        )
        fakeAuthRepo.sessionFlow.value = SessionState.Authenticated(driverId, "driver@wapirides.co.ke")
        viewModel.refreshNotifications()

        assertEquals(1, viewModel.uiState.value.notificationsState.unreadCount)
        composeTestRule.onNodeWithTag("home_notifications_unread_badge", useUnmergedTree = true).assertIsDisplayed()

        // Driver opens Notifications screen and taps the TRIP_REQUEST_RECEIVED notification
        composeTestRule.onNodeWithTag("home_notifications_button").performClick()
        composeTestRule.onNodeWithTag("notifications_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notification_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithText("New seat request", useUnmergedTree = true).assertIsDisplayed()

        // Tapping TRIP_REQUEST_RECEIVED marks it as read (unreadCount -> 0) and navigates to Trips -> Incoming Requests (tab index 1)
        composeTestRule.onNodeWithTag("notification_item_0").performClick()
        assertEquals(0, viewModel.uiState.value.notificationsState.unreadCount)
        assertEquals(1, viewModel.uiState.value.selectedTripsTabIndex)
        composeTestRule.onNodeWithTag("trips_screen").assertIsDisplayed()

        // 3. Driver accepts request, starts trip, and completes trip -> generates notifications for Passenger:
        //    - trip_request_accepted
        //    - trip_started
        //    - trip_completed
        //    - rating_available
        runBlocking {
            val booking = fakeTripRepo.acceptTripRequest(createdRequest.id).getOrThrow()!!
            fakeTripRepo.startBooking(booking.id).getOrThrow()
            fakeTripRepo.completeBooking(booking.id).getOrThrow()
        }

        // Switch session back to Passenger -> verify 4 unread notifications ordered newest first
        fakeAuthRepo.profile = UserProfile(
            id = passengerId,
            fullName = "Faith Njeri",
            phone = "+254711223344",
            email = "passenger@wapirides.co.ke"
        )
        fakeAuthRepo.sessionFlow.value = SessionState.Authenticated(passengerId, "passenger@wapirides.co.ke")
        viewModel.refreshNotifications()

        val passengerNotifs = viewModel.uiState.value.notificationsState.notifications
        assertEquals(4, passengerNotifs.size)
        assertEquals(4, viewModel.uiState.value.notificationsState.unreadCount)
        assertEquals(
            listOf(
                WapiNotificationType.RATING_AVAILABLE,
                WapiNotificationType.TRIP_COMPLETED,
                WapiNotificationType.TRIP_STARTED,
                WapiNotificationType.TRIP_REQUEST_ACCEPTED
            ),
            passengerNotifs.map { it.notificationType }
        )

        // Open Notifications screen from Profile -> Notifications item ("4 unread")
        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_section_notifications").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("4 unread", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_section_notifications").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("notifications_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("notifications_unread_count_chip", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("4 new", useUnmergedTree = true).assertIsDisplayed()

        // Mark a single notification as read via ViewModel and verify unread count decrements from 4 to 3
        viewModel.markNotificationAsRead(passengerNotifs.first().id)
        assertEquals(3, viewModel.uiState.value.notificationsState.unreadCount)
        assertTrue(viewModel.uiState.value.notificationsState.notifications.first().isRead)
        composeTestRule.onNodeWithText("3 new", useUnmergedTree = true).assertIsDisplayed()

        // Click "Mark all as read" button and verify unread count becomes 0 and all items are read
        composeTestRule.onNodeWithTag("notifications_mark_all_read_button").performClick()
        assertEquals(0, viewModel.uiState.value.notificationsState.unreadCount)
        assertTrue(viewModel.uiState.value.notificationsState.notifications.all { it.isRead })

        // Tap a trip notification ("Trip completed") and verify it navigates to TripDetailsScreen for trip-notif-101
        composeTestRule.onNodeWithTag("notification_item_1").performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()
        assertEquals("trip-notif-101", viewModel.uiState.value.selectedTrip?.id)
    }

    @Test
    fun `phase 4_7b messaging lists conversations opens chat sends messages marks read updates unread count and opens from trip details`() {
        val driverId = "driver-msg-1"
        val passengerId = "passenger-msg-1"
        val outsiderId = "outsider-msg-99"

        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(passengerId, "passenger@wapirides.co.ke"),
            profile = UserProfile(
                id = passengerId,
                fullName = "Amina Hassan",
                phone = "+254711998877",
                email = "passenger@wapirides.co.ke"
            )
        )
        val fakeNotifRepo = FakeWapiNotificationRepository(fakeAuthRepo)
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo, fakeNotifRepo)
        val fakeMessagingRepo = FakeWapiMessagingRepository(fakeAuthRepo)

        fakeMessagingRepo.knownProfiles[driverId] = DriverSummary(
            id = driverId,
            fullName = "Brian Kiprop",
            ratingAverage = 4.9,
            ratingCount = 14,
            completedTrips = 20
        )
        fakeMessagingRepo.knownProfiles[passengerId] = DriverSummary(
            id = passengerId,
            fullName = "Amina Hassan",
            ratingAverage = 5.0,
            ratingCount = 6,
            completedTrips = 8
        )

        val trip = Trip(
            id = "trip-msg-201",
            driverId = driverId,
            origin = "Nairobi",
            destination = "Naivasha",
            departureDate = "2026-11-02",
            departureTime = "09:00:00",
            totalSeats = 3,
            availableSeats = 2,
            contributionAmount = 600.0,
            tripStatus = "published",
            driverSummary = fakeMessagingRepo.knownProfiles[driverId]
        )
        fakeTripRepo.savedTrips.add(trip)

        // Create a confirmed booking between passenger and driver
        val confirmedBooking = Booking(
            id = "booking-msg-201",
            tripId = trip.id,
            tripRequestId = "req-msg-201",
            passengerId = passengerId,
            driverId = driverId,
            seatsBooked = 1,
            status = "confirmed",
            tripSummary = trip,
            passengerSummary = fakeMessagingRepo.knownProfiles[passengerId]
        )
        fakeTripRepo.savedBookings.add(confirmedBooking)

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo,
            notificationRepository = fakeNotifRepo,
            messagingRepository = fakeMessagingRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // 1. Navigate to Messages tab -> verify empty state when no conversations exist
        composeTestRule.onNodeWithTag("bottom_nav_messages").performClick()
        composeTestRule.onNodeWithTag("messages_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("messages_empty_state").assertIsDisplayed()
        assertEquals(0, viewModel.uiState.value.messagingState.unreadMessageCount)

        // 2. Seed an authorized conversation (conv-1) with 1 unread message from the driver,
        //    plus an unauthorized conversation (conv-secret) belonging to outsiderId to verify RLS filtering
        fakeMessagingRepo.savedConversations.add(
            WapiConversation(
                id = "conv-1",
                tripId = trip.id,
                bookingId = confirmedBooking.id,
                createdAt = "2026-11-01T08:00:00Z"
            )
        )
        fakeMessagingRepo.savedMembers.add(
            WapiConversationMember(conversationId = "conv-1", userId = passengerId, role = "passenger")
        )
        fakeMessagingRepo.savedMembers.add(
            WapiConversationMember(conversationId = "conv-1", userId = driverId, role = "driver")
        )
        fakeMessagingRepo.savedMessages.add(
            WapiMessage(
                id = "msg-initial-1",
                conversationId = "conv-1",
                senderId = driverId,
                content = "Hi Amina, I will pick you up at ABC Place at 8:50 AM.",
                isRead = false,
                createdAt = "2026-11-01T08:05:00Z"
            )
        )

        // Unauthorized conversation that passenger is NOT a member of
        fakeMessagingRepo.savedConversations.add(
            WapiConversation(
                id = "conv-secret",
                tripId = "other-trip",
                bookingId = "other-booking",
                createdAt = "2026-11-01T09:00:00Z"
            )
        )
        fakeMessagingRepo.savedMembers.add(
            WapiConversationMember(conversationId = "conv-secret", userId = outsiderId, role = "passenger")
        )
        fakeMessagingRepo.savedMessages.add(
            WapiMessage(
                id = "msg-secret-1",
                conversationId = "conv-secret",
                senderId = outsiderId,
                content = "Private message",
                isRead = false,
                createdAt = "2026-11-01T09:05:00Z"
            )
        )

        // Refresh conversations
        viewModel.refreshConversations()
        composeTestRule.waitForIdle()

        // Verify only conv-1 is returned and unread count is 1
        assertEquals(1, viewModel.uiState.value.messagingState.conversations.size)
        assertEquals("conv-1", viewModel.uiState.value.messagingState.conversations.first().id)
        assertEquals(1, viewModel.uiState.value.messagingState.unreadMessageCount)

        // Verify Messages screen shows participant name, latest message preview, and unread badge
        composeTestRule.onNodeWithTag("conversation_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Brian Kiprop", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Hi Amina, I will pick you up at ABC Place at 8:50 AM.",
            useUnmergedTree = true
        ).assertIsDisplayed()
        composeTestRule.onNodeWithTag("conversation_item_0_unread_badge", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("bottom_nav_messages_unread_badge", useUnmergedTree = true).assertIsDisplayed()

        // 3. Tap conversation item -> opens ChatScreen and automatically marks incoming message as read
        composeTestRule.onNodeWithTag("conversation_item_0").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Brian Kiprop", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_message_item_0").assertIsDisplayed()
        assertEquals(0, viewModel.uiState.value.messagingState.unreadMessageCount)
        assertTrue(fakeMessagingRepo.savedMessages.first { it.id == "msg-initial-1" }.isRead)

        // 4. Send a reply message from passenger
        composeTestRule.onNodeWithTag("chat_message_input")
            .performTextInput("Thanks Brian! I will be there 5 minutes early.")
        composeTestRule.onNodeWithTag("chat_send_button").performClick()
        composeTestRule.waitForIdle()

        assertEquals(2, viewModel.uiState.value.messagingState.activeChat.messages.size)
        composeTestRule.onNodeWithTag("chat_message_item_1").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Thanks Brian! I will be there 5 minutes early.",
            useUnmergedTree = true
        ).assertIsDisplayed()
        assertEquals("", viewModel.uiState.value.messagingState.activeChat.draftMessage)

        // 5. Navigate back from ChatScreen -> returns to MessagesScreen with updated preview
        composeTestRule.onNodeWithTag("chat_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("messages_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            "Thanks Brian! I will be there 5 minutes early.",
            useUnmergedTree = true
        ).assertIsDisplayed()

        // 6. Verify opening Chat directly from TripDetailsScreen ("Message Driver" button on confirmed booking)
        viewModel.refreshMyPassengerBookings()
        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("trip_item_upcoming_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_message_driver_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_message_driver_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        assertEquals("conv-1", viewModel.uiState.value.messagingState.activeChat.conversationId)
    }

    @Test
    fun `phase 4_8b safety and trust foundation supports profile editing vehicles trip vehicle association verification emergency contacts and trip sharing`() {
        val userId = "driver-safety-1"
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(userId, "james@wapirides.co.ke"),
            profile = UserProfile(
                id = userId,
                fullName = "James Mwangi",
                phone = "+254712345678",
                email = "james@wapirides.co.ke",
                bio = "Regular Nairobi to Nakuru commuter.",
                status = "active",
                ratingAverage = 4.9,
                ratingCount = 12,
                completedTrips = 18
            )
        )
        val fakeNotifRepo = FakeWapiNotificationRepository(fakeAuthRepo)
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo, fakeNotifRepo)
        val fakeMessagingRepo = FakeWapiMessagingRepository(fakeAuthRepo)
        val fakeSafetyRepo = FakeWapiSafetyTrustRepository(fakeAuthRepo)

        // Seed 2 verification records (1 approved, 1 pending -> Verified)
        fakeSafetyRepo.savedVerificationRecords.add(
            WapiVerificationRecord(
                id = "ver-1",
                userId = userId,
                verificationType = "national_id",
                status = "approved",
                createdAt = "2026-10-01T10:00:00Z"
            )
        )
        fakeSafetyRepo.savedVerificationRecords.add(
            WapiVerificationRecord(
                id = "ver-2",
                userId = userId,
                verificationType = "driver_license",
                status = "pending",
                createdAt = "2026-10-02T10:00:00Z"
            )
        )

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo,
            notificationRepository = fakeNotifRepo,
            messagingRepository = fakeMessagingRepo,
            safetyTrustRepository = fakeSafetyRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // 1. Navigate to Profile -> verify bio, account status badge, and Safety & Trust items
        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio_text", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Regular Nairobi to Nakuru commuter.", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_account_status_badge", useUnmergedTree = true).assertIsDisplayed()

        // 2. Edit Profile (Full name, phone, bio)
        composeTestRule.onNodeWithTag("profile_edit_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("profile_edit_form_card").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("profile_edit_full_name_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("profile_edit_full_name_input").performScrollTo().performTextInput("James K. Mwangi")
        composeTestRule.onNodeWithTag("profile_edit_phone_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("profile_edit_phone_input").performScrollTo().performTextInput("0722987654")
        composeTestRule.onNodeWithTag("profile_edit_bio_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("profile_edit_bio_input").performScrollTo().performTextInput("Safe highway driver with 10+ years experience.")
        composeTestRule.onNodeWithTag("profile_edit_save_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals("James K. Mwangi", fakeAuthRepo.profile.fullName)
        assertEquals("+254722987654", fakeAuthRepo.profile.phone)
        assertEquals("Safe highway driver with 10+ years experience.", fakeAuthRepo.profile.bio)
        composeTestRule.onNodeWithTag("profile_edit_feedback_banner").performScrollTo().assertIsDisplayed()

        // 3. Open Vehicles screen from Profile -> add, edit, and verify vehicle management
        composeTestRule.onNodeWithTag("profile_section_vehicles").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("vehicles_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("vehicles_empty_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("vehicles_add_button").performClick()
        composeTestRule.onNodeWithTag("vehicle_form_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("vehicle_make_input").performScrollTo().performTextInput("Toyota")
        composeTestRule.onNodeWithTag("vehicle_model_input").performScrollTo().performTextInput("Fielder")
        composeTestRule.onNodeWithTag("vehicle_year_input").performScrollTo().performTextInput("2019")
        composeTestRule.onNodeWithTag("vehicle_colour_input").performScrollTo().performTextInput("Silver")
        composeTestRule.onNodeWithTag("vehicle_registration_input").performScrollTo().performTextInput("kda 456b")
        composeTestRule.onNodeWithTag("vehicle_seat_capacity_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("vehicle_seat_capacity_input").performScrollTo().performTextInput("4")
        composeTestRule.onNodeWithTag("vehicle_form_save_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, fakeSafetyRepo.savedVehicles.size)
        val addedVehicle = fakeSafetyRepo.savedVehicles.first()
        assertEquals("Toyota", addedVehicle.make)
        assertEquals("Fielder", addedVehicle.model)
        assertEquals("KDA 456B", addedVehicle.effectiveRegistration)
        composeTestRule.onNodeWithTag("vehicle_card_0").performScrollTo().assertIsDisplayed()

        // Edit the vehicle colour to "Pearl White"
        composeTestRule.onNodeWithTag("vehicle_card_0_edit_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("vehicle_form_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("vehicle_colour_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("vehicle_colour_input").performScrollTo().performTextInput("Pearl White")
        composeTestRule.onNodeWithTag("vehicle_form_save_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals("Pearl White", fakeSafetyRepo.savedVehicles.first().effectiveColour)

        // Return to Profile
        composeTestRule.onNodeWithTag("vehicles_back_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()

        // 4. Open Verification Status screen from Profile -> verify read-only status & records
        composeTestRule.onNodeWithTag("profile_section_verification").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("verification_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verification_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verification_overall_status_badge", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("verification_record_0").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("verification_record_1").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("verification_back_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()

        // 5. Open Safety & Emergency Contacts screen from Profile -> add primary emergency contact
        composeTestRule.onNodeWithTag("profile_section_safety").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("safety_emergency_contacts_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("emergency_contacts_empty_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("emergency_contact_add_button").performClick()
        composeTestRule.onNodeWithTag("emergency_contact_form_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("emergency_contact_name_input").performScrollTo().performTextInput("Grace Mwangi")
        composeTestRule.onNodeWithTag("emergency_contact_phone_input").performScrollTo().performTextInput("0711222333")
        composeTestRule.onNodeWithTag("emergency_contact_relationship_input").performScrollTo().performTextInput("Spouse")
        composeTestRule.onNodeWithTag("emergency_contact_save_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, fakeSafetyRepo.savedEmergencyContacts.size)
        val savedContact = fakeSafetyRepo.savedEmergencyContacts.first()
        assertEquals("Grace Mwangi", savedContact.effectiveName)
        assertEquals("+254711222333", savedContact.effectivePhone)
        assertTrue(savedContact.isPrimary)
        composeTestRule.onNodeWithTag("emergency_contact_card_0").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("safety_back_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()

        // 6. Navigate to Home -> Offer a Ride -> select registered vehicle and publish trip with vehicle_id
        composeTestRule.onNodeWithTag("bottom_nav_home").performClick()
        composeTestRule.onNodeWithTag("home_offer_ride_action").performClick()
        composeTestRule.onNodeWithTag("offer_ride_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("offer_ride_vehicle_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("offer_ride_vehicle_option_0").performScrollTo().assertIsDisplayed()

        // Verify seat capacity validation against selected vehicle (Toyota Fielder has 4 seats)
        composeTestRule.onNodeWithTag("offer_ride_from_input").performScrollTo().performTextInput("Nairobi")
        composeTestRule.onNodeWithTag("offer_ride_to_input").performScrollTo().performTextInput("Nakuru")
        viewModel.onOfferDateSelected("2026-11-10")
        viewModel.onOfferTimeSelected("08:00:00")
        composeTestRule.onNodeWithTag("offer_ride_seats_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("offer_ride_seats_input").performScrollTo().performTextInput("6")
        composeTestRule.onNodeWithTag("offer_ride_publish_button").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("offer_ride_seats_input_error").performScrollTo().assertIsDisplayed()

        // Set valid seats (3 <= 4) and publish
        composeTestRule.onNodeWithTag("offer_ride_seats_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("offer_ride_seats_input").performScrollTo().performTextInput("3")
        composeTestRule.onNodeWithTag("offer_ride_publish_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, fakeTripRepo.savedTrips.size)
        val publishedTrip = fakeTripRepo.savedTrips.first()
        assertEquals(addedVehicle.id, publishedTrip.vehicleId)

        // 7. Open TripDetailsScreen for the published trip -> verify vehicle summary and Trip Sharing Foundation
        composeTestRule.onNodeWithTag("home_upcoming_trip_card").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_vehicle_info_row").performScrollTo().assertIsDisplayed()

        // Record a Trip Share with primary emergency contact Grace Mwangi
        composeTestRule.onNodeWithTag("trip_details_trip_share_card").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_share_contact_option_0").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_share_trip_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, fakeSafetyRepo.savedTripShares.size)
        assertEquals(1, viewModel.uiState.value.safetyTrustState.tripShareState.activeShares.size)
        composeTestRule.onNodeWithTag("trip_details_active_share_item_0").performScrollTo().assertIsDisplayed()

        // Revoke the Trip Share and verify active shares become empty
        composeTestRule.onNodeWithTag("trip_details_revoke_share_button_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(0, viewModel.uiState.value.safetyTrustState.tripShareState.activeShares.size)
    }

    @Test
    fun `phase 4_8c step 4 avatar image processor validates mime type size crops to square 512x512 compresses jpeg and updates local preview without supabase upload`() {
        // 1. Unsupported MIME type rejection (e.g. image/gif, application/pdf)
        val dummyBytes = ByteArray(128) { 0x11 }
        val unsupportedMimeResult = AvatarImageProcessor.processImageBytes(
            rawBytes = dummyBytes,
            mimeType = "image/gif"
        )
        assertTrue(unsupportedMimeResult.isFailure)
        assertTrue(
            unsupportedMimeResult.exceptionOrNull()?.message
                ?.contains("Unsupported image format", ignoreCase = true) == true
        )

        // 2. Empty/corrupt image rejection
        val emptyResult = AvatarImageProcessor.processImageBytes(
            rawBytes = ByteArray(0),
            mimeType = "image/jpeg"
        )
        assertTrue(emptyResult.isFailure)
        assertTrue(
            emptyResult.exceptionOrNull()?.message
                ?.contains("empty", ignoreCase = true) == true
        )

        val corruptResult = AvatarImageProcessor.processImageBytes(
            rawBytes = ByteArray(64) { 0x42 },
            mimeType = "image/jpeg"
        )
        assertTrue(corruptResult.isFailure)

        // 3. Oversized input rejection (> 10 MB)
        val oversizedBytes = ByteArray(AvatarImageProcessor.MAX_INPUT_SIZE_BYTES + 1024)
        val oversizedResult = AvatarImageProcessor.processImageBytes(
            rawBytes = oversizedBytes,
            mimeType = "image/jpeg"
        )
        assertTrue(oversizedResult.isFailure)
        assertTrue(
            oversizedResult.exceptionOrNull()?.message
                ?.contains("10 MB", ignoreCase = true) == true
        )

        // 4. Valid non-square rectangular PNG (800 x 400) -> center-cropped 1:1, resized to 512x512, compressed JPEG <= 1 MB
        val sourceBitmap = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sourceBitmap)
        canvas.drawColor(Color.rgb(26, 93, 58))
        val pngOut = java.io.ByteArrayOutputStream()
        sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, pngOut)
        val validPngBytes = pngOut.toByteArray()
        sourceBitmap.recycle()

        val processedResult = AvatarImageProcessor.processImageBytes(
            rawBytes = validPngBytes,
            mimeType = "image/png"
        )
        assertTrue(processedResult.isSuccess)
        val processed = processedResult.getOrThrow()
        assertEquals("image/jpeg", processed.mimeType)
        assertEquals(512, processed.width)
        assertEquals(512, processed.height)
        assertTrue(processed.bytes.isNotEmpty())
        assertTrue(processed.bytes.size <= AvatarImageProcessor.MAX_OUTPUT_SIZE_BYTES)
        assertEquals("image/jpeg", AvatarImageProcessor.sniffImageMimeType(processed.bytes))

        // 5. Verify ViewModel & ProfileScreen local preview state and no Supabase/profile mutation
        val userId = "user-avatar-step4"
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(userId, "amina@wapirides.co.ke"),
            profile = UserProfile(
                id = userId,
                fullName = "Amina Wanjiku",
                phone = "+254712345678",
                email = "amina@wapirides.co.ke",
                avatarUrl = null
            )
        )
        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = FakeWapiTripRepository(fakeAuthRepo, FakeWapiNotificationRepository(fakeAuthRepo)),
            notificationRepository = FakeWapiNotificationRepository(fakeAuthRepo),
            messagingRepository = FakeWapiMessagingRepository(fakeAuthRepo),
            safetyTrustRepository = FakeWapiSafetyTrustRepository(fakeAuthRepo)
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // Navigate to Profile screen -> initials fallback shown initially
        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar_placeholder").assertIsDisplayed()
        composeTestRule.onNodeWithText("AW", useUnmergedTree = true).assertIsDisplayed()

        // Invalid selection shows error banner and keeps initials fallback
        viewModel.processSelectedAvatarBytes(rawBytes = dummyBytes, mimeType = "image/gif")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("profile_avatar_error_banner").assertIsDisplayed()
        assertNull(viewModel.uiState.value.safetyTrustState.avatarSelection.processedAvatarImage)
        assertNull(fakeAuthRepo.profile.avatarUrl)

        // Valid selection processes image locally, shows preview and feedback banner, and does NOT mutate profiles.avatar_url
        viewModel.processSelectedAvatarBytes(rawBytes = validPngBytes, mimeType = "image/png")
        composeTestRule.waitForIdle()

        val avatarState = viewModel.uiState.value.safetyTrustState.avatarSelection
        assertFalse(avatarState.isProcessingAvatar)
        assertNotNull(avatarState.processedAvatarImage)
        assertNotNull(avatarState.selectedAvatarPreviewBytes)
        assertEquals(512, avatarState.processedAvatarImage?.width)
        assertEquals(512, avatarState.processedAvatarImage?.height)
        assertEquals("image/jpeg", avatarState.processedAvatarImage?.mimeType)
        assertNull(avatarState.avatarErrorMessage)
        assertEquals("Profile photo ready.", avatarState.avatarFeedbackMessage)

        composeTestRule.onNodeWithTag("profile_avatar_feedback_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar_local_preview", useUnmergedTree = true).assertIsDisplayed()

        // Confirm no Supabase upload or profile avatar_url update occurred in Step 4
        assertNull(fakeAuthRepo.profile.avatarUrl)
        assertNull(viewModel.uiState.value.userProfile?.avatarUrl)
    }

    @Test
    fun `phase 4_8c step 5 supabase avatar upload persists cache busted avatar_url updates profile state handles failures and renders remote avatar`() {
        val userId = "user-avatar-step5"
        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(userId, "david@wapirides.co.ke"),
            profile = UserProfile(
                id = userId,
                fullName = "David Ochieng",
                phone = "+254722111222",
                email = "david@wapirides.co.ke",
                bio = "Commuter from Kisumu to Nairobi.",
                avatarUrl = null,
                ratingAverage = 4.8,
                ratingCount = 9,
                completedTrips = 14
            )
        )
        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = FakeWapiTripRepository(fakeAuthRepo, FakeWapiNotificationRepository(fakeAuthRepo)),
            notificationRepository = FakeWapiNotificationRepository(fakeAuthRepo),
            messagingRepository = FakeWapiMessagingRepository(fakeAuthRepo),
            safetyTrustRepository = FakeWapiSafetyTrustRepository(fakeAuthRepo)
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }

        // 1. Navigate to Profile screen -> initials fallback shown when avatarUrl is null
        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar_placeholder").assertIsDisplayed()
        composeTestRule.onNodeWithText("DO", useUnmergedTree = true).assertIsDisplayed()

        // Create a valid 600x600 bitmap and encode as PNG
        val sourceBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sourceBitmap)
        canvas.drawColor(Color.rgb(22, 101, 52))
        val pngOut = java.io.ByteArrayOutputStream()
        sourceBitmap.compress(Bitmap.CompressFormat.PNG, 100, pngOut)
        val validPngBytes = pngOut.toByteArray()
        sourceBitmap.recycle()

        // 2. Test upload failure handling (does not corrupt profile.avatarUrl and shows friendly error banner)
        fakeAuthRepo.shouldFailAvatarUpload = true
        viewModel.processSelectedAvatarBytes(
            rawBytes = validPngBytes,
            mimeType = "image/png",
            uploadImmediately = true
        )
        composeTestRule.waitForIdle()

        assertFalse(viewModel.uiState.value.safetyTrustState.avatarSelection.isUploadingAvatar)
        assertNotNull(viewModel.uiState.value.safetyTrustState.avatarSelection.avatarErrorMessage)
        assertNull(fakeAuthRepo.profile.avatarUrl)
        assertNull(viewModel.uiState.value.userProfile?.avatarUrl)
        composeTestRule.onNodeWithTag("profile_avatar_error_banner").assertIsDisplayed()

        // 3. Test successful upload and profile.avatar_url persistence with cache busting
        fakeAuthRepo.shouldFailAvatarUpload = false
        viewModel.uploadAndSaveSelectedAvatar()
        composeTestRule.waitForIdle()

        val avatarStateAfterSuccess = viewModel.uiState.value.safetyTrustState.avatarSelection
        assertFalse(avatarStateAfterSuccess.isProcessingAvatar)
        assertFalse(avatarStateAfterSuccess.isUploadingAvatar)
        assertNull(avatarStateAfterSuccess.avatarErrorMessage)
        assertEquals("Profile photo updated.", avatarStateAfterSuccess.avatarFeedbackMessage)

        assertEquals("$userId/avatar.jpg", fakeAuthRepo.lastUploadedAvatarStoragePath)
        assertEquals("image/jpeg", fakeAuthRepo.lastUploadedAvatarContentType)
        assertTrue(fakeAuthRepo.lastUploadedAvatarUpsert)
        assertNotNull(fakeAuthRepo.lastUploadedAvatarBytes)

        val persistedAvatarUrl = fakeAuthRepo.profile.avatarUrl
        assertNotNull(persistedAvatarUrl)
        assertTrue(persistedAvatarUrl!!.contains("/avatars/$userId/avatar.jpg?t="))
        assertEquals(persistedAvatarUrl, viewModel.uiState.value.userProfile?.avatarUrl)

        // Ensure other profile fields (full_name, phone, bio, rating_average, rating_count, completed_trips) remain intact
        assertEquals("David Ochieng", viewModel.uiState.value.userProfile?.fullName)
        assertEquals("+254722111222", viewModel.uiState.value.userProfile?.phone)
        assertEquals("Commuter from Kisumu to Nairobi.", viewModel.uiState.value.userProfile?.bio)
        assertEquals(4.8, viewModel.uiState.value.userProfile?.ratingAverage ?: 0.0, 0.001)
        assertEquals(9, viewModel.uiState.value.userProfile?.ratingCount)
        assertEquals(14, viewModel.uiState.value.userProfile?.completedTrips)

        composeTestRule.onNodeWithTag("profile_avatar_feedback_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar_local_preview", useUnmergedTree = true).assertIsDisplayed()

        // 4. Test unauthenticated upload rejection in repository
        fakeAuthRepo.sessionFlow.value = SessionState.Unauthenticated
        val unauthUploadResult = runBlocking {
            fakeAuthRepo.uploadAndSaveCurrentUserAvatar(fakeAuthRepo.lastUploadedAvatarBytes!!)
        }
        assertTrue(unauthUploadResult.isFailure)

        // 5. Verify remote avatar rendering path when local preview is not set (e.g. fresh session with saved avatarUrl)
        fakeAuthRepo.sessionFlow.value = SessionState.Authenticated(userId, "david@wapirides.co.ke")
        val freshViewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = FakeWapiTripRepository(fakeAuthRepo, FakeWapiNotificationRepository(fakeAuthRepo)),
            notificationRepository = FakeWapiNotificationRepository(fakeAuthRepo),
            messagingRepository = FakeWapiMessagingRepository(fakeAuthRepo),
            safetyTrustRepository = FakeWapiSafetyTrustRepository(fakeAuthRepo)
        )
        assertNull(freshViewModel.uiState.value.safetyTrustState.avatarSelection.selectedAvatarPreviewBytes)
        assertEquals(persistedAvatarUrl, freshViewModel.uiState.value.userProfile?.avatarUrl)
    }

    @Test
    fun `phase 4_8c step 6 global wapi user avatar renders remote avatar url and initials fallback across home profile find ride trip details requests messages and chat`() {
        val currentUserId = "user-step6-driver"
        val currentUserAvatarUrl = "https://supabase.wapirides.local/storage/v1/object/public/avatars/$currentUserId/avatar.jpg?t=1700000001"
        val otherDriverAvatarUrl = "https://supabase.wapirides.local/storage/v1/object/public/avatars/driver-2/avatar.jpg?t=1700000002"
        val passengerAvatarUrl = "https://supabase.wapirides.local/storage/v1/object/public/avatars/passenger-1/avatar.jpg?t=1700000003"

        val fakeAuthRepo = FakeWapiAuthRepository(
            initialSession = SessionState.Authenticated(currentUserId, "grace@wapirides.co.ke"),
            profile = UserProfile(
                id = currentUserId,
                fullName = "Grace Mwangi",
                phone = "+254711000999",
                email = "grace@wapirides.co.ke",
                avatarUrl = currentUserAvatarUrl,
                ratingAverage = 4.9,
                ratingCount = 12,
                completedTrips = 18
            )
        )
        val fakeNotifRepo = FakeWapiNotificationRepository(fakeAuthRepo)
        val fakeTripRepo = FakeWapiTripRepository(fakeAuthRepo, fakeNotifRepo)
        val fakeMessagingRepo = FakeWapiMessagingRepository(fakeAuthRepo)
        val fakeSafetyRepo = FakeWapiSafetyTrustRepository(fakeAuthRepo)

        // Seed a published trip from another driver WITH avatarUrl and one WITHOUT avatarUrl
        val tripWithAvatar = Trip(
            id = "trip-step6-with-avatar",
            driverId = "driver-2",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2099-12-15",
            departureTime = "08:30:00",
            totalSeats = 4,
            availableSeats = 3,
            contributionAmount = 800.0,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = "driver-2",
                fullName = "Samuel Kiprotich",
                avatarUrl = otherDriverAvatarUrl,
                ratingAverage = 4.8,
                ratingCount = 10,
                completedTrips = 15
            )
        )
        val tripWithoutAvatar = Trip(
            id = "trip-step6-without-avatar",
            driverId = "driver-3",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2099-12-15",
            departureTime = "10:00:00",
            totalSeats = 4,
            availableSeats = 2,
            contributionAmount = 750.0,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = "driver-3",
                fullName = "Brian Otieno",
                avatarUrl = null,
                ratingAverage = 4.7,
                ratingCount = 6,
                completedTrips = 8
            )
        )
        fakeTripRepo.savedTrips.add(tripWithAvatar)
        fakeTripRepo.savedTrips.add(tripWithoutAvatar)

        // Seed an incoming trip request for current user's driver requests tab with passenger avatarUrl
        val driverOwnTrip = Trip(
            id = "trip-step6-own",
            driverId = currentUserId,
            origin = "Nairobi",
            destination = "Eldoret",
            departureDate = "2099-12-20",
            departureTime = "07:00:00",
            totalSeats = 3,
            availableSeats = 2,
            contributionAmount = 1200.0,
            tripStatus = "published"
        )
        fakeTripRepo.savedTrips.add(driverOwnTrip)
        fakeTripRepo.savedRequests.add(
            TripRequest(
                id = "req-step6-1",
                tripId = driverOwnTrip.id,
                passengerId = "passenger-1",
                requestedSeats = 1,
                status = "pending",
                message = "Joining from Westlands",
                passengerSummary = DriverSummary(
                    id = "passenger-1",
                    fullName = "Faith Njeri",
                    avatarUrl = passengerAvatarUrl,
                    ratingAverage = 5.0,
                    ratingCount = 4,
                    completedTrips = 5
                ),
                tripSummary = driverOwnTrip
            )
        )

        // Seed a conversation with participant avatarUrl
        val convId = "conv-step6-1"
        val faithSummary = DriverSummary(
            id = "passenger-1",
            fullName = "Faith Njeri",
            avatarUrl = passengerAvatarUrl
        )
        fakeMessagingRepo.knownProfiles["passenger-1"] = faithSummary
        fakeMessagingRepo.savedConversations.add(
            WapiConversation(
                id = convId,
                tripId = driverOwnTrip.id,
                otherParticipant = faithSummary,
                tripSummary = driverOwnTrip
            )
        )
        fakeMessagingRepo.savedMembers.add(
            WapiConversationMember(
                id = "mem-step6-1",
                conversationId = convId,
                userId = currentUserId,
                role = "driver"
            )
        )
        fakeMessagingRepo.savedMembers.add(
            WapiConversationMember(
                id = "mem-step6-2",
                conversationId = convId,
                userId = "passenger-1",
                role = "passenger",
                profileSummary = faithSummary
            )
        )

        val viewModel = WapiAppViewModel(
            repository = fakeAuthRepo,
            tripRepository = fakeTripRepo,
            notificationRepository = fakeNotifRepo,
            messagingRepository = fakeMessagingRepo,
            safetyTrustRepository = fakeSafetyRepo
        )

        composeTestRule.setContent {
            WapiRidesTheme {
                WapiAppNavHost(appViewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // 1. HomeScreen header avatar button uses WapiUserAvatar with remote avatarUrl
        composeTestRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_profile_avatar_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_profile_avatar_remote_image", useUnmergedTree = true).assertExists()

        // 2. FindRideScreen -> search Nairobi to Nakuru -> verify WapiRideResultCard avatars (with remote URL and initials fallback)
        composeTestRule.onNodeWithTag("bottom_nav_find").performClick()
        composeTestRule.onNodeWithTag("find_ride_screen").assertIsDisplayed()
        viewModel.onFindOriginChange("Nairobi")
        viewModel.onFindDestinationChange("Nakuru")
        viewModel.onFindDateSelected("2099-12-15")
        viewModel.submitFindRides()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("find_ride_result_item_0_driver_avatar", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("find_ride_result_item_0_driver_avatar_remote_image", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("find_ride_result_item_1_driver_avatar", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("BO", useUnmergedTree = true).assertExists()

        // 3. TripDetailsScreen -> open tripWithAvatar -> verify driver avatar uses WapiUserAvatar
        composeTestRule.onNodeWithTag("find_ride_result_item_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("trip_details_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_driver_avatar", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_details_driver_avatar_remote_image", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("trip_details_back_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 4. TripsScreen -> Requests tab -> verify DriverTripRequestCard passenger avatar uses WapiUserAvatar
        composeTestRule.onNodeWithTag("bottom_nav_trips").performClick()
        composeTestRule.onNodeWithTag("trips_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trips_tab_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("driver_request_item_0_passenger_avatar", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("driver_request_item_0_passenger_avatar_remote_image", useUnmergedTree = true).assertExists()

        // 5. MessagesScreen & ChatScreen -> verify participant avatar uses WapiUserAvatar
        composeTestRule.onNodeWithTag("bottom_nav_messages").performClick()
        composeTestRule.onNodeWithTag("messages_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("conversation_item_0_participant_avatar", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("conversation_item_0_participant_avatar_remote_image", useUnmergedTree = true).assertExists()

        composeTestRule.onNodeWithTag("conversation_item_0").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("chat_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_participant_avatar", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_participant_avatar_remote_image", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("chat_back_button").performClick()
        composeTestRule.waitForIdle()

        // 6. ProfileScreen -> verify profile_avatar_placeholder uses WapiUserAvatar and remains tappable
        composeTestRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar_placeholder").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_avatar_remote_image", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `trips screen presents passenger bookings distinctly from driver trips and supports null tripSummary fallback and navigation`() {
        val driverSummary = DriverSummary(
            id = "driver-uid-100",
            fullName = "John Kamau",
            avatarUrl = "https://project.supabase.co/storage/v1/object/public/avatars/driver-uid-100/avatar.jpg",
            ratingAvg = 4.8,
            ratingCount = 15,
            completedTripsCount = 22
        )

        val driverOwnTrip = Trip(
            id = "trip-driver-hosted-1",
            driverId = "user-dual-role-1",
            origin = "Eldoret",
            destination = "Kisumu",
            departureDate = "2026-11-05",
            departureTime = "09:30:00",
            totalSeats = 4,
            availableSeats = 3,
            contributionAmount = 600.0,
            tripStatus = "published"
        )

        val bookedUpcomingTrip = Trip(
            id = "trip-booked-upcoming-1",
            driverId = driverSummary.id,
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-12",
            departureTime = "08:00:00",
            totalSeats = 3,
            availableSeats = 0,
            contributionAmount = 800.0,
            tripStatus = "full",
            driverSummary = driverSummary
        )

        val confirmedPassengerBooking = Booking(
            id = "booking-upcoming-1",
            tripId = bookedUpcomingTrip.id,
            passengerId = "user-dual-role-1",
            driverId = driverSummary.id,
            seatsBooked = 2,
            status = "confirmed",
            bookingReference = "WAPI-12345678",
            pickupStopName = "Westlands",
            dropoffStopName = "Nakuru CBD",
            tripSummary = bookedUpcomingTrip
        )

        val confirmedNullSummaryBooking = Booking(
            id = "booking-null-summary-2",
            tripId = "trip-unhydrated-999",
            passengerId = "user-dual-role-1",
            driverId = driverSummary.id,
            seatsBooked = 1,
            status = "confirmed",
            bookingReference = "WAPI-NULLTRIP",
            tripSummary = null
        )

        val activeTrip = bookedUpcomingTrip.copy(
            id = "trip-booked-active-1",
            tripStatus = "in_progress"
        )
        val activePassengerBooking = Booking(
            id = "booking-active-1",
            tripId = activeTrip.id,
            passengerId = "user-dual-role-1",
            driverId = driverSummary.id,
            seatsBooked = 1,
            status = "in_progress",
            bookingReference = "WAPI-ACTIVE01",
            tripSummary = activeTrip
        )

        val completedTrip = bookedUpcomingTrip.copy(
            id = "trip-booked-completed-1",
            tripStatus = "completed"
        )
        val completedPassengerBooking = Booking(
            id = "booking-completed-1",
            tripId = completedTrip.id,
            passengerId = "user-dual-role-1",
            driverId = driverSummary.id,
            seatsBooked = 1,
            status = "completed",
            bookingReference = "WAPI-COMP01",
            tripSummary = completedTrip
        )

        val cancelledPassengerBooking = Booking(
            id = "booking-cancelled-1",
            tripId = bookedUpcomingTrip.id,
            passengerId = "user-dual-role-1",
            driverId = driverSummary.id,
            seatsBooked = 1,
            status = "cancelled",
            bookingReference = "WAPI-CANC01",
            tripSummary = bookedUpcomingTrip
        )

        val incomingDriverRequest = TripRequest(
            id = "req-driver-1",
            tripId = driverOwnTrip.id,
            passengerId = "other-passenger-99",
            requestedSeats = 1,
            status = "pending",
            passengerSummary = DriverSummary(id = "other-passenger-99", fullName = "Grace Wanjiku"),
            tripSummary = driverOwnTrip
        )

        var selectedTabIndex by androidx.compose.runtime.mutableStateOf(0)
        var clickedDriverTripId: String? = null
        var clickedBookingTripId: String? = null

        composeTestRule.setContent {
            WapiRidesTheme {
                com.example.ui.screens.TripsScreen(
                    trips = listOf(driverOwnTrip),
                    passengerBookings = listOf(
                        confirmedPassengerBooking,
                        confirmedNullSummaryBooking,
                        activePassengerBooking,
                        completedPassengerBooking,
                        cancelledPassengerBooking
                    ),
                    driverRequestsState = com.example.ui.viewmodel.DriverRequestsState(
                        status = com.example.ui.viewmodel.DriverRequestsStatus.RESULTS,
                        requests = listOf(incomingDriverRequest)
                    ),
                    selectedTabIndex = selectedTabIndex,
                    onSelectTabIndex = { selectedTabIndex = it },
                    onTripClick = { trip -> clickedDriverTripId = trip.id },
                    onBookingTripClick = { booking -> clickedBookingTripId = booking.tripId }
                )
            }
        }
        composeTestRule.waitForIdle()

        // 7. Driver trips continue appearing correctly in Upcoming (index 0) with "DRIVING" role badge and "PUBLISHED" status
        composeTestRule.onNodeWithTag("trip_item_upcoming_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_item_upcoming_0_role_badge", useUnmergedTree = true).assertTextEquals("DRIVING")
        composeTestRule.onNodeWithTag("trip_item_upcoming_0_status_badge", useUnmergedTree = true).assertTextEquals("PUBLISHED")

        // 1 & 2. Confirmed passenger booking appears in Upcoming (index 1) and shows "CONFIRMED SEAT", NOT "PUBLISHED" or "FULL"
        composeTestRule.onNodeWithTag("trip_item_upcoming_1").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_item_upcoming_1_status_badge", useUnmergedTree = true).assertTextEquals("CONFIRMED SEAT")
        composeTestRule.onNodeWithText("FULL").assertDoesNotExist()

        // 3 & 4. Passenger booking displays seats booked ("2 seats booked"), contribution ("KES 1600"), driver info, pickup/dropoff, and booking reference ("Ref: WAPI-12345678")
        composeTestRule.onNodeWithTag("trip_item_upcoming_1_seats_booked", useUnmergedTree = true).assertTextEquals("2 seats booked")
        composeTestRule.onNodeWithTag("trip_item_upcoming_1_contribution", useUnmergedTree = true).assertTextEquals("KES 1600")
        composeTestRule.onNodeWithTag("trip_item_upcoming_1_driver_name", useUnmergedTree = true).assertTextEquals("John Kamau")
        composeTestRule.onNodeWithTag("trip_item_upcoming_1_pickup_stop", useUnmergedTree = true).assertTextEquals("Pickup: Westlands")
        composeTestRule.onNodeWithTag("trip_item_upcoming_1_dropoff_stop", useUnmergedTree = true).assertTextEquals("Drop-off: Nakuru CBD")
        composeTestRule.onNodeWithTag("trip_item_upcoming_1_booking_ref", useUnmergedTree = true).assertTextEquals("Ref: WAPI-12345678")

        // 5 & 6. Passenger booking remains visible when tripSummary == null (index 2) and navigates using booking.tripId
        composeTestRule.onNodeWithTag("trip_item_upcoming_2").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_item_upcoming_2_status_badge", useUnmergedTree = true).assertTextEquals("CONFIRMED SEAT")
        composeTestRule.onNodeWithTag("trip_item_upcoming_2_fallback_title", useUnmergedTree = true).assertTextEquals("Confirmed booking")
        composeTestRule.onNodeWithTag("trip_item_upcoming_2_seats_booked", useUnmergedTree = true).assertTextEquals("1 seat booked")
        composeTestRule.onNodeWithTag("trip_item_upcoming_2_booking_ref", useUnmergedTree = true).assertTextEquals("Ref: WAPI-NULLTRIP")
        composeTestRule.onNodeWithTag("trip_item_upcoming_2").performScrollTo().performClick()
        assertEquals("trip-unhydrated-999", clickedBookingTripId)

        // 11. Existing driver Requests tab (tab 1) behavior remains unchanged
        composeTestRule.onNodeWithTag("trips_tab_1").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("driver_request_item_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("driver_request_item_0_passenger_name", useUnmergedTree = true).assertTextEquals("Grace Wanjiku")

        // 8. Active passenger booking appears under Active (tab 2) with "IN PROGRESS"
        composeTestRule.onNodeWithTag("trips_tab_2").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("trip_item_active_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_item_active_0_status_badge", useUnmergedTree = true).assertTextEquals("IN PROGRESS")
        composeTestRule.onNodeWithTag("trip_item_active_0_booking_ref", useUnmergedTree = true).assertTextEquals("Ref: WAPI-ACTIVE01")

        // 9. Completed passenger booking appears under Completed (tab 3) with "COMPLETED"
        composeTestRule.onNodeWithTag("trips_tab_3").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("trip_item_completed_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_item_completed_0_status_badge", useUnmergedTree = true).assertTextEquals("COMPLETED")
        composeTestRule.onNodeWithTag("trip_item_completed_0_booking_ref", useUnmergedTree = true).assertTextEquals("Ref: WAPI-COMP01")

        // 10. Cancelled passenger booking appears under Cancelled (tab 4) with "BOOKING CANCELLED" (NOT "FULL" or "PUBLISHED")
        composeTestRule.onNodeWithTag("trips_tab_4").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("trip_item_cancelled_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trip_item_cancelled_0_status_badge", useUnmergedTree = true).assertTextEquals("BOOKING CANCELLED")
        composeTestRule.onNodeWithTag("trip_item_cancelled_0_booking_ref", useUnmergedTree = true).assertTextEquals("Ref: WAPI-CANC01")
    }

    @Test
    fun `phase 4_7b messaging schema serialization aligns with live public messages columns`() {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        // Test 1: Decode unread row from live public.messages schema
        val unreadRowJson = """
            {
              "id": "00000000-0000-0000-0000-000000000001",
              "conversation_id": "00000000-0000-0000-0000-000000000002",
              "sender_id": "00000000-0000-0000-0000-000000000003",
              "message": "Hello from live schema",
              "read_at": null,
              "created_at": "2026-10-08T10:00:00Z"
            }
        """.trimIndent()
        val decodedUnread = json.decodeFromString<WapiMessage>(unreadRowJson)
        assertEquals("Hello from live schema", decodedUnread.message)
        assertEquals("Hello from live schema", decodedUnread.textContent)
        assertFalse(decodedUnread.isEffectivelyRead)

        // Test 2: Decode read row where read_at is populated
        val readRowJson = """
            {
              "id": "00000000-0000-0000-0000-000000000001",
              "conversation_id": "00000000-0000-0000-0000-000000000002",
              "sender_id": "00000000-0000-0000-0000-000000000003",
              "message": "Hello from live schema",
              "read_at": "2026-10-08T10:05:00Z",
              "created_at": "2026-10-08T10:00:00Z"
            }
        """.trimIndent()
        val decodedRead = json.decodeFromString<WapiMessage>(readRowJson)
        assertEquals("2026-10-08T10:05:00Z", decodedRead.readAt)
        assertTrue(decodedRead.isEffectivelyRead)

        // Test 3: Serialize WapiMessageInsertPayload and verify only live columns are emitted
        val insertPayload = WapiMessageInsertPayload(
            conversationId = "00000000-0000-0000-0000-000000000002",
            senderId = "00000000-0000-0000-0000-000000000003",
            message = "See you at Westlands stage"
        )
        val insertJsonString = json.encodeToString(WapiMessageInsertPayload.serializer(), insertPayload)
        val insertJsonObj = json.parseToJsonElement(insertJsonString) as kotlinx.serialization.json.JsonObject
        assertTrue(insertJsonObj.containsKey("conversation_id"))
        assertTrue(insertJsonObj.containsKey("sender_id"))
        assertTrue(insertJsonObj.containsKey("message"))
        assertFalse(insertJsonObj.containsKey("content"))
        assertFalse(insertJsonObj.containsKey("body"))
        assertFalse(insertJsonObj.containsKey("is_read"))

        // Test 4: Serialize WapiMessageMarkReadPayload and verify only read_at is emitted
        val markReadPayload = WapiMessageMarkReadPayload(
            readAt = "2026-10-08T10:05:00Z"
        )
        val markReadJsonString = json.encodeToString(WapiMessageMarkReadPayload.serializer(), markReadPayload)
        val markReadJsonObj = json.parseToJsonElement(markReadJsonString) as kotlinx.serialization.json.JsonObject
        assertTrue(markReadJsonObj.containsKey("read_at"))
        assertFalse(markReadJsonObj.containsKey("is_read"))
    }
}
