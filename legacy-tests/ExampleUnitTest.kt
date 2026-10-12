package com.example

import com.example.data.DriverSummary
import com.example.data.Trip
import com.example.data.TripStop
import com.example.data.WapiMatchScorer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local unit tests for deterministic Wapi Match scoring and ranking (Phase 4.6B).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `wapi match exact route within 15 mins top rated driver scores 100 and Excellent Match`() {
        val trip = Trip(
            id = "trip-1",
            driverId = "driver-1",
            origin = "  Nairobi  ",
            destination = "Nakuru",
            departureDate = "2026-10-25",
            departureTime = "08:10:00",
            totalSeats = 4,
            availableSeats = 3,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = "driver-1",
                fullName = "Grace Wanjiku",
                ratingAverage = 5.0,
                ratingCount = 6,
                completedTrips = 10
            )
        )

        val result = WapiMatchScorer.scoreTrip(
            trip = trip,
            searchOrigin = "nairobi",
            searchDestination = "  NAKURU ",
            preferredTime = "08:00:00"
        )

        assertEquals(25, result.breakdown.originScore)
        assertEquals(25, result.breakdown.destinationScore)
        assertEquals(20, result.breakdown.timeScore)
        assertEquals(15, result.breakdown.seatScore)
        assertEquals(15, result.breakdown.trustScore)
        assertEquals(100, result.score)
        assertEquals(WapiMatchScorer.TIER_EXCELLENT, result.tier)
        assertEquals(10, result.timeDifferenceMinutes)
        assertTrue(result.reasons.contains("Exact route"))
        assertTrue(result.reasons.contains("Within 15 mins"))
        assertTrue(result.reasons.contains("Top-rated driver"))
    }

    @Test
    fun `wapi match stop based pickup and dropoff awards 18 each only when sequence is valid`() {
        val stops = listOf(
            TripStop(id = "s1", tripId = "t-stops", stopType = "pickup", name = "Westlands", sequence = 1),
            TripStop(id = "s2", tripId = "t-stops", stopType = "both", name = "Naivasha", sequence = 2),
            TripStop(id = "s3", tripId = "t-stops", stopType = "dropoff", name = "Gilgil", sequence = 3)
        )
        val trip = Trip(
            id = "t-stops",
            driverId = "driver-2",
            origin = "Nairobi CBD",
            destination = "Nakuru Town",
            departureDate = "2026-10-25",
            departureTime = "09:00:00",
            totalSeats = 3,
            availableSeats = 2,
            tripStatus = "published",
            tripStops = stops,
            driverSummary = DriverSummary(id = "driver-2", fullName = "New Driver")
        )

        // Valid stop sequence: Westlands (seq 1) -> Gilgil (seq 3)
        val validMatch = WapiMatchScorer.scoreTrip(
            trip = trip,
            searchOrigin = "Westlands",
            searchDestination = "Gilgil",
            preferredTime = null
        )
        assertEquals(18, validMatch.breakdown.originScore)
        assertEquals(18, validMatch.breakdown.destinationScore)
        assertEquals(16, validMatch.breakdown.timeScore) // no preferredTime -> 16
        assertEquals(15, validMatch.breakdown.seatScore) // 2 seats -> 15
        assertEquals(6, validMatch.breakdown.trustScore) // new driver baseline -> 6
        assertEquals(73, validMatch.score)
        assertEquals(WapiMatchScorer.TIER_GOOD, validMatch.tier)
        assertTrue(validMatch.reasons.contains("Pickup & drop-off stops match"))

        // Impossible stop sequence: Gilgil (seq 3) -> Westlands (seq 1) must award 0
        val impossibleMatch = WapiMatchScorer.scoreTrip(
            trip = trip,
            searchOrigin = "Gilgil",
            searchDestination = "Westlands",
            preferredTime = null
        )
        assertEquals(0, impossibleMatch.breakdown.originScore)
        assertEquals(0, impossibleMatch.breakdown.destinationScore)
    }

    @Test
    fun `wapi match ranking ensures bookable rides always rank above full rides regardless of driver rating`() {
        val fullTopRatedTrip = Trip(
            id = "trip-full",
            driverId = "driver-top",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-25",
            departureTime = "08:00:00",
            totalSeats = 4,
            availableSeats = 0,
            tripStatus = "full",
            driverSummary = DriverSummary(
                id = "driver-top",
                fullName = "Top Rated Full Driver",
                ratingAverage = 5.0,
                ratingCount = 20,
                completedTrips = 25
            )
        )

        val bookableNewDriverTrip = Trip(
            id = "trip-bookable-new",
            driverId = "driver-new",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-25",
            departureTime = "08:25:00",
            totalSeats = 3,
            availableSeats = 1,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = "driver-new",
                fullName = "New Driver Bookable"
            )
        )

        val bookableBestMatchTrip = Trip(
            id = "trip-bookable-best",
            driverId = "driver-best",
            origin = "Nairobi",
            destination = "Nakuru",
            departureDate = "2026-10-25",
            departureTime = "08:05:00",
            totalSeats = 4,
            availableSeats = 3,
            tripStatus = "published",
            driverSummary = DriverSummary(
                id = "driver-best",
                fullName = "Experienced Driver",
                ratingAverage = 4.9,
                ratingCount = 8,
                completedTrips = 12
            )
        )

        val rankedFirstRun = WapiMatchScorer.rankTrips(
            trips = listOf(fullTopRatedTrip, bookableNewDriverTrip, bookableBestMatchTrip),
            searchOrigin = "Nairobi",
            searchDestination = "Nakuru",
            preferredTime = "08:00:00"
        )

        val rankedSecondRun = WapiMatchScorer.rankTrips(
            trips = listOf(bookableNewDriverTrip, fullTopRatedTrip, bookableBestMatchTrip),
            searchOrigin = "Nairobi",
            searchDestination = "Nakuru",
            preferredTime = "08:00:00"
        )

        // Deterministic ordering across runs
        assertEquals(rankedFirstRun.map { it.id }, rankedSecondRun.map { it.id })

        // Best bookable first, second bookable second, full ride last
        assertEquals(listOf("trip-bookable-best", "trip-bookable-new", "trip-full"), rankedFirstRun.map { it.id })
        assertEquals(0, rankedFirstRun.last().wapiMatch!!.breakdown.seatScore)
    }
}

