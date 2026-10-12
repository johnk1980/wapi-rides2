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
 * Payload sent when inserting a new trip into `public.trips`.
 * `id`, `created_at`, and `updated_at` are omitted so PostgreSQL defaults generate them.
 */
@Serializable
data class TripInsertRequest(
    @SerialName("driver_id")
    val driverId: String,
    @SerialName("vehicle_id")
    val vehicleId: String? = null,
    @SerialName("origin")
    val origin: String,
    @SerialName("destination")
    val destination: String,
    @SerialName("departure_date")
    val departureDate: String, // PostgreSQL date: YYYY-MM-DD
    @SerialName("departure_time")
    val departureTime: String, // PostgreSQL time: HH:MM:SS
    @SerialName("total_seats")
    val totalSeats: Int,
    @SerialName("available_seats")
    val availableSeats: Int,
    @SerialName("contribution_amount")
    val contributionAmount: Double? = null,
    @SerialName("trip_status")
    val tripStatus: String = "published",
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Public marketplace profile summary hydrated from `public.profiles`.
 * Never exposes email, phone, or internal authentication IDs in public marketplace views.
 */
@Serializable
data class DriverSummary(
    @SerialName("id")
    val id: String = "",
    @SerialName("full_name")
    val fullName: String = "",
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("rating_avg")
    val ratingAvg: Double? = null,
    @SerialName("rating_average")
    val ratingAverage: Double? = null,
    @SerialName("rating_count")
    val ratingCount: Int? = null,
    @SerialName("completed_trips_count")
    val completedTripsCount: Int? = null,
    @SerialName("completed_trips")
    val completedTrips: Int? = null
) {
    val displayName: String
        get() = fullName.trim().ifBlank { "Wapi Member" }

    val cleanBio: String?
        get() = bio?.trim()?.takeIf { it.isNotEmpty() }

    val initials: String
        get() {
            val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "WM"
            }
        }

    val effectiveRating: Double?
        get() = ratingAverage ?: ratingAvg

    val effectiveCompletedTrips: Int
        get() = completedTrips ?: completedTripsCount ?: 0

    val formattedRating: String
        get() {
            val rating = effectiveRating
            return if (rating != null && rating > 0.0) {
                val base = com.example.platform.formatOneDecimal(rating)
                if (ratingCount != null && ratingCount > 0) "$base ($ratingCount)" else base
            } else {
                "New member"
            }
        }

    val formattedCompletedTrips: String
        get() = when (val count = effectiveCompletedTrips) {
            1 -> "1 trip"
            else -> "$count trips"
        }
}

/**
 * Represents a row from `public.trip_stops`.
 */
@Serializable
data class TripStop(
    @SerialName("id")
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String = "",
    @SerialName("stop_type")
    val stopType: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("latitude")
    val latitude: Double? = null,
    @SerialName("longitude")
    val longitude: Double? = null,
    @SerialName("sequence")
    val sequence: Int = 0
) {
    val isPickupCandidate: Boolean
        get() {
            val t = stopType.lowercase().trim()
            return t.isEmpty() || t == "pickup" || t == "both" || t == "waypoint" || t == "stop"
        }

    val isDropoffCandidate: Boolean
        get() {
            val t = stopType.lowercase().trim()
            return t.isEmpty() || t == "dropoff" || t == "drop_off" || t == "drop-off" || t == "both" || t == "waypoint" || t == "stop"
        }
}

/**
 * Payload sent when inserting a new row into `public.trip_requests`.
 * Passenger only sets `status = "pending"`.
 */
@Serializable
data class TripRequestInsertPayload(
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("passenger_id")
    val passengerId: String,
    @SerialName("requested_seats")
    val requestedSeats: Int,
    @SerialName("pickup_stop_id")
    val pickupStopId: String? = null,
    @SerialName("dropoff_stop_id")
    val dropoffStopId: String? = null,
    @SerialName("status")
    val status: String = "pending",
    @SerialName("message")
    val message: String? = null
)

/**
 * Focused payload for updating only the `status` column of `public.trip_requests`
 * (used for passenger cancellation of pending requests or driver decline).
 */
@Serializable
data class TripRequestStatusUpdatePayload(
    @SerialName("status")
    val status: String
)

/**
 * Represents a row retrieved from `public.trip_requests`.
 */
@Serializable
data class TripRequest(
    @SerialName("id")
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String = "",
    @SerialName("passenger_id")
    val passengerId: String = "",
    @SerialName("requested_seats")
    val requestedSeats: Int = 1,
    @SerialName("pickup_stop_id")
    val pickupStopId: String? = null,
    @SerialName("dropoff_stop_id")
    val dropoffStopId: String? = null,
    @SerialName("status")
    val status: String = "pending",
    @SerialName("message")
    val message: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    // Client-side hydrated fields (ignored when absent from Postgrest JSON)
    val passengerSummary: DriverSummary? = null,
    val tripSummary: Trip? = null,
    val pickupStopName: String? = null,
    val dropoffStopName: String? = null,
    val booking: Booking? = null
) {
    val resolvedPassengerSummary: DriverSummary
        get() = passengerSummary ?: DriverSummary(id = passengerId, fullName = "Wapi Passenger")

    val formattedCreatedAt: String
        get() {
            val raw = createdAt?.trim().orEmpty()
            if (raw.length >= 16) {
                return raw.substring(0, 10) + " · " + raw.substring(11, 16)
            }
            return if (raw.isNotEmpty()) raw.take(10) else "Just now"
        }
}

/**
 * Represents a row retrieved from `public.ratings`.
 * Created exclusively via PostgreSQL `public.submit_rating(p_booking_id, p_rating, p_review)`.
 */
@Serializable
data class Rating(
    @SerialName("id")
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String = "",
    @SerialName("booking_id")
    val bookingId: String = "",
    @SerialName("reviewer_id")
    val reviewerId: String = "",
    @SerialName("reviewee_id")
    val revieweeId: String = "",
    @SerialName("rating")
    val rating: Int = 5,
    @SerialName("review")
    val review: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) {
    val starsDisplay: String
        get() {
            val clamped = rating.coerceIn(1, 5)
            return "★".repeat(clamped) + "☆".repeat(5 - clamped)
        }
}

/**
 * RPC parameters sent to `public.submit_rating(p_booking_id, p_rating, p_review)`.
 * Never includes reviewer_id, reviewee_id, or trip_id.
 */
@Serializable
data class RatingSubmissionRpcParams(
    @SerialName("p_booking_id")
    val bookingId: String,
    @SerialName("p_rating")
    val rating: Int,
    @SerialName("p_review")
    val review: String? = null
)

/**
 * Represents a row retrieved from `public.bookings`.
 * Created exclusively by PostgreSQL `public.confirm_trip_request(p_trip_request_id)`
 * and transitioned exclusively by PostgreSQL RPCs (`start_booking`, `complete_booking`, `cancel_booking`).
 */
@Serializable
data class Booking(
    @SerialName("id")
    val id: String = "",
    @SerialName("trip_id")
    val tripId: String = "",
    @SerialName("passenger_id")
    val passengerId: String = "",
    @SerialName("driver_id")
    val driverId: String = "",
    @SerialName("trip_request_id")
    val tripRequestId: String? = null,
    @SerialName("seats_booked")
    val seatsBooked: Int = 1,
    @SerialName("pickup_stop_id")
    val pickupStopId: String? = null,
    @SerialName("dropoff_stop_id")
    val dropoffStopId: String? = null,
    @SerialName("status")
    val status: String = "confirmed",
    @SerialName("booking_reference")
    val bookingReference: String = "",
    @SerialName("confirmed_at")
    val confirmedAt: String? = null,
    @SerialName("cancelled_at")
    val cancelledAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    // Client-side hydrated fields
    val pickupStopName: String? = null,
    val dropoffStopName: String? = null,
    val passengerSummary: DriverSummary? = null,
    val tripSummary: Trip? = null,
    val existingRating: Rating? = null
) {
    val resolvedPassengerSummary: DriverSummary
        get() = passengerSummary ?: DriverSummary(id = passengerId, fullName = "Wapi Passenger")

    val isConfirmed: Boolean
        get() = status.equals("confirmed", ignoreCase = true)

    val isInProgress: Boolean
        get() = status.equals("in_progress", ignoreCase = true)

    val isCompleted: Boolean
        get() = status.equals("completed", ignoreCase = true)

    val isCancelled: Boolean
        get() = status.equals("cancelled", ignoreCase = true)

    val isRatedByCurrentUser: Boolean
        get() = existingRating != null
}

/**
 * Represents a row retrieved from `public.trips`.
 */
@Serializable
data class Trip(
    @SerialName("id")
    val id: String = "",
    @SerialName("driver_id")
    val driverId: String,
    @SerialName("vehicle_id")
    val vehicleId: String? = null,
    @SerialName("origin")
    val origin: String,
    @SerialName("destination")
    val destination: String,
    @SerialName("departure_date")
    val departureDate: String,
    @SerialName("departure_time")
    val departureTime: String,
    @SerialName("total_seats")
    val totalSeats: Int,
    @SerialName("available_seats")
    val availableSeats: Int,
    @SerialName("contribution_amount")
    val contributionAmount: Double? = null,
    @SerialName("trip_status")
    val tripStatus: String = "published",
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    // Client-side hydrated driver summary from public.profiles (ignored by Postgrest JSON when absent)
    val driverSummary: DriverSummary? = null,
    // Client-side hydrated vehicle summary from public.vehicles when permitted by RLS (ignored by Postgrest JSON when absent)
    val vehicleSummary: WapiVehicle? = null,
    // Client-side hydrated trip stops from public.trip_stops (ignored by Postgrest JSON when absent)
    val tripStops: List<TripStop> = emptyList(),
    // Client-side deterministic Wapi Match result (ignored by Postgrest JSON when absent)
    val wapiMatch: WapiMatchResult? = null
) {
    val formattedDepartureTimeShort: String
        get() = if (departureTime.length >= 5) departureTime.substring(0, 5) else departureTime

    val formattedContribution: String
        get() = if (contributionAmount != null) {
            if (contributionAmount % 1.0 == 0.0) {
                "KES ${contributionAmount.toLong()}"
            } else {
                "KES $contributionAmount"
            }
        } else {
            "Cost shared"
        }

    val isFullOrUnavailable: Boolean
        get() = availableSeats <= 0 || tripStatus.equals("full", ignoreCase = true)

    val isRequestableStatus: Boolean
        get() = tripStatus.equals("published", ignoreCase = true) && availableSeats > 0

    val resolvedDriverSummary: DriverSummary
        get() = driverSummary ?: DriverSummary(id = driverId, fullName = "Wapi Driver")
}

interface WapiTripRepository {
    suspend fun createTrip(
        origin: String,
        destination: String,
        departureDate: String,
        departureTime: String,
        seats: Int,
        contributionAmount: Double?,
        notes: String?,
        vehicleId: String? = null
    ): Result<Trip>

    suspend fun fetchMyDriverTrips(): Result<List<Trip>>

    suspend fun searchTrips(
        origin: String,
        destination: String,
        departureDate: String,
        preferredTime: String? = null
    ): Result<List<Trip>>

    suspend fun fetchTripById(tripId: String): Result<Trip>

    suspend fun getTripStops(tripId: String): Result<List<TripStop>>

    suspend fun getPassengerRequestForTrip(tripId: String): Result<TripRequest?>

    suspend fun createTripRequest(
        trip: Trip,
        requestedSeats: Int,
        pickupStopId: String?,
        dropoffStopId: String?,
        message: String?
    ): Result<TripRequest>

    suspend fun cancelTripRequest(requestId: String): Result<TripRequest>

    suspend fun getDriverTripRequests(): Result<List<TripRequest>>

    suspend fun acceptTripRequest(requestId: String): Result<Booking?>

    suspend fun declineTripRequest(requestId: String): Result<TripRequest>

    suspend fun getBookingForRequest(
        requestId: String,
        tripId: String? = null,
        passengerId: String? = null
    ): Result<Booking?>

    suspend fun getBookingsForTrip(tripId: String): Result<List<Booking>>

    suspend fun getMyPassengerBookings(): Result<List<Booking>>

    suspend fun getMyDriverCompletedBookings(): Result<List<Booking>> = Result.success(emptyList())

    suspend fun startBooking(bookingId: String): Result<Booking?>

    suspend fun completeBooking(bookingId: String): Result<Booking?>

    suspend fun cancelBooking(bookingId: String): Result<Booking?>

    suspend fun submitRating(
        bookingId: String,
        rating: Int,
        review: String?
    ): Result<Rating>

    suspend fun getRatingForBooking(bookingId: String): Result<Rating?>
}

class SupabaseWapiTripRepository : WapiTripRepository {

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
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Authentication required. Please sign in to publish a ride.")

        val driverId = currentUser.id.trim()
        if (driverId.isBlank()) {
            throw IllegalStateException("Authentication session is invalid. Please sign in again.")
        }

        val cleanVehicleId = vehicleId?.trim()?.takeIf { it.isNotEmpty() }
        // Verify that any selected vehicleId belongs to the authenticated user (`owner_id == driverId`)
        if (cleanVehicleId != null) {
            val ownedVehicles = runCatching {
                client.from("vehicles")
                    .select {
                        filter {
                            eq("id", cleanVehicleId)
                            eq("owner_id", driverId)
                        }
                        limit(1)
                    }
                    .decodeList<WapiVehicle>()
            }.getOrDefault(emptyList())

            if (ownedVehicles.isEmpty()) {
                throw IllegalArgumentException("Selected vehicle does not belong to your account or is no longer available.")
            }
        }

        val normalizedTime = normalizePostgresTime(departureTime)
        val cleanNotes = notes?.trim()?.takeIf { it.isNotEmpty() }

        val insertRequest = TripInsertRequest(
            driverId = driverId,
            vehicleId = cleanVehicleId,
            origin = origin.trim(),
            destination = destination.trim(),
            departureDate = departureDate.trim(),
            departureTime = normalizedTime,
            totalSeats = seats,
            availableSeats = seats,
            contributionAmount = contributionAmount,
            tripStatus = "published",
            notes = cleanNotes
        )

        val insertedRows = runCatching {
            client.from("trips")
                .insert(insertRequest) {
                    select()
                }
                .decodeList<Trip>()
        }.getOrElse {
            client.from("trips").insert(insertRequest)
            emptyList()
        }

        val baseTrip = insertedRows.firstOrNull() ?: Trip(
            id = "",
            driverId = driverId,
            vehicleId = cleanVehicleId,
            origin = insertRequest.origin,
            destination = insertRequest.destination,
            departureDate = insertRequest.departureDate,
            departureTime = insertRequest.departureTime,
            totalSeats = insertRequest.totalSeats,
            availableSeats = insertRequest.availableSeats,
            contributionAmount = insertRequest.contributionAmount,
            tripStatus = insertRequest.tripStatus,
            notes = insertRequest.notes
        )

        hydrateTripsWithDriverSummaries(listOf(baseTrip)).first()
    }

    override suspend fun fetchMyDriverTrips(): Result<List<Trip>> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user session found.")

        val rawTrips = client.from("trips")
            .select {
                filter {
                    eq("driver_id", currentUser.id)
                }
                order(column = "departure_date", order = Order.ASCENDING)
                order(column = "departure_time", order = Order.ASCENDING)
            }
            .decodeList<Trip>()

        hydrateTripsWithDriverSummaries(rawTrips)
    }

    override suspend fun searchTrips(
        origin: String,
        destination: String,
        departureDate: String,
        preferredTime: String?
    ): Result<List<Trip>> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val cleanOrigin = origin.trim()
        val cleanDestination = destination.trim()
        val cleanDate = departureDate.trim()
        val cleanPreferredTime = preferredTime?.trim()?.takeIf { it.isNotEmpty() }

        // Query all published/full trips on the requested travel date so stop-based pickup/drop-off
        // candidates on that date can also be evaluated deterministically by WapiMatchScorer.
        val rawDateTrips = runCatching {
            client.from("trips")
                .select {
                    filter {
                        eq("departure_date", cleanDate)
                        isIn("trip_status", listOf("published", "full"))
                    }
                    order(column = "departure_date", order = Order.ASCENDING)
                    order(column = "departure_time", order = Order.ASCENDING)
                }
                .decodeList<Trip>()
        }.getOrElse {
            client.from("trips")
                .select {
                    filter {
                        eq("departure_date", cleanDate)
                        ilike("origin", "%$cleanOrigin%")
                        ilike("destination", "%$cleanDestination%")
                        isIn("trip_status", listOf("published", "full"))
                    }
                    order(column = "departure_date", order = Order.ASCENDING)
                    order(column = "departure_time", order = Order.ASCENDING)
                }
                .decodeList<Trip>()
        }

        val filteredByStatus = rawDateTrips.filter { trip ->
            val status = trip.tripStatus.lowercase()
            status == "published" || status == "full"
        }

        if (filteredByStatus.isEmpty()) return@runCatching emptyList()

        // Hydrate trip stops in batch for these candidate trips
        val candidateTripIds = filteredByStatus.map { it.id.trim() }.filter { it.isNotEmpty() }.distinct()
        val stopsByTripId: Map<String, List<TripStop>> = if (candidateTripIds.isNotEmpty()) {
            runCatching {
                client.from("trip_stops")
                    .select {
                        filter {
                            isIn("trip_id", candidateTripIds)
                        }
                        order(column = "sequence", order = Order.ASCENDING)
                    }
                    .decodeList<TripStop>()
                    .groupBy { it.tripId }
                    .mapValues { (_, list) -> list.sortedBy { it.sequence } }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        // Hydrate driver profiles before scoring so trustScore has effectiveRating & completedTrips
        val hydratedTrips = hydrateTripsWithDriverSummaries(filteredByStatus).map { trip ->
            val stops = stopsByTripId[trip.id] ?: trip.tripStops
            trip.copy(tripStops = stops)
        }

        // Score and filter to rides that match both origin/pickup and destination/dropoff
        val compatibleTrips = hydratedTrips.filter { trip ->
            val match = WapiMatchScorer.scoreTrip(
                trip = trip,
                searchOrigin = cleanOrigin,
                searchDestination = cleanDestination,
                preferredTime = cleanPreferredTime,
                stops = trip.tripStops
            )
            match.breakdown.originScore > 0 && match.breakdown.destinationScore > 0
        }

        WapiMatchScorer.rankTrips(
            trips = compatibleTrips,
            searchOrigin = cleanOrigin,
            searchDestination = cleanDestination,
            preferredTime = cleanPreferredTime,
            stopsByTripId = stopsByTripId
        )
    }

    override suspend fun fetchTripById(tripId: String): Result<Trip> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val rows = client.from("trips")
            .select {
                filter {
                    eq("id", tripId.trim())
                }
                limit(1)
            }
            .decodeList<Trip>()

        val trip = rows.firstOrNull()
            ?: throw NoSuchElementException("Trip not found.")

        hydrateTripsWithDriverSummaries(listOf(trip)).first()
    }

    override suspend fun getTripStops(tripId: String): Result<List<TripStop>> = runCatching {
        val cleanTripId = tripId.trim()
        if (cleanTripId.isEmpty()) return@runCatching emptyList()
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        client.from("trip_stops")
            .select {
                filter {
                    eq("trip_id", cleanTripId)
                }
                order(column = "sequence", order = Order.ASCENDING)
            }
            .decodeList<TripStop>()
    }

    override suspend fun getPassengerRequestForTrip(tripId: String): Result<TripRequest?> = runCatching {
        val cleanTripId = tripId.trim()
        if (cleanTripId.isEmpty()) return@runCatching null
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: return@runCatching null

        val requests = client.from("trip_requests")
            .select {
                filter {
                    eq("trip_id", cleanTripId)
                    eq("passenger_id", currentUser.id)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<TripRequest>()

        val stops = runCatching { getTripStops(cleanTripId).getOrDefault(emptyList()) }.getOrDefault(emptyList())
        val stopsById = stops.associateBy { it.id }

        if (requests.isEmpty()) {
            // Also check if a direct booking exists for this passenger on this trip
            val directBooking = getBookingForRequest(
                requestId = "",
                tripId = cleanTripId,
                passengerId = currentUser.id
            ).getOrNull()?.let { booking ->
                val existingRating = if (booking.isCompleted) {
                    getRatingForBooking(booking.id).getOrNull()
                } else null
                booking.copy(
                    pickupStopName = booking.pickupStopId?.let { stopsById[it]?.name },
                    dropoffStopName = booking.dropoffStopId?.let { stopsById[it]?.name },
                    existingRating = existingRating
                )
            }
            if (directBooking != null) {
                return@runCatching TripRequest(
                    id = directBooking.tripRequestId.orEmpty(),
                    tripId = cleanTripId,
                    passengerId = currentUser.id,
                    requestedSeats = directBooking.seatsBooked,
                    pickupStopId = directBooking.pickupStopId,
                    dropoffStopId = directBooking.dropoffStopId,
                    status = "accepted",
                    pickupStopName = directBooking.pickupStopName,
                    dropoffStopName = directBooking.dropoffStopName,
                    booking = directBooking
                )
            }
            return@runCatching null
        }

        // Prioritize active requests (accepted or pending) if multiple historical rows exist
        val chosen = requests.firstOrNull {
            it.status.equals("accepted", ignoreCase = true) ||
                it.status.equals("pending", ignoreCase = true)
        } ?: requests.first()

        val relatedBooking = if (chosen.status.equals("accepted", ignoreCase = true)) {
            getBookingForRequest(
                requestId = chosen.id,
                tripId = cleanTripId,
                passengerId = currentUser.id
            ).getOrNull()?.let { booking ->
                val existingRating = if (booking.isCompleted) {
                    getRatingForBooking(booking.id).getOrNull()
                } else null
                booking.copy(
                    pickupStopName = booking.pickupStopId?.let { stopsById[it]?.name },
                    dropoffStopName = booking.dropoffStopId?.let { stopsById[it]?.name },
                    existingRating = existingRating
                )
            }
        } else {
            null
        }

        chosen.copy(
            pickupStopName = chosen.pickupStopId?.let { stopsById[it]?.name },
            dropoffStopName = chosen.dropoffStopId?.let { stopsById[it]?.name },
            booking = relatedBooking
        )
    }

    override suspend fun createTripRequest(
        trip: Trip,
        requestedSeats: Int,
        pickupStopId: String?,
        dropoffStopId: String?,
        message: String?
    ): Result<TripRequest> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to request a seat.")

        val passengerId = currentUser.id.trim()
        if (passengerId.isEmpty()) {
            throw IllegalStateException("Invalid user session. Please sign in again.")
        }

        val cleanTripId = trip.id.trim()
        if (cleanTripId.isEmpty()) {
            throw IllegalArgumentException("Trip does not exist.")
        }

        if (trip.driverId == passengerId) {
            throw IllegalArgumentException("You cannot request a seat on your own ride.")
        }

        // Refresh latest trip state from database to verify requestability & seat count
        val latestTrip = runCatching { fetchTripById(cleanTripId).getOrThrow() }.getOrDefault(trip)
        if (latestTrip.driverId == passengerId) {
            throw IllegalArgumentException("You cannot request a seat on your own ride.")
        }
        if (!latestTrip.tripStatus.equals("published", ignoreCase = true)) {
            throw IllegalStateException("This ride is no longer accepting seat requests.")
        }
        if (latestTrip.availableSeats <= 0) {
            throw IllegalStateException("Those seats are no longer available.")
        }
        if (requestedSeats < 1) {
            throw IllegalArgumentException("Requested seats must be at least 1.")
        }
        if (requestedSeats > latestTrip.availableSeats) {
            throw IllegalArgumentException("Requested seats cannot exceed available seats (${latestTrip.availableSeats}).")
        }

        val cleanMessage = message?.trim()?.takeIf { it.isNotEmpty() }
        if (cleanMessage != null && cleanMessage.length > 500) {
            throw IllegalArgumentException("Message must not exceed 500 characters.")
        }

        // Validate pickup/dropoff stops belong to this trip if provided
        val tripStops = getTripStops(cleanTripId).getOrDefault(emptyList())
        val validStopIds = tripStops.map { it.id }.toSet()
        val cleanPickupId = pickupStopId?.trim()?.takeIf { it.isNotEmpty() }
        val cleanDropoffId = dropoffStopId?.trim()?.takeIf { it.isNotEmpty() }

        if (cleanPickupId != null && !validStopIds.contains(cleanPickupId)) {
            throw IllegalArgumentException("Selected pickup stop does not belong to this trip.")
        }
        if (cleanDropoffId != null && !validStopIds.contains(cleanDropoffId)) {
            throw IllegalArgumentException("Selected drop-off stop does not belong to this trip.")
        }

        // Prevent duplicate active requests (pending or accepted with an active booking) for the same trip
        val existingRequest = getPassengerRequestForTrip(cleanTripId).getOrNull()
        if (existingRequest != null) {
            if (existingRequest.status.equals("accepted", ignoreCase = true)) {
                val existingBookingStatus = existingRequest.booking?.status?.lowercase()
                if (existingBookingStatus != "cancelled") {
                    return@runCatching existingRequest
                }
            }
            if (existingRequest.status.equals("pending", ignoreCase = true)) {
                return@runCatching existingRequest
            }
        }

        val insertPayload = TripRequestInsertPayload(
            tripId = cleanTripId,
            passengerId = passengerId,
            requestedSeats = requestedSeats,
            pickupStopId = cleanPickupId,
            dropoffStopId = cleanDropoffId,
            status = "pending",
            message = cleanMessage
        )

        val insertedRows = runCatching {
            client.from("trip_requests")
                .insert(insertPayload) {
                    select()
                }
                .decodeList<TripRequest>()
        }.getOrElse {
            client.from("trip_requests").insert(insertPayload)
            emptyList()
        }

        val stopsById = tripStops.associateBy { it.id }
        val created = insertedRows.firstOrNull()
            ?: getPassengerRequestForTrip(cleanTripId).getOrNull()
            ?: TripRequest(
                id = "",
                tripId = cleanTripId,
                passengerId = passengerId,
                requestedSeats = requestedSeats,
                pickupStopId = cleanPickupId,
                dropoffStopId = cleanDropoffId,
                status = "pending",
                message = cleanMessage
            )

        created.copy(
            pickupStopName = created.pickupStopId?.let { stopsById[it]?.name },
            dropoffStopName = created.dropoffStopId?.let { stopsById[it]?.name }
        )
    }

    override suspend fun cancelTripRequest(requestId: String): Result<TripRequest> = runCatching {
        val cleanRequestId = requestId.trim()
        if (cleanRequestId.isEmpty()) {
            throw IllegalArgumentException("Invalid trip request.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client
        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Please sign in to cancel your request.")

        // Only allow cancellation when status == pending and passenger_id == currentUser.id
        // Only send the intended status change ("cancelled")
        val statusUpdate = TripRequestStatusUpdatePayload(status = "cancelled")

        val updatedRows = runCatching {
            client.from("trip_requests")
                .update(statusUpdate) {
                    filter {
                        eq("id", cleanRequestId)
                        eq("passenger_id", currentUser.id)
                        eq("status", "pending")
                    }
                    select()
                }
                .decodeList<TripRequest>()
        }.getOrElse {
            client.from("trip_requests")
                .update(statusUpdate) {
                    filter {
                        eq("id", cleanRequestId)
                        eq("passenger_id", currentUser.id)
                        eq("status", "pending")
                    }
                }
            emptyList()
        }

        updatedRows.firstOrNull() ?: TripRequest(
            id = cleanRequestId,
            passengerId = currentUser.id,
            status = "cancelled"
        )
    }

    override suspend fun getDriverTripRequests(): Result<List<TripRequest>> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user session found.")

        val myTrips = fetchMyDriverTrips().getOrDefault(emptyList())
        val tripsById = myTrips.filter { it.id.isNotBlank() }.associateBy { it.id }
        if (tripsById.isEmpty()) {
            return@runCatching emptyList()
        }

        val myTripIds = tripsById.keys.toList()

        val rawRequests = client.from("trip_requests")
            .select {
                filter {
                    isIn("trip_id", myTripIds)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<TripRequest>()

        if (rawRequests.isEmpty()) return@runCatching emptyList()

        // Hydrate passenger profiles from public.profiles
        val passengerIds = rawRequests.map { it.passengerId.trim() }.filter { it.isNotEmpty() }.distinct()
        val passengerProfilesById: Map<String, DriverSummary> = if (passengerIds.isNotEmpty()) {
            runCatching {
                client.from("profiles")
                    .select {
                        filter {
                            isIn("id", passengerIds)
                        }
                    }
                    .decodeList<DriverSummary>()
                    .associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        // Hydrate stop names if any request references pickup_stop_id or dropoff_stop_id
        val referencedStopIds = rawRequests.flatMap {
            listOfNotNull(it.pickupStopId, it.dropoffStopId)
        }.filter { it.isNotBlank() }.distinct()

        val stopsById: Map<String, TripStop> = if (referencedStopIds.isNotEmpty()) {
            runCatching {
                client.from("trip_stops")
                    .select {
                        filter {
                            isIn("id", referencedStopIds)
                        }
                    }
                    .decodeList<TripStop>()
                    .associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        // Hydrate bookings for accepted requests so the driver sees the generated booking_reference and booking status
        val acceptedRequestIds = rawRequests
            .filter { it.status.equals("accepted", ignoreCase = true) && it.id.isNotBlank() }
            .map { it.id }

        val bookingsByRequestId: Map<String, Booking> = if (acceptedRequestIds.isNotEmpty()) {
            runCatching {
                client.from("bookings")
                    .select {
                        filter {
                            isIn("trip_request_id", acceptedRequestIds)
                        }
                    }
                    .decodeList<Booking>()
                    .filter { !it.tripRequestId.isNullOrBlank() }
                    .associateBy { it.tripRequestId!! }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        // Prioritize pending requests first, followed by accepted and declined
        rawRequests.map { req ->
            val pSummary = passengerProfilesById[req.passengerId]
                ?: DriverSummary(id = req.passengerId, fullName = "Wapi Passenger")
            val tSummary = tripsById[req.tripId]
            val pStopName = req.pickupStopId?.let { stopsById[it]?.name }
            val dStopName = req.dropoffStopId?.let { stopsById[it]?.name }
            val relatedBooking = bookingsByRequestId[req.id]?.copy(
                pickupStopName = pStopName,
                dropoffStopName = dStopName,
                passengerSummary = pSummary,
                tripSummary = tSummary
            )

            req.copy(
                passengerSummary = pSummary,
                tripSummary = tSummary,
                pickupStopName = pStopName,
                dropoffStopName = dStopName,
                booking = relatedBooking
            )
        }.sortedWith(
            compareBy<TripRequest> { req ->
                when (req.status.lowercase()) {
                    "pending" -> 0
                    "accepted" -> 1
                    "declined" -> 2
                    else -> 3
                }
            }.thenByDescending { it.createdAt.orEmpty() }
        )
    }

    override suspend fun acceptTripRequest(requestId: String): Result<Booking?> = runCatching {
        val cleanRequestId = requestId.trim()
        if (cleanRequestId.isEmpty()) {
            throw IllegalArgumentException("Invalid trip request.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        // Authoritative PostgreSQL RPC call: public.confirm_trip_request(p_trip_request_id)
        // Never insert into public.bookings or decrement available_seats from Android.
        val rpcParams = buildJsonObject {
            put("p_trip_request_id", JsonPrimitive(cleanRequestId))
        }

        val rpcResult = client.postgrest.rpc(
            function = "confirm_trip_request",
            parameters = rpcParams
        )

        val directBooking = runCatching {
            rpcResult.decodeSingleOrNull<Booking>()
        }.getOrNull() ?: runCatching {
            rpcResult.decodeList<Booking>().firstOrNull()
        }.getOrNull()

        directBooking ?: getBookingForRequest(requestId = cleanRequestId).getOrNull()
    }

    override suspend fun declineTripRequest(requestId: String): Result<TripRequest> = runCatching {
        val cleanRequestId = requestId.trim()
        if (cleanRequestId.isEmpty()) {
            throw IllegalArgumentException("Invalid trip request.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        // Update only status = "declined" for the selected pending request
        val statusUpdate = TripRequestStatusUpdatePayload(status = "declined")

        val updatedRows = runCatching {
            client.from("trip_requests")
                .update(statusUpdate) {
                    filter {
                        eq("id", cleanRequestId)
                    }
                    select()
                }
                .decodeList<TripRequest>()
        }.getOrElse {
            client.from("trip_requests")
                .update(statusUpdate) {
                    filter {
                        eq("id", cleanRequestId)
                    }
                }
            emptyList()
        }

        updatedRows.firstOrNull() ?: TripRequest(
            id = cleanRequestId,
            status = "declined"
        )
    }

    override suspend fun getBookingForRequest(
        requestId: String,
        tripId: String?,
        passengerId: String?
    ): Result<Booking?> = runCatching {
        val cleanRequestId = requestId.trim()
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        if (cleanRequestId.isNotEmpty()) {
            val byRequestId = runCatching {
                client.from("bookings")
                    .select {
                        filter {
                            eq("trip_request_id", cleanRequestId)
                        }
                        limit(1)
                    }
                    .decodeList<Booking>()
            }.getOrDefault(emptyList())

            if (byRequestId.isNotEmpty()) {
                return@runCatching byRequestId.first()
            }
        }

        if (!tripId.isNullOrBlank() && !passengerId.isNullOrBlank()) {
            val byTripAndPassenger = runCatching {
                client.from("bookings")
                    .select {
                        filter {
                            eq("trip_id", tripId.trim())
                            eq("passenger_id", passengerId.trim())
                        }
                        order(column = "created_at", order = Order.DESCENDING)
                        limit(1)
                    }
                    .decodeList<Booking>()
            }.getOrDefault(emptyList())

            return@runCatching byTripAndPassenger.firstOrNull()
        }

        null
    }

    override suspend fun getBookingsForTrip(tripId: String): Result<List<Booking>> = runCatching {
        val cleanTripId = tripId.trim()
        if (cleanTripId.isEmpty()) return@runCatching emptyList()
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val rawBookings = client.from("bookings")
            .select {
                filter {
                    eq("trip_id", cleanTripId)
                }
                order(column = "created_at", order = Order.ASCENDING)
            }
            .decodeList<Booking>()

        if (rawBookings.isEmpty()) return@runCatching emptyList()

        val passengerIds = rawBookings.map { it.passengerId.trim() }.filter { it.isNotEmpty() }.distinct()
        val passengerProfilesById: Map<String, DriverSummary> = if (passengerIds.isNotEmpty()) {
            runCatching {
                client.from("profiles")
                    .select {
                        filter {
                            isIn("id", passengerIds)
                        }
                    }
                    .decodeList<DriverSummary>()
                    .associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        val stops = runCatching { getTripStops(cleanTripId).getOrDefault(emptyList()) }.getOrDefault(emptyList())
        val stopsById = stops.associateBy { it.id }

        val currentUserId = client.auth.currentUserOrNull()?.id.orEmpty()
        val completedBookingIds = rawBookings.filter { it.isCompleted && it.id.isNotBlank() }.map { it.id }
        val ratingsByBookingId: Map<String, Rating> = if (currentUserId.isNotEmpty() && completedBookingIds.isNotEmpty()) {
            fetchCurrentUserRatingsByBookingIds(currentUserId, completedBookingIds)
        } else {
            emptyMap()
        }

        rawBookings.map { booking ->
            booking.copy(
                passengerSummary = passengerProfilesById[booking.passengerId]
                    ?: DriverSummary(id = booking.passengerId, fullName = "Wapi Passenger"),
                pickupStopName = booking.pickupStopId?.let { stopsById[it]?.name },
                dropoffStopName = booking.dropoffStopId?.let { stopsById[it]?.name },
                existingRating = ratingsByBookingId[booking.id]
            )
        }
    }

    override suspend fun getMyPassengerBookings(): Result<List<Booking>> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user session found.")

        val rawBookings = client.from("bookings")
            .select {
                filter {
                    eq("passenger_id", currentUser.id)
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<Booking>()

        if (rawBookings.isEmpty()) return@runCatching emptyList()

        val tripIds = rawBookings.map { it.tripId.trim() }.filter { it.isNotEmpty() }.distinct()
        val batchTripsById: Map<String, Trip> = if (tripIds.isNotEmpty()) {
            runCatching {
                val trips = client.from("trips")
                    .select {
                        filter {
                            isIn("id", tripIds)
                        }
                    }
                    .decodeList<Trip>()
                hydrateTripsWithDriverSummaries(trips).associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        val tripsById: MutableMap<String, Trip> = batchTripsById.toMutableMap()
        for (tripId in tripIds) {
            if (!tripsById.containsKey(tripId)) {
                fetchTripById(tripId).getOrNull()?.let { fetchedTrip ->
                    tripsById[tripId] = fetchedTrip
                }
            }
        }

        val stopIds = rawBookings.flatMap { listOfNotNull(it.pickupStopId?.trim(), it.dropoffStopId?.trim()) }
            .filter { it.isNotEmpty() }
            .distinct()
        val stopsById: Map<String, TripStop> = if (stopIds.isNotEmpty()) {
            runCatching {
                client.from("trip_stops")
                    .select {
                        filter {
                            isIn("id", stopIds)
                        }
                    }
                    .decodeList<TripStop>()
                    .associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        val completedBookingIds = rawBookings.filter { it.isCompleted && it.id.isNotBlank() }.map { it.id }
        val ratingsByBookingId: Map<String, Rating> = if (completedBookingIds.isNotEmpty()) {
            fetchCurrentUserRatingsByBookingIds(currentUser.id, completedBookingIds)
        } else {
            emptyMap()
        }

        rawBookings.map { booking ->
            val trip = tripsById[booking.tripId]
            val tripStopsById = trip?.tripStops?.associateBy { it.id }.orEmpty()
            val resolvedPickupName = booking.pickupStopName
                ?: booking.pickupStopId?.let { stopId ->
                    stopsById[stopId]?.name ?: tripStopsById[stopId]?.name
                }
            val resolvedDropoffName = booking.dropoffStopName
                ?: booking.dropoffStopId?.let { stopId ->
                    stopsById[stopId]?.name ?: tripStopsById[stopId]?.name
                }
            booking.copy(
                tripSummary = trip,
                pickupStopName = resolvedPickupName,
                dropoffStopName = resolvedDropoffName,
                existingRating = ratingsByBookingId[booking.id]
            )
        }
    }

    override suspend fun getMyDriverCompletedBookings(): Result<List<Booking>> = runCatching {
        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("No authenticated user session found.")

        val rawBookings = client.from("bookings")
            .select {
                filter {
                    eq("driver_id", currentUser.id)
                    eq("status", "completed")
                }
                order(column = "created_at", order = Order.DESCENDING)
            }
            .decodeList<Booking>()

        if (rawBookings.isEmpty()) return@runCatching emptyList()

        val tripIds = rawBookings.map { it.tripId.trim() }.filter { it.isNotEmpty() }.distinct()
        val tripsById: Map<String, Trip> = if (tripIds.isNotEmpty()) {
            runCatching {
                val trips = client.from("trips")
                    .select {
                        filter {
                            isIn("id", tripIds)
                        }
                    }
                    .decodeList<Trip>()
                hydrateTripsWithDriverSummaries(trips).associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        val passengerIds = rawBookings.map { it.passengerId.trim() }.filter { it.isNotEmpty() }.distinct()
        val passengerProfilesById: Map<String, DriverSummary> = if (passengerIds.isNotEmpty()) {
            runCatching {
                client.from("profiles")
                    .select {
                        filter {
                            isIn("id", passengerIds)
                        }
                    }
                    .decodeList<DriverSummary>()
                    .associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        val completedBookingIds = rawBookings.filter { it.id.isNotBlank() }.map { it.id }
        val ratingsByBookingId: Map<String, Rating> = if (completedBookingIds.isNotEmpty()) {
            fetchCurrentUserRatingsByBookingIds(currentUser.id, completedBookingIds)
        } else {
            emptyMap()
        }

        rawBookings.map { booking ->
            booking.copy(
                passengerSummary = passengerProfilesById[booking.passengerId]
                    ?: DriverSummary(id = booking.passengerId, fullName = "Wapi Passenger"),
                tripSummary = tripsById[booking.tripId],
                existingRating = ratingsByBookingId[booking.id]
            )
        }
    }

    override suspend fun startBooking(bookingId: String): Result<Booking?> = runCatching {
        val cleanBookingId = bookingId.trim()
        if (cleanBookingId.isEmpty()) {
            throw IllegalArgumentException("Booking could not be found.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        // Authoritative PostgreSQL RPC call: public.start_booking(p_booking_id)
        // Never update booking.status or trip.trip_status directly from Android.
        val rpcParams = buildJsonObject {
            put("p_booking_id", JsonPrimitive(cleanBookingId))
        }

        val rpcResult = client.postgrest.rpc(
            function = "start_booking",
            parameters = rpcParams
        )

        val directBooking = runCatching {
            rpcResult.decodeSingleOrNull<Booking>()
        }.getOrNull() ?: runCatching {
            rpcResult.decodeList<Booking>().firstOrNull()
        }.getOrNull()

        directBooking ?: fetchBookingById(cleanBookingId)
    }

    override suspend fun completeBooking(bookingId: String): Result<Booking?> = runCatching {
        val cleanBookingId = bookingId.trim()
        if (cleanBookingId.isEmpty()) {
            throw IllegalArgumentException("Booking could not be found.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        // Authoritative PostgreSQL RPC call: public.complete_booking(p_booking_id)
        // Never update booking.status, completed_at, or trip.trip_status directly from Android.
        val rpcParams = buildJsonObject {
            put("p_booking_id", JsonPrimitive(cleanBookingId))
        }

        val rpcResult = client.postgrest.rpc(
            function = "complete_booking",
            parameters = rpcParams
        )

        val directBooking = runCatching {
            rpcResult.decodeSingleOrNull<Booking>()
        }.getOrNull() ?: runCatching {
            rpcResult.decodeList<Booking>().firstOrNull()
        }.getOrNull()

        directBooking ?: fetchBookingById(cleanBookingId)
    }

    override suspend fun cancelBooking(bookingId: String): Result<Booking?> = runCatching {
        val cleanBookingId = bookingId.trim()
        if (cleanBookingId.isEmpty()) {
            throw IllegalArgumentException("Booking could not be found.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        // Authoritative PostgreSQL RPC call: public.cancel_booking(p_booking_id)
        // Never update booking.status, cancelled_at, or restore available_seats from Android.
        val rpcParams = buildJsonObject {
            put("p_booking_id", JsonPrimitive(cleanBookingId))
        }

        val rpcResult = client.postgrest.rpc(
            function = "cancel_booking",
            parameters = rpcParams
        )

        val directBooking = runCatching {
            rpcResult.decodeSingleOrNull<Booking>()
        }.getOrNull() ?: runCatching {
            rpcResult.decodeList<Booking>().firstOrNull()
        }.getOrNull()

        directBooking ?: fetchBookingById(cleanBookingId)
    }

    override suspend fun submitRating(
        bookingId: String,
        rating: Int,
        review: String?
    ): Result<Rating> = runCatching {
        val cleanBookingId = bookingId.trim()
        if (cleanBookingId.isEmpty()) {
            throw IllegalArgumentException("Booking not found.")
        }
        if (rating !in 1..5) {
            throw IllegalArgumentException("Rating must be between 1 and 5.")
        }

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: throw IllegalStateException("Authentication required.")
        if (currentUser.id.isBlank()) {
            throw IllegalStateException("Authentication required.")
        }

        val cleanReview = review?.trim()?.takeIf { it.isNotEmpty() }

        // Authoritative PostgreSQL RPC call: public.submit_rating(p_booking_id, p_rating, p_review)
        // Never insert directly into public.ratings or pass reviewer_id / reviewee_id / trip_id from Android.
        val params = RatingSubmissionRpcParams(
            bookingId = cleanBookingId,
            rating = rating,
            review = cleanReview
        )

        val rpcParams = buildJsonObject {
            put("p_booking_id", JsonPrimitive(params.bookingId))
            put("p_rating", JsonPrimitive(params.rating))
            if (params.review != null) {
                put("p_review", JsonPrimitive(params.review))
            }
        }

        val rpcResult = client.postgrest.rpc(
            function = "submit_rating",
            parameters = rpcParams
        )

        val decodedRating = runCatching {
            rpcResult.decodeSingleOrNull<Rating>()
        }.getOrNull() ?: runCatching {
            rpcResult.decodeList<Rating>().firstOrNull()
        }.getOrNull()

        decodedRating
            ?: getRatingForBooking(cleanBookingId).getOrNull()
            ?: Rating(
                bookingId = cleanBookingId,
                reviewerId = currentUser.id,
                rating = rating,
                review = cleanReview
            )
    }

    override suspend fun getRatingForBooking(bookingId: String): Result<Rating?> = runCatching {
        val cleanBookingId = bookingId.trim()
        if (cleanBookingId.isEmpty()) return@runCatching null

        SupabaseConnectivityClient.ensureConfigured()
        val client = SupabaseConnectivityClient.client

        val currentUser = client.auth.currentUserOrNull()
            ?: return@runCatching null

        client.from("ratings")
            .select {
                filter {
                    eq("booking_id", cleanBookingId)
                    eq("reviewer_id", currentUser.id)
                }
                limit(1)
            }
            .decodeList<Rating>()
            .firstOrNull()
    }

    private suspend fun fetchCurrentUserRatingsByBookingIds(
        reviewerId: String,
        bookingIds: List<String>
    ): Map<String, Rating> {
        if (reviewerId.isBlank() || bookingIds.isEmpty()) return emptyMap()
        val client = SupabaseConnectivityClient.client
        return runCatching {
            client.from("ratings")
                .select {
                    filter {
                        eq("reviewer_id", reviewerId)
                        isIn("booking_id", bookingIds)
                    }
                }
                .decodeList<Rating>()
                .associateBy { it.bookingId }
        }.getOrDefault(emptyMap())
    }

    private suspend fun fetchBookingById(bookingId: String): Booking? {
        val cleanId = bookingId.trim()
        if (cleanId.isEmpty()) return null
        val client = SupabaseConnectivityClient.client
        return runCatching {
            client.from("bookings")
                .select {
                    filter {
                        eq("id", cleanId)
                    }
                    limit(1)
                }
                .decodeList<Booking>()
                .firstOrNull()
        }.getOrNull()
    }

    private suspend fun hydrateTripsWithDriverSummaries(trips: List<Trip>): List<Trip> {
        if (trips.isEmpty()) return emptyList()
        val driverIds = trips.map { it.driverId.trim() }.filter { it.isNotEmpty() }.distinct()
        val vehicleIds = trips.mapNotNull { it.vehicleId?.trim()?.takeIf { id -> id.isNotEmpty() } }.distinct()

        val client = SupabaseConnectivityClient.client
        val profilesById: Map<String, DriverSummary> = if (driverIds.isNotEmpty()) {
            runCatching {
                client.from("profiles")
                    .select {
                        filter {
                            isIn("id", driverIds)
                        }
                    }
                    .decodeList<DriverSummary>()
                    .associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        val vehiclesById: Map<String, WapiVehicle> = if (vehicleIds.isNotEmpty()) {
            runCatching {
                client.from("vehicles")
                    .select {
                        filter {
                            isIn("id", vehicleIds)
                        }
                    }
                    .decodeList<WapiVehicle>()
                    .associateBy { it.id }
            }.getOrDefault(emptyMap())
        } else {
            emptyMap()
        }

        return trips.map { trip ->
            val summary = profilesById[trip.driverId] ?: DriverSummary(id = trip.driverId)
            val resolvedVehicle = trip.vehicleId?.let { vehiclesById[it] } ?: trip.vehicleSummary
            trip.copy(
                driverSummary = summary,
                vehicleSummary = resolvedVehicle
            )
        }
    }

    companion object {
        fun normalizePostgresTime(rawTime: String): String {
            val trimmed = rawTime.trim()
            return when {
                Regex("^\\d{2}:\\d{2}:\\d{2}$").matches(trimmed) -> trimmed
                Regex("^\\d{2}:\\d{2}$").matches(trimmed) -> "$trimmed:00"
                else -> trimmed
            }
        }

        fun parseTimeMinutes(timeStr: String): Int? {
            val parts = timeStr.trim().split(":")
            if (parts.size < 2) return null
            val hours = parts[0].toIntOrNull() ?: return null
            val minutes = parts[1].toIntOrNull() ?: return null
            return hours * 60 + minutes
        }
    }
}
