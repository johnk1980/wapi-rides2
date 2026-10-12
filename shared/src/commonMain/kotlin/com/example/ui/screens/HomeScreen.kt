package com.example.ui.screens

import com.example.resources.Res
import com.example.resources.action_find_a_ride
import com.example.resources.action_find_a_ride_desc
import com.example.resources.action_offer_a_ride
import com.example.resources.action_offer_a_ride_desc
import com.example.resources.coming_soon_badge
import com.example.resources.empty_upcoming_trip
import com.example.resources.empty_upcoming_trip_hint
import com.example.resources.home_tagline
import com.example.resources.popular_routes_placeholder_desc
import com.example.resources.popular_routes_placeholder_title
import com.example.resources.section_popular_routes
import com.example.resources.section_upcoming_trip
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Trip
import com.example.data.UserProfile
import com.example.ui.components.WapiBrandLogoSlot
import com.example.ui.components.WapiEmptyStateSection
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiUserAvatar
import com.example.ui.theme.WapiBorder
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiGreenTint
import com.example.ui.theme.WapiOrange
import com.example.ui.theme.WapiOrangeTint
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.theme.WapiSubtleSurface
import com.example.ui.theme.WapiWhite

@Composable
fun HomeScreen(
    userProfile: UserProfile?,
    isProfileLoading: Boolean,
    upcomingTrips: List<Trip> = emptyList(),
    tripPublishedBannerMessage: String? = null,
    unreadNotificationCount: Int = 0,
    onFindRideClick: () -> Unit,
    onOfferRideClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onTripClick: (Trip) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val greetingPrefix = remember { resolveGreetingPrefix() }
    val displayName = userProfile?.firstName?.takeIf { it.isNotBlank() }

    val greetingLine = if (!displayName.isNullOrBlank()) {
        "$greetingPrefix, $displayName"
    } else {
        greetingPrefix
    }

    val nextUpcomingTrip = upcomingTrips.firstOrNull {
        it.tripStatus.equals("published", ignoreCase = true) ||
            it.tripStatus.equals("full", ignoreCase = true) ||
            it.tripStatus.equals("draft", ignoreCase = true)
    } ?: upcomingTrips.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("home_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = WapiDimens.MaxContentWidth)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = WapiDimens.ScreenHorizontalPadding,
                    vertical = WapiDimens.SpaceLG
                )
        ) {
            // Header Row: Brand mark + Notifications button with unread badge + Avatar button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WapiBrandLogoSlot(
                    onDarkBackground = false,
                    size = 44.dp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onNotificationsClick)
                                .testTag("home_notifications_button"),
                            shape = CircleShape,
                            color = WapiWhite,
                            border = BorderStroke(1.dp, WapiBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = WapiDeepGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        if (unreadNotificationCount > 0) {
                            val badgeText = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString()
                            Surface(
                                shape = CircleShape,
                                color = WapiOrange,
                                border = BorderStroke(1.5.dp, WapiWhite),
                                modifier = Modifier
                                    .padding(top = 2.dp, end = 2.dp)
                                    .testTag("home_notifications_unread_badge")
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = WapiDeepGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    WapiUserAvatar(
                        avatarUrl = userProfile?.avatarUrl,
                        initials = userProfile?.initials ?: "WR",
                        size = 48.dp,
                        contentDescription = "Profile",
                        onClick = onProfileClick,
                        testTag = "home_profile_avatar_button",
                        remoteImageTestTag = "home_profile_avatar_remote_image"
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!tripPublishedBannerMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = tripPublishedBannerMessage,
                    isError = false,
                    testTag = "home_trip_published_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            // Personalized Greeting
            Text(
                text = greetingLine,
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("home_greeting_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.home_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            // Prominent Primary Mobility Actions
            ProminentActionTile(
                title = stringResource(Res.string.action_find_a_ride),
                subtitle = stringResource(Res.string.action_find_a_ride_desc),
                icon = Icons.Outlined.Search,
                containerColor = WapiDeepGreen,
                contentColor = WapiWhite,
                subtitleColor = WapiWhite.copy(alpha = 0.82f),
                iconContainerColor = WapiWhite.copy(alpha = 0.14f),
                iconTint = WapiOrange,
                borderColor = WapiDeepGreen,
                onClick = onFindRideClick,
                testTag = "home_find_ride_action"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            ProminentActionTile(
                title = stringResource(Res.string.action_offer_a_ride),
                subtitle = stringResource(Res.string.action_offer_a_ride_desc),
                icon = Icons.Outlined.DirectionsCar,
                containerColor = WapiWhite,
                contentColor = WapiPrimaryText,
                subtitleColor = WapiSecondaryText,
                iconContainerColor = WapiOrangeTint,
                iconTint = WapiDeepGreen,
                borderColor = WapiBorder,
                onClick = onOfferRideClick,
                testTag = "home_offer_ride_action"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXL))

            // Upcoming Trip Section
            SectionHeader(title = stringResource(Res.string.section_upcoming_trip))

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            if (nextUpcomingTrip != null) {
                WapiTripSummaryCard(
                    trip = nextUpcomingTrip,
                    onClick = { onTripClick(nextUpcomingTrip) },
                    testTag = "home_upcoming_trip_card"
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_upcoming_trip_section"),
                    shape = RoundedCornerShape(WapiDimens.RadiusMD),
                    color = WapiWhite,
                    border = BorderStroke(1.dp, WapiBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = WapiGreenTint,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = WapiDeepGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.empty_upcoming_trip),
                                style = MaterialTheme.typography.titleMedium,
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("home_upcoming_trip_empty_text")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(Res.string.empty_upcoming_trip_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXL))

            // Popular Routes / Recommended Section (Polished Empty/Placeholder State)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = stringResource(Res.string.section_popular_routes))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WapiGreenTint
                ) {
                    Text(
                        text = stringResource(Res.string.coming_soon_badge),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            WapiEmptyStateSection(
                icon = Icons.Outlined.Explore,
                title = stringResource(Res.string.popular_routes_placeholder_title),
                description = stringResource(Res.string.popular_routes_placeholder_desc),
                testTag = "home_popular_routes_section"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
        }
    }
}

@Composable
fun WapiTripSummaryCard(
    trip: Trip,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    roleLabelText: String? = null,
    ratingStatusText: String? = null,
    isRatedBadge: Boolean = false,
    testTag: String = "wapi_trip_summary_card"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(RoundedCornerShape(WapiDimens.RadiusMD))
                        .clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .testTag(testTag),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiWhite,
        border = BorderStroke(1.dp, WapiBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WapiDimens.SpaceLG)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!roleLabelText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WapiSubtleSurface,
                            border = BorderStroke(1.dp, WapiBorder)
                        ) {
                            Text(
                                text = roleLabelText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiSecondaryText,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("${testTag}_role_badge")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = WapiGreenTint
                    ) {
                        Text(
                            text = trip.tripStatus.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("${testTag}_status_badge")
                        )
                    }

                    if (!ratingStatusText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isRatedBadge) WapiGreenTint else WapiOrangeTint,
                            border = BorderStroke(
                                1.dp,
                                if (isRatedBadge) WapiPrimaryGreen.copy(alpha = 0.25f) else WapiOrange.copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = ratingStatusText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("${testTag}_rating_status")
                            )
                        }
                    }
                }

                Text(
                    text = trip.formattedContribution,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WapiDeepGreen
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.origin,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WapiPrimaryText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = WapiSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WapiPrimaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            HorizontalDivider(thickness = 1.dp, color = WapiBorder)

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = WapiSecondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${trip.departureDate} · ${trip.formattedDepartureTimeShort}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WapiSecondaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.EventSeat,
                        contentDescription = null,
                        tint = WapiDeepGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${trip.availableSeats}/${trip.totalSeats} seats",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiDeepGreen
                    )
                }
            }

            if (!trip.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trip.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = WapiSecondaryText
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = WapiPrimaryText
    )
}

@Composable
private fun ProminentActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    subtitleColor: Color,
    iconContainerColor: Color,
    iconTint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WapiDimens.RadiusMD))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconContainerColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.75f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun resolveGreetingPrefix(): String {
    val hour = com.example.platform.currentHourOfDay()
    return when (hour) {
        in 4..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}
