package com.example.data

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Breakdown of the 100-point deterministic Wapi Match score across each scoring dimension.
 */
@Serializable
data class WapiMatchBreakdown(
    val originScore: Int,
    val destinationScore: Int,
    val timeScore: Int,
    val seatScore: Int,
    val trustScore: Int,
    val matchedPickupStopName: String? = null,
    val matchedDropoffStopName: String? = null
) {
    val totalScore: Int
        get() = (originScore + destinationScore + timeScore + seatScore + trustScore).coerceIn(0, 100)
}

/**
 * Deterministic, explainable Wapi Match evaluation result for a Trip against a passenger's search.
 */
@Serializable
data class WapiMatchResult(
    val score: Int,
    val tier: String,
    val reasons: List<String>,
    val timeDifferenceMinutes: Int? = null,
    val breakdown: WapiMatchBreakdown
) {
    val formattedScoreBadge: String
        get() = "$score% Wapi Match"
}

/**
 * Pure, deterministic, locally evaluated Wapi Match scoring and ranking engine.
 * Calculates compatibility scores (0..100), human-readable tiers, explainable match reasons,
 * and deterministic ride ordering without external APIs or non-deterministic inputs.
 */
object WapiMatchScorer {

    const val TIER_EXCELLENT_MIN = 90
    const val TIER_GREAT_MIN = 75
    const val TIER_GOOD_MIN = 60
    const val TIER_POSSIBLE_MIN = 40

    const val TIER_EXCELLENT = "Excellent Match"
    const val TIER_GREAT = "Great Match"
    const val TIER_GOOD = "Good Match"
    const val TIER_POSSIBLE = "Possible Match"
    const val TIER_LOW = "Low Match"

    /**
     * Normalizes location text deterministically:
     * - trims leading/trailing whitespace
     * - collapses repeated internal whitespace to a single space
     * - lowercases using Locale.ROOT
     */
    fun normalizeLocationText(raw: String): String {
        return raw.trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
    }

    /**
     * Evaluates whether [candidateRaw] is a strong starts-with, word-boundary, or meaningful substring match
     * for [queryRaw] (after deterministic normalization), when not an exact match.
     */
    internal fun isStrongPartialMatch(queryNormalized: String, candidateNormalized: String): Boolean {
        if (queryNormalized.isEmpty() || candidateNormalized.isEmpty()) return false
        if (queryNormalized == candidateNormalized) return false
        return candidateNormalized.startsWith(queryNormalized) ||
            queryNormalized.startsWith(candidateNormalized) ||
            candidateNormalized.contains(queryNormalized) ||
            queryNormalized.contains(candidateNormalized)
    }

    /**
     * Evaluates how well a single [stopName] matches [queryNormalized].
     * Returns true if normalized stop name equals or strongly matches [queryNormalized].
     */
    internal fun matchesStopName(queryNormalized: String, stopName: String): Boolean {
        if (queryNormalized.isEmpty()) return false
        val normalizedStop = normalizeLocationText(stopName)
        if (normalizedStop.isEmpty()) return false
        return normalizedStop == queryNormalized || isStrongPartialMatch(queryNormalized, normalizedStop)
    }

    /**
     * Scores a single [trip] (and its ordered [stops], if any) against the passenger's search criteria.
     */
    fun scoreTrip(
        trip: Trip,
        searchOrigin: String,
        searchDestination: String,
        preferredTime: String? = null,
        stops: List<TripStop> = trip.tripStops
    ): WapiMatchResult {
        val normSearchOrigin = normalizeLocationText(searchOrigin)
        val normSearchDest = normalizeLocationText(searchDestination)
        val normTripOrigin = normalizeLocationText(trip.origin)
        val normTripDest = normalizeLocationText(trip.destination)

        val orderedStops = stops.sortedBy { it.sequence }

        // 1. Evaluate direct origin & destination scores (before stop fallback)
        val directOriginScore = when {
            normSearchOrigin.isNotEmpty() && normSearchOrigin == normTripOrigin -> 25
            normSearchOrigin.isNotEmpty() && isStrongPartialMatch(normSearchOrigin, normTripOrigin) -> 20
            else -> 0
        }

        val directDestScore = when {
            normSearchDest.isNotEmpty() && normSearchDest == normTripDest -> 25
            normSearchDest.isNotEmpty() && isStrongPartialMatch(normSearchDest, normTripDest) -> 20
            else -> 0
        }

        // Candidate matching stops
        val matchingPickupStops = if (normSearchOrigin.isNotEmpty()) {
            orderedStops.filter { it.isPickupCandidate && matchesStopName(normSearchOrigin, it.name) }
        } else {
            emptyList()
        }

        val matchingDropoffStops = if (normSearchDest.isNotEmpty()) {
            orderedStops.filter { it.isDropoffCandidate && matchesStopName(normSearchDest, it.name) }
        } else {
            emptyList()
        }

        // Resolve Origin (max 25, no double counting) and Destination (max 25, no double counting)
        // Enforce stop sequence validity when both origin and destination rely on stops.
        val originScore: Int
        val destinationScore: Int
        var matchedPickupStop: TripStop? = null
        var matchedDropoffStop: TripStop? = null

        if (directOriginScore > 0 && directDestScore > 0) {
            originScore = directOriginScore
            destinationScore = directDestScore
        } else if (directOriginScore > 0 && directDestScore == 0) {
            originScore = directOriginScore
            val dropStop = matchingDropoffStops.firstOrNull()
            if (dropStop != null) {
                destinationScore = 18
                matchedDropoffStop = dropStop
            } else {
                destinationScore = 0
            }
        } else if (directOriginScore == 0 && directDestScore > 0) {
            val pickStop = matchingPickupStops.firstOrNull()
            if (pickStop != null) {
                originScore = 18
                matchedPickupStop = pickStop
            } else {
                originScore = 0
            }
            destinationScore = directDestScore
        } else {
            // Both origin and destination are not direct matches; check stop combinations.
            // When both pickup and drop-off stops exist, drop-off sequence MUST be strictly after pickup sequence.
            var validPair: Pair<TripStop, TripStop>? = null
            for (p in matchingPickupStops) {
                val d = matchingDropoffStops.firstOrNull { it.sequence > p.sequence && it.id != p.id }
                    ?: matchingDropoffStops.firstOrNull { it.sequence > p.sequence }
                if (d != null) {
                    validPair = p to d
                    break
                }
            }

            if (validPair != null) {
                originScore = 18
                destinationScore = 18
                matchedPickupStop = validPair.first
                matchedDropoffStop = validPair.second
            } else if (matchingPickupStops.isNotEmpty() && matchingDropoffStops.isEmpty()) {
                originScore = 18
                destinationScore = 0
                matchedPickupStop = matchingPickupStops.first()
            } else if (matchingPickupStops.isEmpty() && matchingDropoffStops.isNotEmpty()) {
                originScore = 0
                destinationScore = 18
                matchedDropoffStop = matchingDropoffStops.first()
            } else {
                // Either neither matched, or both matched stops in an impossible order (dropoff.sequence <= pickup.sequence).
                // Do not award impossible stop combinations.
                originScore = 0
                destinationScore = 0
            }
        }

        // 2. Departure Time Compatibility — 20 points
        val cleanPreferredTime = preferredTime?.trim()?.takeIf { it.isNotEmpty() }
        val targetMinutes = cleanPreferredTime?.let { SupabaseWapiTripRepository.parseTimeMinutes(it) }
        val tripMinutes = SupabaseWapiTripRepository.parseTimeMinutes(trip.departureTime)

        val timeDifferenceMinutes: Int?
        val timeScore: Int
        if (targetMinutes != null && tripMinutes != null) {
            val diff = abs(tripMinutes - targetMinutes)
            timeDifferenceMinutes = diff
            timeScore = when {
                diff <= 15 -> 20
                diff <= 30 -> 17
                diff <= 60 && tripMinutes >= targetMinutes -> 14
                diff <= 60 -> 11
                diff <= 120 -> 8
                else -> 4
            }
        } else {
            timeDifferenceMinutes = null
            timeScore = 16
        }

        // 3. Seat Availability — 15 points
        val isPublishedAndAvailable = trip.isRequestableStatus && !trip.isFullOrUnavailable
        val seatScore = when {
            !isPublishedAndAvailable -> 0
            trip.availableSeats >= 2 -> 15
            trip.availableSeats == 1 -> 12
            else -> 0
        }

        // 4. Driver Trust & Experience — 15 points (max 10 rating + max 5 experience)
        val driver = trip.resolvedDriverSummary
        val effectiveRating = driver.effectiveRating
        val ratingCount = driver.ratingCount ?: 0
        val completedTrips = driver.effectiveCompletedTrips

        val ratingPoints = if (effectiveRating != null && effectiveRating > 0.0) {
            ((effectiveRating.coerceIn(0.0, 5.0) / 5.0) * 10.0).roundToInt().coerceIn(0, 10)
        } else {
            6 // Neutral baseline for a driver with no ratings yet
        }

        var experiencePoints = 0
        if (completedTrips >= 1) experiencePoints += 1
        if (completedTrips >= 3) experiencePoints += 2
        if (ratingCount >= 3) experiencePoints += 2
        val clampedExperiencePoints = experiencePoints.coerceIn(0, 5)

        val trustScore = (ratingPoints + clampedExperiencePoints).coerceIn(0, 15)

        val breakdown = WapiMatchBreakdown(
            originScore = originScore.coerceIn(0, 25),
            destinationScore = destinationScore.coerceIn(0, 25),
            timeScore = timeScore.coerceIn(0, 20),
            seatScore = seatScore.coerceIn(0, 15),
            trustScore = trustScore.coerceIn(0, 15),
            matchedPickupStopName = matchedPickupStop?.name,
            matchedDropoffStopName = matchedDropoffStop?.name
        )

        val finalScore = breakdown.totalScore
        val tier = resolveTier(finalScore)
        val reasons = buildExplainableReasons(
            trip = trip,
            breakdown = breakdown,
            timeDifferenceMinutes = timeDifferenceMinutes
        )

        return WapiMatchResult(
            score = finalScore,
            tier = tier,
            reasons = reasons,
            timeDifferenceMinutes = timeDifferenceMinutes,
            breakdown = breakdown
        )
    }

    /**
     * Resolves the user-friendly match tier for a 0..100 score.
     */
    fun resolveTier(score: Int): String {
        val clamped = score.coerceIn(0, 100)
        return when {
            clamped >= TIER_EXCELLENT_MIN -> TIER_EXCELLENT
            clamped >= TIER_GREAT_MIN -> TIER_GREAT
            clamped >= TIER_GOOD_MIN -> TIER_GOOD
            clamped >= TIER_POSSIBLE_MIN -> TIER_POSSIBLE
            else -> TIER_LOW
        }
    }

    /**
     * Builds concise, explainable match reasons (up to 4) based on the actual scoring breakdown.
     */
    fun buildExplainableReasons(
        trip: Trip,
        breakdown: WapiMatchBreakdown,
        timeDifferenceMinutes: Int?
    ): List<String> {
        val reasons = mutableListOf<String>()

        // Route / Stop reason
        when {
            breakdown.originScore == 25 && breakdown.destinationScore == 25 -> {
                reasons.add("Exact route")
            }
            breakdown.matchedPickupStopName != null && breakdown.matchedDropoffStopName != null -> {
                reasons.add("Pickup & drop-off stops match")
            }
            breakdown.matchedPickupStopName != null && breakdown.destinationScore >= 20 -> {
                reasons.add("Pickup stop matches")
            }
            breakdown.originScore >= 20 && breakdown.matchedDropoffStopName != null -> {
                reasons.add("Drop-off stop matches")
            }
            breakdown.originScore >= 20 && breakdown.destinationScore >= 20 -> {
                reasons.add("Direct route match")
            }
            breakdown.matchedPickupStopName != null -> {
                reasons.add("Pickup stop matches")
            }
            breakdown.matchedDropoffStopName != null -> {
                reasons.add("Drop-off stop matches")
            }
        }

        // Time reason
        if (timeDifferenceMinutes != null) {
            when {
                timeDifferenceMinutes == 0 -> reasons.add("Exact departure time")
                timeDifferenceMinutes <= 15 -> reasons.add("Within 15 mins")
                timeDifferenceMinutes <= 30 -> reasons.add("Within 30 mins")
                timeDifferenceMinutes <= 60 -> reasons.add("Within 1 hr")
            }
        }

        // Seat availability reason
        if (trip.isFullOrUnavailable || !trip.isRequestableStatus) {
            reasons.add("Ride full")
        } else if (trip.availableSeats >= 2) {
            reasons.add("${trip.availableSeats} seats open")
        } else if (trip.availableSeats == 1) {
            reasons.add("1 seat left")
        }

        // Driver trust / experience reason
        val driver = trip.resolvedDriverSummary
        val rating = driver.effectiveRating
        val completed = driver.effectiveCompletedTrips
        if (rating != null && rating >= 4.7 && (driver.ratingCount ?: 0) >= 1) {
            reasons.add("Top-rated driver")
        } else if (rating != null && rating >= 4.4) {
            reasons.add("Trusted driver")
        } else if (completed >= 3) {
            reasons.add("Experienced driver")
        }

        if (reasons.isEmpty()) {
            reasons.add("Same travel date")
        }

        return reasons.take(4)
    }

    /**
     * Scores and deterministically ranks a list of candidate [trips] for the given search inputs.
     *
     * Ordering rules:
     * 1. Requestable rides (`published` with `availableSeats > 0`) ALWAYS rank above full/unavailable rides.
     * 2. Higher Wapi Match `score` (descending).
     * 3. Smaller absolute departure time difference when `preferredTime` is supplied (ascending).
     * 4. Higher driver `effectiveRating` (descending, treating unrated as 0.0 for tie-breaking).
     * 5. Higher `availableSeats` (descending).
     * 6. Earlier `departureTime` (ascending).
     * 7. `trip.id` (ascending) for complete determinism.
     */
    fun rankTrips(
        trips: List<Trip>,
        searchOrigin: String,
        searchDestination: String,
        preferredTime: String? = null,
        stopsByTripId: Map<String, List<TripStop>> = emptyMap()
    ): List<Trip> {
        val scoredTrips = trips.map { trip ->
            val stops = stopsByTripId[trip.id] ?: trip.tripStops
            val match = scoreTrip(
                trip = trip,
                searchOrigin = searchOrigin,
                searchDestination = searchDestination,
                preferredTime = preferredTime,
                stops = stops
            )
            trip.copy(
                tripStops = stops,
                wapiMatch = match
            )
        }

        return scoredTrips.sortedWith(
            compareBy<Trip> { trip ->
                // 0 = bookable/requestable, 1 = full/unavailable
                if (trip.isRequestableStatus && !trip.isFullOrUnavailable) 0 else 1
            }.thenByDescending { trip ->
                trip.wapiMatch?.score ?: 0
            }.thenBy { trip ->
                trip.wapiMatch?.timeDifferenceMinutes ?: Int.MAX_VALUE
            }.thenByDescending { trip ->
                trip.resolvedDriverSummary.effectiveRating ?: 0.0
            }.thenByDescending { trip ->
                trip.availableSeats
            }.thenBy { trip ->
                trip.departureTime
            }.thenBy { trip ->
                trip.id
            }
        )
    }
}
