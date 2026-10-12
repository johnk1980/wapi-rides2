package com.example.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String,
    @SerialName("full_name")
    val fullName: String = "",
    @SerialName("phone")
    val phone: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("rating_average")
    val ratingAverage: Double = 0.0,
    @SerialName("rating_count")
    val ratingCount: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0
) {
    val firstName: String
        get() {
            val trimmed = fullName.trim()
            if (trimmed.isNotEmpty()) {
                return trimmed.substringBefore(" ")
            }
            val emailPrefix = email.substringBefore("@").trim()
            return if (emailPrefix.isNotEmpty()) {
                emailPrefix.replaceFirstChar { it.uppercase() }
            } else {
                "Traveller"
            }
        }

    val initials: String
        get() {
            val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                email.isNotBlank() -> email.take(2).uppercase()
                else -> "WR"
            }
        }

    val cleanBio: String?
        get() = bio?.trim()?.takeIf { it.isNotEmpty() }

    val formattedAccountStatus: String
        get() {
            val raw = status?.trim()?.lowercase().orEmpty()
            return when (raw) {
                "active", "" -> "Active"
                "suspended" -> "Suspended"
                "pending" -> "Pending"
                else -> raw.replaceFirstChar { it.uppercase() }
            }
        }

    val hasRatings: Boolean
        get() = ratingCount > 0 && ratingAverage > 0.0

    val formattedRatingAverage: String
        get() = if (hasRatings) {
            "${com.example.platform.formatOneDecimal(ratingAverage)} ★"
        } else {
            "No ratings yet"
        }

    val formattedRatingCount: String
        get() = when {
            ratingCount <= 0 -> "0 ratings"
            ratingCount == 1 -> "1 rating"
            else -> "$ratingCount ratings"
        }

    val formattedCompletedTrips: String
        get() = when (completedTrips) {
            1 -> "1 completed trip"
            else -> "$completedTrips completed trips"
        }
}
