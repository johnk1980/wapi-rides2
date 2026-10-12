package com.example.data

import com.example.SupabaseConnectivityClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a row in `public.vehicles`.
 * Note:
 * - `owner_id` is strictly restricted by RLS (`auth.uid() = owner_id`).
 * - `verification_status` is read-only / backend-controlled and is never included in client INSERT/UPDATE payloads.
 */
@Serializable
data class WapiVehicle(
    @SerialName("id")
    val id: String = "",
    @SerialName("owner_id")
    val ownerId: String = "",
    @SerialName("make")
    val make: String = "",
    @SerialName("model")
    val model: String = "",
    @SerialName("year")
    val year: Int? = null,
    @SerialName("colour")
    val colour: String = "",
    @SerialName("color")
    val colorAlias: String? = null,
    @SerialName("registration")
    val registration: String = "",
    @SerialName("registration_number")
    val registrationNumberAlias: String? = null,
    @SerialName("plate_number")
    val plateNumberAlias: String? = null,
    @SerialName("seat_capacity")
    val seatCapacity: Int = 4,
    @SerialName("photo")
    val photo: String? = null,
    @SerialName("photo_url")
    val photoUrlAlias: String? = null,
    @SerialName("verification_status")
    val verificationStatus: String = "unverified",
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    val effectiveColour: String
        get() = colour.trim().ifEmpty { colorAlias?.trim().orEmpty() }

    val effectiveRegistration: String
        get() = registration.trim()
            .ifEmpty { registrationNumberAlias?.trim().orEmpty() }
            .ifEmpty { plateNumberAlias?.trim().orEmpty() }
            .uppercase()

    val effectivePhoto: String?
        get() = photo?.trim()?.takeIf { it.isNotEmpty() }
            ?: photoUrlAlias?.trim()?.takeIf { it.isNotEmpty() }

    val makeAndModel: String
        get() = listOf(make.trim(), model.trim()).filter { it.isNotEmpty() }.joinToString(" ").ifEmpty { "Vehicle" }

    val displayTitle: String
        get() = if (year != null && year > 1900) "$year $makeAndModel" else makeAndModel

    val displaySubtitle: String
        get() = listOf(effectiveColour, effectiveRegistration, "$seatCapacity seats")
            .filter { it.isNotBlank() }
            .joinToString(" · ")

    val formattedVerificationStatus: String
        get() = when (verificationStatus.trim().lowercase()) {
            "verified", "approved" -> "Verified"
            "pending", "under_review", "in_review" -> "Pending"
            "rejected", "declined" -> "Not verified"
            else -> "Not verified"
        }

    val isVerified: Boolean
        get() = verificationStatus.trim().lowercase() in setOf("verified", "approved")
}

/**
 * Focused payload for inserting a vehicle into `public.vehicles`.
 * Strictly sets `owner_id = auth.uid()` and NEVER sends `verification_status`.
 */
@Serializable
data class VehicleInsertPayload(
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("make")
    val make: String,
    @SerialName("model")
    val model: String,
    @SerialName("year")
    val year: Int? = null,
    @SerialName("colour")
    val colour: String,
    @SerialName("registration_number")
    val registration: String,
    @SerialName("seat_capacity")
    val seatCapacity: Int,
    @SerialName("photo_url")
    val photo: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

/**
 * Focused payload for updating a user's own vehicle in `public.vehicles`.
 * Never modifies `owner_id` or `verification_status`.
 */
@Serializable
data class VehicleUpdatePayload(
    @SerialName("make")
    val make: String,
    @SerialName("model")
    val model: String,
    @SerialName("year")
    val year: Int? = null,
    @SerialName("colour")
    val colour: String,
    @SerialName("registration_number")
    val registration: String,
    @SerialName("seat_capacity")
    val seatCapacity: Int,
    @SerialName("photo_url")
    val photo: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

/**
 * Read-only representation of a row in `public.verification_records`.
 * Ordinary users can only SELECT their own verification records (`user_id = auth.uid()`).
 * Client INSERT and UPDATE are strictly prohibited.
 */
@Serializable
data class WapiVerificationRecord(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("verification_type")
    val verificationType: String = "",
    @SerialName("type")
    val typeAlias: String? = null,
    @SerialName("status")
    val status: String = "",
    @SerialName("verification_status")
    val verificationStatusAlias: String? = null,
    @SerialName("verified_at")
    val verifiedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val effectiveType: String
        get() = verificationType.trim().ifEmpty { typeAlias?.trim().orEmpty() }.ifEmpty { "identity" }

    val effectiveStatus: String
        get() = status.trim().ifEmpty { verificationStatusAlias?.trim().orEmpty() }.lowercase()

    val formattedTypeLabel: String
        get() = when (effectiveType.lowercase()) {
            "id", "national_id", "identity", "government_id" -> "National ID"
            "phone", "phone_number" -> "Phone Number"
            "email", "email_address" -> "Email Address"
            "driver_license", "driving_licence", "license" -> "Driving Licence"
            else -> effectiveType.replace("_", " ").replaceFirstChar { it.uppercase() }
        }

    val formattedStatusLabel: String
        get() = when (effectiveStatus) {
            "verified", "approved" -> "Verified"
            "pending", "in_review", "under_review" -> "Pending"
            "rejected", "declined", "failed" -> "Not verified"
            else -> "Not verified"
        }

    val isVerified: Boolean
        get() = effectiveStatus in setOf("verified", "approved")

    val isPending: Boolean
        get() = effectiveStatus in setOf("pending", "in_review", "under_review")
}

/**
 * Overall verification status summary derived strictly from the authenticated user's
 * own `public.verification_records`.
 */
enum class OverallVerificationStatus(val badgeLabel: String) {
    VERIFIED("Verified"),
    PENDING("Pending"),
    NOT_VERIFIED("Not verified"),
    NO_RECORD("No verification record");

    companion object {
        fun fromRecords(records: List<WapiVerificationRecord>): OverallVerificationStatus {
            if (records.isEmpty()) return NO_RECORD
            return when {
                records.any { it.isVerified } -> VERIFIED
                records.any { it.isPending } -> PENDING
                else -> NOT_VERIFIED
            }
        }
    }
}

/**
 * Represents a row in `public.emergency_contacts`.
 * Strictly scoped to the authenticated user (`user_id = auth.uid()`).
 */
@Serializable
data class WapiEmergencyContact(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("contact_name")
    val contactNameAlias: String? = null,
    @SerialName("phone")
    val phone: String = "",
    @SerialName("phone_number")
    val phoneNumberAlias: String? = null,
    @SerialName("relationship")
    val relationship: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val effectiveName: String
        get() = name.trim().ifEmpty { contactNameAlias?.trim().orEmpty() }

    val effectivePhone: String
        get() = phone.trim().ifEmpty { phoneNumberAlias?.trim().orEmpty() }

    val effectiveRelationship: String?
        get() = relationship?.trim()?.takeIf { it.isNotEmpty() }
}

@Serializable
data class EmergencyContactInsertPayload(
    @SerialName("user_id")
    val userId: String,
    @SerialName("name")
    val name: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("relationship")
    val relationship: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean = false
)

@Serializable
data class EmergencyContactUpdatePayload(
    @SerialName("name")
    val name: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("relationship")
    val relationship: String? = null,
    @SerialName("is_primary")
    val isPrimary: Boolean = false
)

/**
 * Represents a row in `public.trip_shares`.
 * Used by a legitimate trip participant (driver or confirmed/in_progress passenger)
 * to record and manage trip shares without public tracking links or GPS location exposure.
 */
@Serializable
data class WapiTripShare(
    @SerialName("id")
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String = "",
    @SerialName("booking_id")
    val bookingId: String? = null,
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("shared_by_user_id")
    val sharedByUserIdAlias: String? = null,
    @SerialName("emergency_contact_id")
    val emergencyContactId: String? = null,
    @SerialName("recipient_name")
    val recipientName: String? = null,
    @SerialName("recipient_phone")
    val recipientPhone: String? = null,
    @SerialName("share_code")
    val shareCode: String? = null,
    @SerialName("share_token")
    val shareTokenAlias: String? = null,
    @SerialName("status")
    val status: String = "active",
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("revoked_at")
    val revokedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val effectiveOwnerUserId: String
        get() = userId.trim().ifEmpty { sharedByUserIdAlias?.trim().orEmpty() }

    val effectiveShareCode: String
        get() = shareCode?.trim()?.takeIf { it.isNotEmpty() }
            ?: shareTokenAlias?.trim()?.takeIf { it.isNotEmpty() }
            ?: id.take(8).uppercase()

    val isCurrentlyActive: Boolean
        get() = isActive &&
            revokedAt.isNullOrBlank() &&
            !status.equals("revoked", ignoreCase = true) &&
            !status.equals("cancelled", ignoreCase = true)

    val formattedRecipientSummary: String
        get() {
            val namePart = recipientName?.trim()?.takeIf { it.isNotEmpty() }
            val phonePart = recipientPhone?.trim()?.takeIf { it.isNotEmpty() }
            return when {
                namePart != null && phonePart != null -> "$namePart ($phonePart)"
                namePart != null -> namePart
                phonePart != null -> phonePart
                else -> "Trusted contact"
            }
        }
}

@Serializable
data class TripShareInsertPayload(
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("booking_id")
    val bookingId: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("emergency_contact_id")
    val emergencyContactId: String? = null,
    @SerialName("recipient_name")
    val recipientName: String? = null,
    @SerialName("recipient_phone")
    val recipientPhone: String? = null,
    @SerialName("status")
    val status: String = "active",
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class TripShareRevokePayload(
    @SerialName("status")
    val status: String = "revoked",
    @SerialName("is_active")
    val isActive: Boolean = false
)

interface WapiSafetyTrustRepository {
    // Vehicles
    suspend fun getMyVehicles(): Result<List<WapiVehicle>>
    suspend fun getVehicleById(vehicleId: String): Result<WapiVehicle?>
    suspend fun addVehicle(
        make: String,
        model: String,
        year: Int?,
        colour: String,
        registration: String,
        seatCapacity: Int,
        photo: String? = null,
        isActive: Boolean = true
    ): Result<WapiVehicle>

    suspend fun updateVehicle(
        vehicleId: String,
        make: String,
        model: String,
        year: Int?,
        colour: String,
        registration: String,
        seatCapacity: Int,
        photo: String? = null,
        isActive: Boolean = true
    ): Result<WapiVehicle>

    suspend fun deleteVehicle(vehicleId: String): Result<Unit>

    // Verification Records (Read-Only for current authenticated user)
    suspend fun getMyVerificationRecords(): Result<List<WapiVerificationRecord>>

    // Emergency Contacts
    suspend fun getMyEmergencyContacts(): Result<List<WapiEmergencyContact>>
    suspend fun addEmergencyContact(
        name: String,
        phone: String,
        relationship: String?,
        isPrimary: Boolean = false
    ): Result<WapiEmergencyContact>

    suspend fun updateEmergencyContact(
        contactId: String,
        name: String,
        phone: String,
        relationship: String?,
        isPrimary: Boolean = false
    ): Result<WapiEmergencyContact>

    suspend fun deleteEmergencyContact(contactId: String): Result<Unit>

    // Trip Shares
    suspend fun getMyTripSharesForTrip(tripId: String): Result<List<WapiTripShare>>
    suspend fun createTripShare(
        tripId: String,
        bookingId: String? = null,
        emergencyContact: WapiEmergencyContact? = null,
        recipientName: String? = null,
        recipientPhone: String? = null
    ): Result<WapiTripShare>

    suspend fun revokeTripShare(shareId: String): Result<Unit>
}

class SupabaseWapiSafetyTrustRepository : WapiSafetyTrustRepository {

    private fun requireAuthenticatedUserId(): String {
        SupabaseConnectivityClient.ensureConfigured()
        val user = SupabaseConnectivityClient.client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Authentication required. Please sign in to continue.")
        val uid = user.id.trim()
        if (uid.isEmpty()) {
            throw IllegalStateException("Authentication required. Please sign in to continue.")
        }
        return uid
    }

    // -------------------------------------------------------------------------
    // Vehicles (`public.vehicles`)
    // RLS: SELECT, INSERT, UPDATE, DELETE restricted to `auth.uid() = owner_id`
    // -------------------------------------------------------------------------

    override suspend fun getMyVehicles(): Result<List<WapiVehicle>> = runCatching {
        val uid = requireAuthenticatedUserId()
        val client = SupabaseConnectivityClient.client

        client.from("vehicles")
            .select {
                filter {
                    eq("owner_id", uid)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<WapiVehicle>()
    }

    override suspend fun getVehicleById(vehicleId: String): Result<WapiVehicle?> = runCatching {
        val cleanId = vehicleId.trim()
        if (cleanId.isEmpty()) return@runCatching null
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        // Relies on Supabase RLS (`auth.uid() = owner_id` or any permitted SELECT policy).
        // If RLS restricts access to non-owners, this safely returns null without throwing.
        runCatching {
            client.from("vehicles")
                .select {
                    filter {
                        eq("id", cleanId)
                    }
                    limit(1)
                }
                .decodeList<WapiVehicle>()
                .firstOrNull()
        }.getOrNull()
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
        val uid = requireAuthenticatedUserId()
        val client = SupabaseConnectivityClient.client

        val cleanMake = make.trim()
        val cleanModel = model.trim()
        val cleanColour = colour.trim()
        val cleanReg = registration.trim().uppercase()
        val cleanPhoto = photo?.trim()?.takeIf { it.isNotEmpty() }

        require(cleanMake.isNotEmpty()) { "Vehicle make is required." }
        require(cleanModel.isNotEmpty()) { "Vehicle model is required." }
        require(cleanColour.isNotEmpty()) { "Vehicle colour is required." }
        require(cleanReg.isNotEmpty()) { "Vehicle registration number is required." }
        require(seatCapacity in 1..14) { "Seat capacity must be between 1 and 14." }

        val payload = VehicleInsertPayload(
            ownerId = uid,
            make = cleanMake,
            model = cleanModel,
            year = year,
            colour = cleanColour,
            registration = cleanReg,
            seatCapacity = seatCapacity,
            photo = cleanPhoto,
            isActive = isActive
        )

        val inserted = runCatching {
            client.from("vehicles")
                .insert(payload) {
                    select()
                }
                .decodeList<WapiVehicle>()
        }.getOrElse {
            client.from("vehicles").insert(payload)
            emptyList()
        }

        inserted.firstOrNull()
            ?: getMyVehicles().getOrDefault(emptyList()).firstOrNull {
                it.effectiveRegistration.equals(cleanReg, ignoreCase = true)
            }
            ?: WapiVehicle(
                ownerId = uid,
                make = cleanMake,
                model = cleanModel,
                year = year,
                colour = cleanColour,
                registration = cleanReg,
                seatCapacity = seatCapacity,
                photo = cleanPhoto,
                verificationStatus = "unverified",
                isActive = isActive
            )
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
        val uid = requireAuthenticatedUserId()
        val cleanVehicleId = vehicleId.trim()
        require(cleanVehicleId.isNotEmpty()) { "Invalid vehicle ID." }

        val cleanMake = make.trim()
        val cleanModel = model.trim()
        val cleanColour = colour.trim()
        val cleanReg = registration.trim().uppercase()
        val cleanPhoto = photo?.trim()?.takeIf { it.isNotEmpty() }

        require(cleanMake.isNotEmpty()) { "Vehicle make is required." }
        require(cleanModel.isNotEmpty()) { "Vehicle model is required." }
        require(cleanColour.isNotEmpty()) { "Vehicle colour is required." }
        require(cleanReg.isNotEmpty()) { "Vehicle registration number is required." }
        require(seatCapacity in 1..14) { "Seat capacity must be between 1 and 14." }

        val client = SupabaseConnectivityClient.client
        // Never sends owner_id or verification_status in update payload
        val payload = VehicleUpdatePayload(
            make = cleanMake,
            model = cleanModel,
            year = year,
            colour = cleanColour,
            registration = cleanReg,
            seatCapacity = seatCapacity,
            photo = cleanPhoto,
            isActive = isActive
        )

        val updated = runCatching {
            client.from("vehicles")
                .update(payload) {
                    filter {
                        eq("id", cleanVehicleId)
                        eq("owner_id", uid)
                    }
                    select()
                }
                .decodeList<WapiVehicle>()
        }.getOrElse {
            client.from("vehicles")
                .update(payload) {
                    filter {
                        eq("id", cleanVehicleId)
                        eq("owner_id", uid)
                    }
                }
            emptyList()
        }

        updated.firstOrNull()
            ?: getVehicleById(cleanVehicleId).getOrNull()
            ?: WapiVehicle(
                id = cleanVehicleId,
                ownerId = uid,
                make = cleanMake,
                model = cleanModel,
                year = year,
                colour = cleanColour,
                registration = cleanReg,
                seatCapacity = seatCapacity,
                photo = cleanPhoto,
                isActive = isActive
            )
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> = runCatching {
        val uid = requireAuthenticatedUserId()
        val cleanVehicleId = vehicleId.trim()
        require(cleanVehicleId.isNotEmpty()) { "Invalid vehicle ID." }

        val client = SupabaseConnectivityClient.client
        client.from("vehicles")
            .delete {
                filter {
                    eq("id", cleanVehicleId)
                    eq("owner_id", uid)
                }
            }
    }

    // -------------------------------------------------------------------------
    // Verification Records (`public.verification_records`)
    // Read-only for the current authenticated user. No INSERT/UPDATE/DELETE.
    // -------------------------------------------------------------------------

    override suspend fun getMyVerificationRecords(): Result<List<WapiVerificationRecord>> = runCatching {
        val uid = requireAuthenticatedUserId()
        val client = SupabaseConnectivityClient.client

        client.from("verification_records")
            .select {
                filter {
                    eq("user_id", uid)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<WapiVerificationRecord>()
    }

    // -------------------------------------------------------------------------
    // Emergency Contacts (`public.emergency_contacts`)
    // Strictly scoped to `user_id = auth.uid()`.
    // -------------------------------------------------------------------------

    override suspend fun getMyEmergencyContacts(): Result<List<WapiEmergencyContact>> = runCatching {
        val uid = requireAuthenticatedUserId()
        val client = SupabaseConnectivityClient.client

        client.from("emergency_contacts")
            .select {
                filter {
                    eq("user_id", uid)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<WapiEmergencyContact>()
    }

    override suspend fun addEmergencyContact(
        name: String,
        phone: String,
        relationship: String?,
        isPrimary: Boolean
    ): Result<WapiEmergencyContact> = runCatching {
        val uid = requireAuthenticatedUserId()
        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        val cleanRel = relationship?.trim()?.takeIf { it.isNotEmpty() }

        require(cleanName.isNotEmpty()) { "Contact name is required." }
        require(cleanPhone.isNotEmpty()) { "Contact phone number is required." }

        val client = SupabaseConnectivityClient.client
        val payload = EmergencyContactInsertPayload(
            userId = uid,
            name = cleanName,
            phone = cleanPhone,
            relationship = cleanRel,
            isPrimary = isPrimary
        )

        val inserted = runCatching {
            client.from("emergency_contacts")
                .insert(payload) {
                    select()
                }
                .decodeList<WapiEmergencyContact>()
        }.getOrElse {
            client.from("emergency_contacts").insert(payload)
            emptyList()
        }

        inserted.firstOrNull()
            ?: getMyEmergencyContacts().getOrDefault(emptyList()).firstOrNull {
                it.effectiveName == cleanName && it.effectivePhone == cleanPhone
            }
            ?: WapiEmergencyContact(
                userId = uid,
                name = cleanName,
                phone = cleanPhone,
                relationship = cleanRel,
                isPrimary = isPrimary
            )
    }

    override suspend fun updateEmergencyContact(
        contactId: String,
        name: String,
        phone: String,
        relationship: String?,
        isPrimary: Boolean
    ): Result<WapiEmergencyContact> = runCatching {
        val uid = requireAuthenticatedUserId()
        val cleanId = contactId.trim()
        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        val cleanRel = relationship?.trim()?.takeIf { it.isNotEmpty() }

        require(cleanId.isNotEmpty()) { "Invalid contact ID." }
        require(cleanName.isNotEmpty()) { "Contact name is required." }
        require(cleanPhone.isNotEmpty()) { "Contact phone number is required." }

        val client = SupabaseConnectivityClient.client
        val payload = EmergencyContactUpdatePayload(
            name = cleanName,
            phone = cleanPhone,
            relationship = cleanRel,
            isPrimary = isPrimary
        )

        val updated = runCatching {
            client.from("emergency_contacts")
                .update(payload) {
                    filter {
                        eq("id", cleanId)
                        eq("user_id", uid)
                    }
                    select()
                }
                .decodeList<WapiEmergencyContact>()
        }.getOrElse {
            client.from("emergency_contacts")
                .update(payload) {
                    filter {
                        eq("id", cleanId)
                        eq("user_id", uid)
                    }
                }
            emptyList()
        }

        updated.firstOrNull()
            ?: WapiEmergencyContact(
                id = cleanId,
                userId = uid,
                name = cleanName,
                phone = cleanPhone,
                relationship = cleanRel,
                isPrimary = isPrimary
            )
    }

    override suspend fun deleteEmergencyContact(contactId: String): Result<Unit> = runCatching {
        val uid = requireAuthenticatedUserId()
        val cleanId = contactId.trim()
        require(cleanId.isNotEmpty()) { "Invalid contact ID." }

        val client = SupabaseConnectivityClient.client
        client.from("emergency_contacts")
            .delete {
                filter {
                    eq("id", cleanId)
                    eq("user_id", uid)
                }
            }
    }

    // -------------------------------------------------------------------------
    // Trip Shares (`public.trip_shares`)
    // Scoped to the authenticated participant (`user_id = auth.uid()`).
    // No public tracking links or GPS location data.
    // -------------------------------------------------------------------------

    override suspend fun getMyTripSharesForTrip(tripId: String): Result<List<WapiTripShare>> = runCatching {
        val uid = requireAuthenticatedUserId()
        val cleanTripId = tripId.trim()
        if (cleanTripId.isEmpty()) return@runCatching emptyList()

        val client = SupabaseConnectivityClient.client
        client.from("trip_shares")
            .select {
                filter {
                    eq("trip_id", cleanTripId)
                    eq("user_id", uid)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<WapiTripShare>()
            .filter { it.isCurrentlyActive }
    }

    override suspend fun createTripShare(
        tripId: String,
        bookingId: String?,
        emergencyContact: WapiEmergencyContact?,
        recipientName: String?,
        recipientPhone: String?
    ): Result<WapiTripShare> = runCatching {
        val uid = requireAuthenticatedUserId()
        val cleanTripId = tripId.trim()
        require(cleanTripId.isNotEmpty()) { "Trip ID is required to share a journey." }

        val cleanBookingId = bookingId?.trim()?.takeIf { it.isNotEmpty() }
        val cleanContactId = emergencyContact?.id?.trim()?.takeIf { it.isNotEmpty() }
        val resolvedName = emergencyContact?.effectiveName?.takeIf { it.isNotEmpty() }
            ?: recipientName?.trim()?.takeIf { it.isNotEmpty() }
        val resolvedPhone = emergencyContact?.effectivePhone?.takeIf { it.isNotEmpty() }
            ?: recipientPhone?.trim()?.takeIf { it.isNotEmpty() }

        val client = SupabaseConnectivityClient.client
        val payload = TripShareInsertPayload(
            tripId = cleanTripId,
            bookingId = cleanBookingId,
            userId = uid,
            emergencyContactId = cleanContactId,
            recipientName = resolvedName,
            recipientPhone = resolvedPhone,
            status = "active",
            isActive = true
        )

        val inserted = runCatching {
            client.from("trip_shares")
                .insert(payload) {
                    select()
                }
                .decodeList<WapiTripShare>()
        }.getOrElse {
            client.from("trip_shares").insert(payload)
            emptyList()
        }

        inserted.firstOrNull()
            ?: getMyTripSharesForTrip(cleanTripId).getOrDefault(emptyList()).firstOrNull()
            ?: WapiTripShare(
                tripId = cleanTripId,
                bookingId = cleanBookingId,
                userId = uid,
                emergencyContactId = cleanContactId,
                recipientName = resolvedName,
                recipientPhone = resolvedPhone,
                status = "active",
                isActive = true
            )
    }

    override suspend fun revokeTripShare(shareId: String): Result<Unit> = runCatching {
        val uid = requireAuthenticatedUserId()
        val cleanShareId = shareId.trim()
        require(cleanShareId.isNotEmpty()) { "Invalid trip share ID." }

        val client = SupabaseConnectivityClient.client
        val payload = TripShareRevokePayload(status = "revoked", isActive = false)
        runCatching {
            client.from("trip_shares")
                .update(payload) {
                    filter {
                        eq("id", cleanShareId)
                        eq("user_id", uid)
                    }
                }
        }.onFailure {
            client.from("trip_shares")
                .delete {
                    filter {
                        eq("id", cleanShareId)
                        eq("user_id", uid)
                    }
                }
        }
    }
}
