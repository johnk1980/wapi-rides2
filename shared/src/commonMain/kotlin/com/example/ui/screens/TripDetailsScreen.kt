package com.example.ui.screens

import com.example.resources.Res
import com.example.resources.action_back
import com.example.resources.cancel_booking_dialog_body
import com.example.resources.cancel_booking_dialog_confirm
import com.example.resources.cancel_booking_dialog_dismiss
import com.example.resources.cancel_booking_dialog_title
import com.example.resources.lifecycle_booking_cancelled_body
import com.example.resources.lifecycle_booking_cancelled_title
import com.example.resources.lifecycle_booking_confirmed_title
import com.example.resources.lifecycle_cta_cancel_booking
import com.example.resources.lifecycle_cta_complete_trip
import com.example.resources.lifecycle_cta_start_trip
import com.example.resources.lifecycle_ride_ready_body
import com.example.resources.lifecycle_ride_ready_title
import com.example.resources.lifecycle_trip_completed_body
import com.example.resources.lifecycle_trip_completed_title
import com.example.resources.lifecycle_trip_in_progress_driver_body
import com.example.resources.lifecycle_trip_in_progress_passenger_body
import com.example.resources.lifecycle_trip_in_progress_title
import com.example.resources.rating_rate_driver
import com.example.resources.rating_rate_passenger
import com.example.resources.rating_review_placeholder
import com.example.resources.rating_section_title
import com.example.resources.rating_submit_cta
import com.example.resources.rating_submitted_badge
import com.example.resources.rating_submitted_plain
import com.example.resources.rating_thank_you_message
import com.example.resources.request_state_cancelled_title
import com.example.resources.request_state_declined_title
import com.example.resources.request_state_expired_title
import com.example.resources.request_state_pending_subtitle
import com.example.resources.request_state_pending_title
import com.example.resources.trip_details_cta_cancel_request
import com.example.resources.trip_details_cta_manage_ride
import com.example.resources.trip_details_cta_request_seat
import com.example.resources.trip_details_cta_ride_full
import com.example.resources.trip_details_label_dropoff_stop
import com.example.resources.trip_details_label_passenger_message
import com.example.resources.trip_details_label_pickup_stop
import com.example.resources.trip_details_label_seats_to_request
import com.example.resources.trip_details_placeholder_passenger_message
import com.example.resources.trip_details_section_driver
import com.example.resources.trip_details_section_notes
import com.example.resources.trip_details_section_request
import com.example.resources.trip_details_section_route
import com.example.resources.trip_details_section_seats
import com.example.resources.trip_details_section_your_ride
import com.example.resources.trip_details_subtitle
import com.example.resources.trip_details_title
import com.example.resources.trip_details_your_ride_body
import com.example.resources.trip_details_your_ride_title
import com.example.resources.trip_share_cta
import com.example.resources.trip_share_section_title
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.FmdGood
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.data.Booking
import com.example.data.Trip
import com.example.data.TripRequest
import com.example.data.TripStop
import com.example.data.WapiEmergencyContact
import com.example.data.WapiTripShare
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiPrimaryButton
import com.example.ui.components.WapiSecondaryButton
import com.example.ui.components.WapiTextField
import com.example.ui.components.WapiUserAvatar
import com.example.ui.theme.WapiBorder
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiError
import com.example.ui.theme.WapiErrorContainer
import com.example.ui.theme.WapiGreenTint
import com.example.ui.theme.WapiMutedText
import com.example.ui.theme.WapiOnErrorContainer
import com.example.ui.theme.WapiOrange
import com.example.ui.theme.WapiOrangeTint
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.theme.WapiSubtleSurface
import com.example.ui.theme.WapiSuccess
import com.example.ui.theme.WapiSuccessContainer
import com.example.ui.theme.WapiWhite
import com.example.ui.viewmodel.BookingLifecycleState
import com.example.ui.viewmodel.BookingRatingFormState
import com.example.ui.viewmodel.EmergencyContactsState
import com.example.ui.viewmodel.PassengerRequestStatus
import com.example.ui.viewmodel.PassengerSeatRequestState
import com.example.ui.viewmodel.RatingState
import com.example.ui.viewmodel.TripShareState

@Composable
fun WapiRideResultCard(
    trip: Trip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "find_ride_result_card"
) {
    val isUnavailable = trip.isFullOrUnavailable
    val statusBadgeText = if (isUnavailable) "FULL" else trip.tripStatus.uppercase()
    val statusBadgeBg = if (isUnavailable) WapiSubtleSurface else WapiGreenTint
    val statusBadgeTextColor = if (isUnavailable) WapiSecondaryText else WapiDeepGreen
    val driver = trip.resolvedDriverSummary
    val wapiMatch = trip.wapiMatch

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WapiDimens.RadiusMD))
            .clickable(onClick = onClick)
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
            // Top Row: Status badge + Wapi Match badge + Contribution per seat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBadgeBg
                    ) {
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = statusBadgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (wapiMatch != null) {
                        val isHighMatch = wapiMatch.score >= 75 && !isUnavailable
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isHighMatch) WapiGreenTint else WapiOrangeTint,
                            border = BorderStroke(
                                1.dp,
                                if (isHighMatch) WapiPrimaryGreen.copy(alpha = 0.3f) else WapiOrange.copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = wapiMatch.formattedScoreBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("${testTag}_match_score")
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = trip.formattedContribution,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen
                    )
                    if (trip.contributionAmount != null) {
                        Text(
                            text = "per seat",
                            style = MaterialTheme.typography.labelSmall,
                            color = WapiMutedText
                        )
                    }
                }
            }

            if (wapiMatch != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = WapiPrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = wapiMatch.tier,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.testTag("${testTag}_match_tier")
                    )
                    if (wapiMatch.reasons.isNotEmpty()) {
                        Text(
                            text = "· ${wapiMatch.reasons.joinToString(" · ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiSecondaryText,
                            maxLines = 2,
                            modifier = Modifier.testTag("${testTag}_match_reasons")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            // Route Row: Origin -> Destination
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                    tint = WapiPrimaryGreen,
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

            // Schedule & Seat availability
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
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = WapiSecondaryText
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.EventSeat,
                        contentDescription = null,
                        tint = if (isUnavailable) WapiMutedText else WapiDeepGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isUnavailable) {
                            "0 of ${trip.totalSeats} seats left"
                        } else {
                            "${trip.availableSeats} of ${trip.totalSeats} seats left"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isUnavailable) WapiMutedText else WapiDeepGreen
                    )
                }
            }

            if (!trip.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trip.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = WapiSecondaryText,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            // Driver summary footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WapiUserAvatar(
                        avatarUrl = driver.avatarUrl,
                        initials = driver.initials,
                        size = 32.dp,
                        contentDescription = "${driver.displayName} avatar",
                        borderAlpha = 0.2f,
                        testTag = "${testTag}_driver_avatar",
                        remoteImageTestTag = "${testTag}_driver_avatar_remote_image"
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = driver.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = WapiPrimaryText
                        )
                        Text(
                            text = "${driver.formattedRating} · ${driver.formattedCompletedTrips}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WapiSecondaryText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View details",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiDeepGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = WapiDeepGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    trip: Trip?,
    currentUserId: String,
    isLoading: Boolean,
    errorMessage: String?,
    passengerRequestState: PassengerSeatRequestState = PassengerSeatRequestState(),
    bookingLifecycleState: BookingLifecycleState = BookingLifecycleState(),
    ratingState: RatingState = RatingState(),
    emergencyContactsState: EmergencyContactsState = EmergencyContactsState(),
    tripShareState: TripShareState = TripShareState(),
    onSeatsChange: (Int) -> Unit = {},
    onPickupStopSelect: (String?) -> Unit = {},
    onDropoffStopSelect: (String?) -> Unit = {},
    onMessageChange: (String) -> Unit = {},
    onSubmitRequestClick: () -> Unit = {},
    onCancelRequestClick: () -> Unit = {},
    onManageRideClick: () -> Unit = {},
    onStartTripClick: (String?) -> Unit = {},
    onCompleteTripClick: (String?) -> Unit = {},
    onRequestCancelBookingClick: (String) -> Unit = {},
    onConfirmCancelBookingClick: () -> Unit = {},
    onDismissCancelBookingDialog: () -> Unit = {},
    onRatingStarsSelected: (String, Int) -> Unit = { _, _ -> },
    onRatingReviewTextChanged: (String, String) -> Unit = { _, _ -> },
    onSubmitBookingRatingClick: (String) -> Unit = {},
    onOpenBookingConversationClick: (String?, String?, String?) -> Unit = { _, _, _ -> },
    onSelectTripShareContact: (String?) -> Unit = {},
    onShareTripClick: (String, String?) -> Unit = { _, _ -> },
    onRevokeTripShareClick: (String, String) -> Unit = { _, _ -> },
    onManageEmergencyContactsClick: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("trip_details_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = WapiDimens.MaxContentWidth)
                .verticalScroll(scrollState)
                .padding(
                    horizontal = WapiDimens.ScreenHorizontalPadding,
                    vertical = WapiDimens.SpaceMD
                )
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("trip_details_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = WapiPrimaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Text(
                text = stringResource(Res.string.trip_details_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("trip_details_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.trip_details_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            when {
                isLoading -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trip_details_loading"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp,
                                color = WapiPrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading journey details…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                !errorMessage.isNullOrBlank() || trip == null -> {
                    WapiFeedbackBanner(
                        message = errorMessage ?: "Trip details could not be loaded.",
                        isError = true,
                        testTag = "trip_details_error_banner"
                    )
                }

                else -> {
                    val isUnavailable = trip.isFullOrUnavailable
                    val driver = trip.resolvedDriverSummary
                    val isOwnTrip = currentUserId.isNotBlank() && trip.driverId == currentUserId
                    val wapiMatch = trip.wapiMatch
                    val tripStatusLower = trip.tripStatus.lowercase()
                    val statusBadgeLabel = when {
                        tripStatusLower == "in_progress" -> "IN PROGRESS"
                        tripStatusLower == "completed" -> "COMPLETED"
                        tripStatusLower == "cancelled" -> "CANCELLED"
                        isUnavailable -> "FULL"
                        else -> trip.tripStatus.uppercase()
                    }
                    val statusBadgeBg = when {
                        tripStatusLower == "in_progress" -> WapiOrangeTint
                        tripStatusLower == "completed" -> WapiSuccessContainer
                        tripStatusLower == "cancelled" -> WapiErrorContainer
                        isUnavailable -> WapiSubtleSurface
                        else -> WapiGreenTint
                    }
                    val statusBadgeTextColor = when {
                        tripStatusLower == "completed" -> WapiSuccess
                        tripStatusLower == "cancelled" -> WapiOnErrorContainer
                        isUnavailable -> WapiSecondaryText
                        else -> WapiDeepGreen
                    }

                    // Wapi Match Summary Card (when navigated from a passenger search)
                    if (wapiMatch != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trip_details_wapi_match_card"),
                            shape = RoundedCornerShape(WapiDimens.RadiusMD),
                            color = WapiGreenTint,
                            border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.3f))
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
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = WapiPrimaryGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = wapiMatch.formattedScoreBadge,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WapiDeepGreen,
                                            modifier = Modifier.testTag("trip_details_match_score")
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = WapiWhite
                                    ) {
                                        Text(
                                            text = wapiMatch.tier,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WapiDeepGreen,
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("trip_details_match_tier")
                                        )
                                    }
                                }

                                if (wapiMatch.reasons.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = wapiMatch.reasons.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WapiSecondaryText,
                                        modifier = Modifier.testTag("trip_details_match_reasons")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                    }

                    // 1. Route & Schedule Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trip_details_route_card"),
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
                                Text(
                                    text = stringResource(Res.string.trip_details_section_route),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = WapiDeepGreen
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusBadgeBg
                                ) {
                                    Text(
                                        text = statusBadgeLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = statusBadgeTextColor,
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                            .testTag("trip_details_status_badge")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                            // Origin row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = WapiGreenTint,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.MyLocation,
                                            contentDescription = null,
                                            tint = WapiDeepGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Departure (From)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WapiSecondaryText
                                    )
                                    Text(
                                        text = trip.origin,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = WapiPrimaryText,
                                        modifier = Modifier.testTag("trip_details_origin_text")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                            // Destination row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = WapiOrangeTint,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.FmdGood,
                                            contentDescription = null,
                                            tint = WapiDeepGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Destination (To)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WapiSecondaryText
                                    )
                                    Text(
                                        text = trip.destination,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = WapiPrimaryText,
                                        modifier = Modifier.testTag("trip_details_destination_text")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                            // Date & Departure Time row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceMD)
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = WapiDeepGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Travel date",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WapiSecondaryText
                                        )
                                        Text(
                                            text = trip.departureDate,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = WapiPrimaryText,
                                            modifier = Modifier.testTag("trip_details_date_text")
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = WapiDeepGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Departure time",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WapiSecondaryText
                                        )
                                        Text(
                                            text = trip.formattedDepartureTimeShort,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = WapiPrimaryText,
                                            modifier = Modifier.testTag("trip_details_time_text")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    // 2. Seats & Cost Contribution Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trip_details_seats_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(WapiDimens.SpaceLG)
                        ) {
                            Text(
                                text = stringResource(Res.string.trip_details_section_seats),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen
                            )

                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = WapiGreenTint,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.EventSeat,
                                                contentDescription = null,
                                                tint = WapiDeepGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Seat availability",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WapiSecondaryText
                                        )
                                        Text(
                                            text = "${trip.availableSeats} available of ${trip.totalSeats} total seats",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WapiPrimaryText,
                                            modifier = Modifier.testTag("trip_details_seats_text")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = WapiGreenTint,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Outlined.Payments,
                                                contentDescription = null,
                                                tint = WapiDeepGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Contribution per seat",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WapiSecondaryText
                                        )
                                        Text(
                                            text = trip.formattedContribution,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WapiDeepGreen,
                                            modifier = Modifier.testTag("trip_details_contribution_text")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    // 3. Trip Notes Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trip_details_notes_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(WapiDimens.SpaceLG)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Notes,
                                    contentDescription = null,
                                    tint = WapiDeepGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(Res.string.trip_details_section_notes),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = WapiDeepGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                            Text(
                                text = trip.notes?.takeIf { it.isNotBlank() }
                                    ?: "No additional pickup or luggage notes specified by the driver.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (!trip.notes.isNullOrBlank()) WapiPrimaryText else WapiSecondaryText,
                                modifier = Modifier.testTag("trip_details_notes_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    // 4. Driver Community Profile Summary Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trip_details_driver_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(WapiDimens.SpaceLG)
                        ) {
                            Text(
                                text = stringResource(Res.string.trip_details_section_driver),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen
                            )

                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                WapiUserAvatar(
                                    avatarUrl = driver.avatarUrl,
                                    initials = driver.initials,
                                    size = 52.dp,
                                    contentDescription = "${driver.displayName} avatar",
                                    testTag = "trip_details_driver_avatar",
                                    remoteImageTestTag = "trip_details_driver_avatar_remote_image"
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = driver.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WapiPrimaryText,
                                            modifier = Modifier.testTag("trip_details_driver_name")
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Outlined.VerifiedUser,
                                            contentDescription = "Verified Wapi member",
                                            tint = WapiPrimaryGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.StarOutline,
                                            contentDescription = null,
                                            tint = WapiOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${driver.formattedRating} · ${driver.formattedCompletedTrips}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WapiSecondaryText
                                        )
                                    }
                                }
                            }

                            if (!trip.vehicleId.isNullOrBlank() || trip.vehicleSummary != null) {
                                val vehicle = trip.vehicleSummary
                                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                                HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.testTag("trip_details_vehicle_info_row")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DirectionsCar,
                                        contentDescription = null,
                                        tint = WapiDeepGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (vehicle != null) {
                                        Column {
                                            Text(
                                                text = vehicle.displayTitle,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = WapiPrimaryText,
                                                modifier = Modifier.testTag("trip_details_vehicle_title")
                                            )
                                            Text(
                                                text = "${vehicle.displaySubtitle} · ${vehicle.seatCapacity} seats",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = WapiSecondaryText,
                                                modifier = Modifier.testTag("trip_details_vehicle_subtitle")
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Registered vehicle on file",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = WapiSecondaryText,
                                            modifier = Modifier.testTag("trip_details_vehicle_fallback")
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    // 5. Driver Own Ride & Lifecycle Section OR Passenger Seat Request & Lifecycle Section
                    if (isOwnTrip) {
                        DriverOwnRideSection(
                            trip = trip,
                            lifecycleState = bookingLifecycleState,
                            ratingState = ratingState,
                            onManageRideClick = onManageRideClick,
                            onStartTripClick = onStartTripClick,
                            onCompleteTripClick = onCompleteTripClick,
                            onRequestCancelBookingClick = onRequestCancelBookingClick,
                            onRatingStarsSelected = onRatingStarsSelected,
                            onRatingReviewTextChanged = onRatingReviewTextChanged,
                            onSubmitBookingRatingClick = { bookingId ->
                                focusManager.clearFocus()
                                onSubmitBookingRatingClick(bookingId)
                            },
                            onMessagePassengerClick = { booking ->
                                onOpenBookingConversationClick(
                                    booking.id.takeIf { it.isNotBlank() },
                                    trip.id.takeIf { it.isNotBlank() },
                                    booking.passengerId.takeIf { it.isNotBlank() }
                                )
                            }
                        )
                    } else {
                        PassengerRequestSection(
                            trip = trip,
                            requestState = passengerRequestState,
                            lifecycleState = bookingLifecycleState,
                            ratingState = ratingState,
                            onSeatsChange = onSeatsChange,
                            onPickupStopSelect = onPickupStopSelect,
                            onDropoffStopSelect = onDropoffStopSelect,
                            onMessageChange = onMessageChange,
                            onSubmitRequestClick = {
                                focusManager.clearFocus()
                                onSubmitRequestClick()
                            },
                            onCancelRequestClick = onCancelRequestClick,
                            onRequestCancelBookingClick = onRequestCancelBookingClick,
                            onRatingStarsSelected = onRatingStarsSelected,
                            onRatingReviewTextChanged = onRatingReviewTextChanged,
                            onSubmitBookingRatingClick = { bookingId ->
                                focusManager.clearFocus()
                                onSubmitBookingRatingClick(bookingId)
                            },
                            onMessageDriverClick = { booking ->
                                onOpenBookingConversationClick(
                                    booking?.id?.takeIf { it.isNotBlank() },
                                    trip.id.takeIf { it.isNotBlank() },
                                    trip.driverId.takeIf { it.isNotBlank() }
                                )
                            }
                        )
                    }

                    // 6. Trip Sharing Foundation (Phase 4.8B) — available for active/upcoming trips
                    val canShareTrip = currentUserId.isNotBlank() &&
                        tripStatusLower != "cancelled" &&
                        (isOwnTrip ||
                            passengerRequestState.confirmedBooking != null ||
                            passengerRequestState.status == PassengerRequestStatus.ACCEPTED ||
                            passengerRequestState.status == PassengerRequestStatus.PENDING ||
                            tripShareState.tripShares.isNotEmpty())

                    if (canShareTrip) {
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                        val activeBookingId = if (isOwnTrip) {
                            bookingLifecycleState.primaryStartableBooking?.id
                                ?: bookingLifecycleState.primaryCompletableBooking?.id
                        } else {
                            passengerRequestState.confirmedBooking?.id
                        }
                        TripSharingFoundationCard(
                            trip = trip,
                            contacts = emergencyContactsState.contacts,
                            tripShareState = tripShareState,
                            onSelectContact = onSelectTripShareContact,
                            onShareTripClick = {
                                onShareTripClick(trip.id, activeBookingId)
                            },
                            onRevokeShareClick = { shareId ->
                                onRevokeTripShareClick(shareId, trip.id)
                            },
                            onManageEmergencyContactsClick = onManageEmergencyContactsClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
        }

        // Confirmation Dialog for Cancelling a Confirmed Booking (Phase 4.4)
        if (!bookingLifecycleState.cancelConfirmationBookingId.isNullOrBlank()) {
            BasicAlertDialog(
                onDismissRequest = onDismissCancelBookingDialog
            ) {
                Surface(
                    shape = RoundedCornerShape(WapiDimens.RadiusLG),
                    color = WapiWhite,
                    border = BorderStroke(1.dp, WapiBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WapiDimens.SpaceMD)
                        .testTag("cancel_booking_confirm_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WapiDimens.SpaceLG)
                    ) {
                        Text(
                            text = stringResource(Res.string.cancel_booking_dialog_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiPrimaryText,
                            modifier = Modifier.testTag("cancel_booking_dialog_title")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(Res.string.cancel_booking_dialog_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = WapiSecondaryText,
                            modifier = Modifier.testTag("cancel_booking_dialog_body")
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiSecondaryButton(
                                text = stringResource(Res.string.cancel_booking_dialog_dismiss),
                                onClick = onDismissCancelBookingDialog,
                                enabled = !bookingLifecycleState.isCancellingBooking,
                                modifier = Modifier.weight(1f),
                                testTag = "cancel_booking_keep_button"
                            )

                            WapiPrimaryButton(
                                text = stringResource(Res.string.cancel_booking_dialog_confirm),
                                onClick = onConfirmCancelBookingClick,
                                enabled = !bookingLifecycleState.isCancellingBooking,
                                isLoading = bookingLifecycleState.isCancellingBooking,
                                modifier = Modifier.weight(1f),
                                testTag = "cancel_booking_confirm_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverOwnRideSection(
    trip: Trip,
    lifecycleState: BookingLifecycleState,
    ratingState: RatingState,
    onManageRideClick: () -> Unit,
    onStartTripClick: (String?) -> Unit,
    onCompleteTripClick: (String?) -> Unit,
    onRequestCancelBookingClick: (String) -> Unit,
    onRatingStarsSelected: (String, Int) -> Unit,
    onRatingReviewTextChanged: (String, String) -> Unit,
    onSubmitBookingRatingClick: (String) -> Unit,
    onMessagePassengerClick: (Booking) -> Unit = {}
) {
    val tripStatusLower = trip.tripStatus.lowercase()
    val confirmedBookings = lifecycleState.confirmedBookings
    val inProgressBookings = lifecycleState.inProgressBookings
    val completedBookings = lifecycleState.completedBookings
    val hasConfirmedBookings = confirmedBookings.isNotEmpty()
    val isTripInProgress = tripStatusLower == "in_progress" || inProgressBookings.isNotEmpty()
    val isTripCompleted = tripStatusLower == "completed" ||
        (completedBookings.isNotEmpty() && !isTripInProgress && !hasConfirmedBookings)

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!lifecycleState.feedbackBannerMessage.isNullOrBlank()) {
            WapiFeedbackBanner(
                message = lifecycleState.feedbackBannerMessage,
                isError = false,
                testTag = "trip_details_lifecycle_feedback_banner"
            )
            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
        }

        if (!lifecycleState.errorMessage.isNullOrBlank()) {
            WapiFeedbackBanner(
                message = lifecycleState.errorMessage,
                isError = true,
                testTag = "trip_details_lifecycle_error_banner"
            )
            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trip_details_own_ride_card"),
            shape = RoundedCornerShape(WapiDimens.RadiusMD),
            color = WapiGreenTint,
            border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WapiDimens.SpaceLG)
            ) {
                Text(
                    text = stringResource(Res.string.trip_details_section_your_ride),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WapiDeepGreen
                )

                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                val headerTitle = when {
                    isTripCompleted -> stringResource(Res.string.lifecycle_trip_completed_title)
                    isTripInProgress -> stringResource(Res.string.lifecycle_trip_in_progress_title)
                    hasConfirmedBookings -> stringResource(Res.string.lifecycle_ride_ready_title)
                    else -> stringResource(Res.string.trip_details_your_ride_title)
                }

                val headerBody = when {
                    isTripCompleted -> stringResource(Res.string.lifecycle_trip_completed_body)
                    isTripInProgress -> stringResource(Res.string.lifecycle_trip_in_progress_driver_body)
                    hasConfirmedBookings -> stringResource(Res.string.lifecycle_ride_ready_body)
                    else -> stringResource(Res.string.trip_details_your_ride_body)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            isTripCompleted -> Icons.Outlined.CheckCircle
                            isTripInProgress -> Icons.Outlined.PlayArrow
                            else -> Icons.Outlined.DirectionsCar
                        },
                        contentDescription = null,
                        tint = WapiDeepGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.testTag("trip_details_your_ride_title")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = headerBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WapiPrimaryText,
                    modifier = Modifier.testTag("trip_details_driver_lifecycle_status_text")
                )

                // List active/completed bookings on this trip for driver visibility, pre-departure cancellation, and post-completion passenger rating
                val visibleBookings = lifecycleState.tripBookings.filter {
                    it.isConfirmed || it.isInProgress || it.isCompleted || it.isCancelled
                }
                val firstCompletedBookingId = visibleBookings.firstOrNull { it.isCompleted }?.id

                if (visibleBookings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                    HorizontalDivider(thickness = 1.dp, color = WapiPrimaryGreen.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                    Text(
                        text = "Passenger Bookings (${visibleBookings.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        visibleBookings.forEachIndexed { index, booking ->
                            val isPrimaryRatingCard = booking.id == firstCompletedBookingId
                            DriverBookingItemCard(
                                booking = booking,
                                tripStatus = trip.tripStatus,
                                isActionDisabled = lifecycleState.isProcessingLifecycleAction,
                                ratingFormState = ratingState.formForBooking(booking),
                                isAnyRatingSubmitting = !ratingState.activeSubmittingBookingId.isNullOrBlank(),
                                isPrimaryRatingCard = isPrimaryRatingCard,
                                onCancelBookingClick = { onRequestCancelBookingClick(booking.id) },
                                onRatingStarsSelected = { stars -> onRatingStarsSelected(booking.id, stars) },
                                onRatingReviewTextChanged = { text -> onRatingReviewTextChanged(booking.id, text) },
                                onSubmitBookingRatingClick = { onSubmitBookingRatingClick(booking.id) },
                                onMessagePassengerClick = { onMessagePassengerClick(booking) },
                                testTagPrefix = "driver_trip_booking_$index"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                // Driver Lifecycle Primary Actions:
                // - Before start (when confirmed bookings exist): "Start Trip"
                // - While in_progress: "Complete Trip"
                // - When completed: interactive rating UI is rendered inside completed bookings (or summary card if no bookings loaded)
                when {
                    isTripCompleted -> {
                        if (firstCompletedBookingId == null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WapiWhite,
                                border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("trip_details_rate_trip_card")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.StarOutline,
                                            contentDescription = null,
                                            tint = WapiOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(Res.string.rating_section_title),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WapiDeepGreen,
                                            modifier = Modifier.testTag("trip_details_rate_trip_text")
                                        )
                                    }
                                    Text(
                                        text = stringResource(Res.string.rating_rate_passenger),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WapiSecondaryText
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                        }
                    }

                    isTripInProgress -> {
                        val targetBookingId = lifecycleState.primaryCompletableBooking?.id
                        WapiPrimaryButton(
                            text = stringResource(Res.string.lifecycle_cta_complete_trip),
                            onClick = { onCompleteTripClick(targetBookingId) },
                            enabled = !lifecycleState.isProcessingLifecycleAction,
                            isLoading = lifecycleState.isCompleting,
                            testTag = "trip_details_complete_trip_button"
                        )
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                    }

                    hasConfirmedBookings && tripStatusLower != "cancelled" -> {
                        val targetBookingId = lifecycleState.primaryStartableBooking?.id
                        WapiPrimaryButton(
                            text = stringResource(Res.string.lifecycle_cta_start_trip),
                            onClick = { onStartTripClick(targetBookingId) },
                            enabled = !lifecycleState.isProcessingLifecycleAction,
                            isLoading = lifecycleState.isStarting,
                            testTag = "trip_details_start_trip_button"
                        )
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                    }
                }

                WapiSecondaryButton(
                    text = stringResource(Res.string.trip_details_cta_manage_ride),
                    onClick = onManageRideClick,
                    enabled = !lifecycleState.isProcessingLifecycleAction,
                    testTag = "trip_details_manage_ride_button"
                )
            }
        }
    }
}

@Composable
private fun DriverBookingItemCard(
    booking: Booking,
    tripStatus: String,
    isActionDisabled: Boolean,
    ratingFormState: BookingRatingFormState,
    isAnyRatingSubmitting: Boolean,
    isPrimaryRatingCard: Boolean,
    onCancelBookingClick: () -> Unit,
    onRatingStarsSelected: (Int) -> Unit,
    onRatingReviewTextChanged: (String) -> Unit,
    onSubmitBookingRatingClick: () -> Unit,
    onMessagePassengerClick: () -> Unit = {},
    testTagPrefix: String
) {
    val passenger = booking.resolvedPassengerSummary
    val canDriverCancel = booking.isConfirmed &&
        (tripStatus.equals("published", ignoreCase = true) || tripStatus.equals("full", ignoreCase = true))

    val badgeLabel = when {
        booking.isCompleted -> "Trip completed"
        booking.isInProgress -> "Trip in progress"
        booking.isCancelled -> "Booking cancelled"
        else -> "Confirmed"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagPrefix),
        shape = RoundedCornerShape(10.dp),
        color = WapiWhite,
        border = BorderStroke(1.dp, WapiBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = passenger.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryText,
                        modifier = Modifier.testTag("${testTagPrefix}_passenger_name")
                    )
                    Text(
                        text = "${booking.seatsBooked} seat(s) · ${booking.bookingReference.ifBlank { "Confirmed" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        booking.isCompleted -> WapiSuccessContainer
                        booking.isInProgress -> WapiOrangeTint
                        booking.isCancelled -> WapiErrorContainer
                        else -> WapiGreenTint
                    }
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            booking.isCompleted -> WapiSuccess
                            booking.isCancelled -> WapiOnErrorContainer
                            else -> WapiDeepGreen
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("${testTagPrefix}_status_badge")
                    )
                }
            }

            if (!booking.isCancelled) {
                Spacer(modifier = Modifier.height(8.dp))
                WapiSecondaryButton(
                    text = "Message Passenger",
                    onClick = onMessagePassengerClick,
                    enabled = !isActionDisabled,
                    testTag = "${testTagPrefix}_message_passenger_button"
                )
            }

            if (canDriverCancel) {
                Spacer(modifier = Modifier.height(8.dp))
                WapiSecondaryButton(
                    text = stringResource(Res.string.lifecycle_cta_cancel_booking),
                    onClick = onCancelBookingClick,
                    enabled = !isActionDisabled,
                    testTag = "${testTagPrefix}_cancel_button"
                )
            }

            // Phase 4.5: Driver rates Passenger on completed booking
            if (booking.isCompleted && booking.id.isNotBlank()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                BookingRatingSectionCard(
                    booking = booking,
                    ratingFormState = ratingFormState,
                    isPassengerRatingDriver = false,
                    targetPersonName = passenger.displayName,
                    isAnyRatingSubmitting = isAnyRatingSubmitting,
                    onRatingStarsSelected = onRatingStarsSelected,
                    onRatingReviewTextChanged = onRatingReviewTextChanged,
                    onSubmitRatingClick = onSubmitBookingRatingClick,
                    useCanonicalTags = isPrimaryRatingCard,
                    testTagPrefix = "${testTagPrefix}_rating"
                )
            }
        }
    }
}

@Composable
private fun PassengerRequestSection(
    trip: Trip,
    requestState: PassengerSeatRequestState,
    lifecycleState: BookingLifecycleState,
    ratingState: RatingState,
    onSeatsChange: (Int) -> Unit,
    onPickupStopSelect: (String?) -> Unit,
    onDropoffStopSelect: (String?) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmitRequestClick: () -> Unit,
    onCancelRequestClick: () -> Unit,
    onRequestCancelBookingClick: (String) -> Unit,
    onRatingStarsSelected: (String, Int) -> Unit,
    onRatingReviewTextChanged: (String, String) -> Unit,
    onSubmitBookingRatingClick: (String) -> Unit,
    onMessageDriverClick: (Booking?) -> Unit = {}
) {
    val bannerMessage = lifecycleState.feedbackBannerMessage ?: requestState.feedbackBannerMessage
    val errorMessage = lifecycleState.errorMessage ?: requestState.errorMessage

    Column(modifier = Modifier.fillMaxWidth()) {
        if (!bannerMessage.isNullOrBlank()) {
            WapiFeedbackBanner(
                message = bannerMessage,
                isError = false,
                testTag = "trip_details_request_feedback_banner"
            )
            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
        }

        if (!errorMessage.isNullOrBlank()) {
            WapiFeedbackBanner(
                message = errorMessage,
                isError = true,
                testTag = "trip_details_request_error_banner"
            )
            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
        }

        when (requestState.status) {
            PassengerRequestStatus.LOADING -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_details_request_loading"),
                    shape = RoundedCornerShape(WapiDimens.RadiusMD),
                    color = WapiWhite,
                    border = BorderStroke(1.dp, WapiBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WapiDimens.SpaceLG),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = WapiPrimaryGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Checking seat request status…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WapiSecondaryText
                        )
                    }
                }
            }

            PassengerRequestStatus.PENDING -> {
                PendingRequestCard(
                    request = requestState.existingRequest,
                    isCancelling = requestState.isCancelling,
                    onCancelClick = onCancelRequestClick
                )
            }

            PassengerRequestStatus.ACCEPTED -> {
                ConfirmedBookingCard(
                    trip = trip,
                    request = requestState.existingRequest,
                    booking = requestState.confirmedBooking,
                    ratingState = ratingState,
                    isLifecycleBusy = lifecycleState.isProcessingLifecycleAction,
                    onCancelBookingClick = { bookingId ->
                        onRequestCancelBookingClick(bookingId)
                    },
                    onRatingStarsSelected = onRatingStarsSelected,
                    onRatingReviewTextChanged = onRatingReviewTextChanged,
                    onSubmitBookingRatingClick = onSubmitBookingRatingClick,
                    onMessageDriverClick = { onMessageDriverClick(requestState.confirmedBooking) }
                )
            }

            PassengerRequestStatus.DECLINED,
            PassengerRequestStatus.CANCELLED,
            PassengerRequestStatus.EXPIRED -> {
                HistoricalRequestStatusBanner(status = requestState.status)
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                RequestSeatFormCard(
                    trip = trip,
                    requestState = requestState,
                    onSeatsChange = onSeatsChange,
                    onPickupStopSelect = onPickupStopSelect,
                    onDropoffStopSelect = onDropoffStopSelect,
                    onMessageChange = onMessageChange,
                    onSubmitRequestClick = onSubmitRequestClick
                )
            }

            PassengerRequestStatus.NO_REQUEST,
            PassengerRequestStatus.ERROR -> {
                RequestSeatFormCard(
                    trip = trip,
                    requestState = requestState,
                    onSeatsChange = onSeatsChange,
                    onPickupStopSelect = onPickupStopSelect,
                    onDropoffStopSelect = onDropoffStopSelect,
                    onMessageChange = onMessageChange,
                    onSubmitRequestClick = onSubmitRequestClick
                )
            }
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: TripRequest?,
    isCancelling: Boolean,
    onCancelClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_details_pending_request_card"),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiWhite,
        border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.35f))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WapiOrangeTint,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.HourglassTop,
                                contentDescription = null,
                                tint = WapiDeepGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.request_state_pending_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiPrimaryText,
                            modifier = Modifier.testTag("trip_details_request_status_title")
                        )
                        Text(
                            text = stringResource(Res.string.request_state_pending_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiSecondaryText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WapiOrangeTint
                ) {
                    Text(
                        text = "PENDING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (request != null) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                Text(
                    text = "Requested seats: ${request.requestedSeats}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = WapiPrimaryText
                )

                if (!request.pickupStopName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pickup: ${request.pickupStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }

                if (!request.dropoffStopName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Drop-off: ${request.dropoffStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }

                if (!request.message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"${request.message}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            WapiSecondaryButton(
                text = if (isCancelling) "Cancelling…" else stringResource(Res.string.trip_details_cta_cancel_request),
                onClick = onCancelClick,
                enabled = !isCancelling,
                testTag = "trip_details_cancel_request_button"
            )
        }
    }
}

@Composable
private fun ConfirmedBookingCard(
    trip: Trip,
    request: TripRequest?,
    booking: Booking?,
    ratingState: RatingState = RatingState(),
    isLifecycleBusy: Boolean = false,
    onCancelBookingClick: (String) -> Unit = {},
    onRatingStarsSelected: (String, Int) -> Unit = { _, _ -> },
    onRatingReviewTextChanged: (String, String) -> Unit = { _, _ -> },
    onSubmitBookingRatingClick: (String) -> Unit = {},
    onMessageDriverClick: () -> Unit = {}
) {
    val driver = trip.resolvedDriverSummary
    val seatsBooked = booking?.seatsBooked ?: request?.requestedSeats ?: 1
    val pickupName = booking?.pickupStopName ?: request?.pickupStopName
    val dropoffName = booking?.dropoffStopName ?: request?.dropoffStopName
    val rawBookingStatus = booking?.status?.lowercase()?.trim().orEmpty()
    val rawTripStatus = trip.tripStatus.lowercase().trim()

    val isBookingCancelled = rawBookingStatus == "cancelled"
    val isBookingCompleted = rawBookingStatus == "completed" ||
        (!isBookingCancelled && rawTripStatus == "completed")
    val isBookingInProgress = rawBookingStatus == "in_progress" ||
        (!isBookingCancelled && !isBookingCompleted && rawTripStatus == "in_progress")
    val isBookingConfirmed = !isBookingCancelled && !isBookingCompleted && !isBookingInProgress

    val statusTitle = when {
        isBookingCancelled -> stringResource(Res.string.lifecycle_booking_cancelled_title)
        isBookingCompleted -> stringResource(Res.string.lifecycle_trip_completed_title)
        isBookingInProgress -> stringResource(Res.string.lifecycle_trip_in_progress_title)
        else -> stringResource(Res.string.lifecycle_booking_confirmed_title)
    }

    val statusSubtitle = when {
        isBookingCancelled -> stringResource(Res.string.lifecycle_booking_cancelled_body)
        isBookingCompleted -> stringResource(Res.string.lifecycle_trip_completed_body)
        isBookingInProgress -> stringResource(Res.string.lifecycle_trip_in_progress_passenger_body)
        else -> "Your seat is confirmed"
    }

    val badgeText = when {
        isBookingCancelled -> "CANCELLED"
        isBookingCompleted -> "COMPLETED"
        isBookingInProgress -> "IN PROGRESS"
        else -> "CONFIRMED"
    }

    val badgeBg = when {
        isBookingCancelled -> WapiErrorContainer
        isBookingCompleted -> WapiSuccessContainer
        isBookingInProgress -> WapiOrangeTint
        else -> WapiSuccessContainer
    }

    val badgeColor = when {
        isBookingCancelled -> WapiOnErrorContainer
        isBookingCompleted -> WapiSuccess
        isBookingInProgress -> WapiDeepGreen
        else -> WapiSuccess
    }

    // Passengers can only cancel when booking is confirmed and trip has not started
    val canCancelBooking = isBookingConfirmed &&
        !booking?.id.isNullOrBlank() &&
        (rawTripStatus == "published" || rawTripStatus == "full")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_details_confirmed_booking_card"),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiWhite,
        border = BorderStroke(
            1.5.dp,
            if (isBookingCancelled) WapiBorder else WapiPrimaryGreen
        )
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
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeBg,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isBookingCancelled -> Icons.Outlined.Info
                                    isBookingInProgress -> Icons.Outlined.DirectionsCar
                                    else -> Icons.Outlined.CheckCircle
                                },
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isBookingCancelled) WapiOnErrorContainer else WapiDeepGreen,
                            modifier = Modifier.testTag("trip_details_request_status_title")
                        )
                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiSecondaryText,
                            modifier = Modifier.testTag("trip_details_booking_lifecycle_subtitle")
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = badgeColor,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("trip_details_booking_status_badge")
                    )
                }
            }

            if (!booking?.bookingReference.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WapiGreenTint,
                    border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ConfirmationNumber,
                                contentDescription = null,
                                tint = WapiDeepGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Booking reference",
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText
                            )
                        }
                        Text(
                            text = booking?.bookingReference.orEmpty(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier.testTag("trip_details_booking_reference_text")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            BookingDetailRow(label = "Route", value = "${trip.origin} → ${trip.destination}")
            BookingDetailRow(label = "Date & Departure", value = "${trip.departureDate} · ${trip.formattedDepartureTimeShort}")
            BookingDetailRow(label = "Seats booked", value = "$seatsBooked")
            if (!pickupName.isNullOrBlank()) {
                BookingDetailRow(label = "Pickup stop", value = pickupName)
            }
            if (!dropoffName.isNullOrBlank()) {
                BookingDetailRow(label = "Drop-off stop", value = dropoffName)
            }
            BookingDetailRow(label = "Driver", value = driver.displayName)

            if (!isBookingCancelled) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                WapiSecondaryButton(
                    text = "Message Driver",
                    onClick = onMessageDriverClick,
                    enabled = !isLifecycleBusy,
                    testTag = "trip_details_message_driver_button"
                )
            }

            // Phase 4.5: Passenger rates Driver on completed booking
            if (isBookingCompleted && booking != null && booking.id.isNotBlank()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                BookingRatingSectionCard(
                    booking = booking,
                    ratingFormState = ratingState.formForBooking(booking),
                    isPassengerRatingDriver = true,
                    targetPersonName = driver.displayName,
                    isAnyRatingSubmitting = !ratingState.activeSubmittingBookingId.isNullOrBlank(),
                    onRatingStarsSelected = { stars -> onRatingStarsSelected(booking.id, stars) },
                    onRatingReviewTextChanged = { text -> onRatingReviewTextChanged(booking.id, text) },
                    onSubmitRatingClick = { onSubmitBookingRatingClick(booking.id) },
                    useCanonicalTags = true,
                    testTagPrefix = "trip_details_passenger_rating"
                )
            }

            // Cancellation button ONLY for confirmed bookings before trip starts
            if (canCancelBooking) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                WapiSecondaryButton(
                    text = stringResource(Res.string.lifecycle_cta_cancel_booking),
                    onClick = { onCancelBookingClick(booking!!.id) },
                    enabled = !isLifecycleBusy,
                    testTag = "trip_details_cancel_booking_button"
                )
            }
        }
    }
}

@Composable
private fun BookingRatingSectionCard(
    booking: Booking,
    ratingFormState: BookingRatingFormState,
    isPassengerRatingDriver: Boolean,
    targetPersonName: String,
    isAnyRatingSubmitting: Boolean,
    onRatingStarsSelected: (Int) -> Unit,
    onRatingReviewTextChanged: (String) -> Unit,
    onSubmitRatingClick: () -> Unit,
    useCanonicalTags: Boolean,
    testTagPrefix: String
) {
    val existingRating = booking.existingRating ?: ratingFormState.submittedRating
    val isRated = booking.isRatedByCurrentUser || ratingFormState.isRated || existingRating != null
    val rolePrompt = if (isPassengerRatingDriver) {
        stringResource(Res.string.rating_rate_driver)
    } else {
        stringResource(Res.string.rating_rate_passenger)
    }

    val cardTag = if (useCanonicalTags) "trip_details_rate_trip_card" else "${testTagPrefix}_card"
    val titleTag = if (useCanonicalTags) "trip_details_rate_trip_text" else "${testTagPrefix}_title"
    val targetRoleTag = if (useCanonicalTags) "trip_details_rating_target_role" else "${testTagPrefix}_target_role"
    val targetNameTag = if (useCanonicalTags) "trip_details_rating_target_name" else "${testTagPrefix}_target_name"
    val reviewInputTag = if (useCanonicalTags) "rating_review_input" else "${testTagPrefix}_review_input"
    val submitButtonTag = if (useCanonicalTags) "rating_submit_button" else "${testTagPrefix}_submit_button"
    val submittedBadgeTag = if (useCanonicalTags) "rating_submitted_badge" else "${testTagPrefix}_submitted_badge"
    val submittedStarsTag = if (useCanonicalTags) "rating_submitted_stars" else "${testTagPrefix}_submitted_stars"
    val errorBannerTag = if (useCanonicalTags) "rating_error_banner" else "${testTagPrefix}_error_banner"

    Surface(
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiGreenTint,
        border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(cardTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WapiDimens.SpaceMD)
        ) {
            if (isRated) {
                val displayStarsCount = (existingRating?.rating ?: ratingFormState.selectedRating).coerceIn(1, 5)
                val starsString = "★".repeat(displayStarsCount) + "☆".repeat(5 - displayStarsCount)
                val submittedReview = existingRating?.review ?: ratingFormState.reviewText.trim().takeIf { it.isNotEmpty() }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(Res.string.rating_submitted_plain)} $starsString",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.testTag(submittedBadgeTag)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag(submittedStarsTag),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) { idx ->
                                val filled = idx < displayStarsCount
                                Icon(
                                    imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (filled) WapiOrange else WapiMutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$displayStarsCount/5",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${stringResource(Res.string.rating_submitted_badge)} · $rolePrompt · $targetPersonName",
                    style = MaterialTheme.typography.bodySmall,
                    color = WapiSecondaryText,
                    modifier = Modifier.testTag(targetNameTag)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.rating_thank_you_message),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = WapiDeepGreen,
                    modifier = Modifier.testTag(
                        if (useCanonicalTags) "rating_feedback_banner" else "${testTagPrefix}_feedback_banner"
                    )
                )

                if (!submittedReview.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"$submittedReview\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiPrimaryText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            } else {
                val selectedStars = ratingFormState.selectedRating.coerceIn(1, 5)
                val controlsEnabled = !ratingFormState.isSubmitting && !isAnyRatingSubmitting

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = WapiOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.rating_section_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier.testTag(titleTag)
                        )
                    }

                    Text(
                        text = "$selectedStars / 5 ★",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.testTag(
                            if (useCanonicalTags) "rating_selected_stars_label" else "${testTagPrefix}_selected_stars_label"
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = rolePrompt,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = WapiPrimaryText,
                    modifier = Modifier.testTag(targetRoleTag)
                )

                Text(
                    text = targetPersonName,
                    style = MaterialTheme.typography.bodySmall,
                    color = WapiSecondaryText,
                    modifier = Modifier.testTag(targetNameTag)
                )

                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                // 1–5 Star Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        val isSelected = star <= selectedStars
                        val starTag = if (useCanonicalTags) "rating_star_$star" else "${testTagPrefix}_star_$star"
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    enabled = controlsEnabled,
                                    onClick = { onRatingStarsSelected(star) }
                                )
                                .testTag(starTag),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) WapiWhite else WapiSubtleSurface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) WapiOrange else WapiBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$star stars",
                                    tint = if (isSelected) WapiOrange else WapiMutedText,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                WapiTextField(
                    value = ratingFormState.reviewText,
                    onValueChange = onRatingReviewTextChanged,
                    label = stringResource(Res.string.rating_review_placeholder),
                    placeholder = stringResource(Res.string.rating_review_placeholder),
                    enabled = controlsEnabled,
                    leadingIcon = Icons.Outlined.ChatBubbleOutline,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    testTag = reviewInputTag
                )

                if (!ratingFormState.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                    WapiFeedbackBanner(
                        message = ratingFormState.errorMessage,
                        isError = true,
                        testTag = errorBannerTag
                    )
                }

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                WapiPrimaryButton(
                    text = stringResource(Res.string.rating_submit_cta),
                    onClick = onSubmitRatingClick,
                    enabled = controlsEnabled && selectedStars in 1..5,
                    isLoading = ratingFormState.isSubmitting,
                    testTag = submitButtonTag
                )
            }
        }
    }
}

@Composable
private fun BookingDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WapiSecondaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = WapiPrimaryText
        )
    }
}

@Composable
private fun HistoricalRequestStatusBanner(
    status: PassengerRequestStatus
) {
    val label = when (status) {
        PassengerRequestStatus.DECLINED -> stringResource(Res.string.request_state_declined_title)
        PassengerRequestStatus.CANCELLED -> stringResource(Res.string.request_state_cancelled_title)
        PassengerRequestStatus.EXPIRED -> stringResource(Res.string.request_state_expired_title)
        else -> ""
    }
    if (label.isBlank()) return

    val isDeclined = status == PassengerRequestStatus.DECLINED
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_details_historical_status_banner"),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = if (isDeclined) WapiErrorContainer else WapiSubtleSurface,
        border = BorderStroke(
            1.dp,
            if (isDeclined) WapiOnErrorContainer.copy(alpha = 0.25f) else WapiBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = if (isDeclined) WapiOnErrorContainer else WapiSecondaryText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isDeclined) WapiOnErrorContainer else WapiPrimaryText,
                modifier = Modifier.testTag("trip_details_request_status_title")
            )
        }
    }
}

@Composable
private fun RequestSeatFormCard(
    trip: Trip,
    requestState: PassengerSeatRequestState,
    onSeatsChange: (Int) -> Unit,
    onPickupStopSelect: (String?) -> Unit,
    onDropoffStopSelect: (String?) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmitRequestClick: () -> Unit
) {
    val isRideFull = trip.isFullOrUnavailable || !trip.tripStatus.equals("published", ignoreCase = true)
    val maxSeats = trip.availableSeats.coerceAtLeast(1)
    val selectedSeats = requestState.selectedSeats.coerceIn(1, maxSeats)
    val pickupStops = requestState.pickupStops
    val dropoffStops = requestState.dropoffStops

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_details_request_seat_card"),
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
                Text(
                    text = stringResource(Res.string.trip_details_section_request),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WapiDeepGreen
                )

                Text(
                    text = if (isRideFull) {
                        stringResource(Res.string.trip_details_cta_ride_full)
                    } else {
                        "Request a seat"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (isRideFull) WapiError else WapiSecondaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            // Seat Selector Row (1 .. available_seats)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.trip_details_label_seats_to_request),
                        style = MaterialTheme.typography.titleSmall,
                        color = WapiPrimaryText
                    )
                    Text(
                        text = if (isRideFull) {
                            "0 seats available"
                        } else {
                            "Max ${trip.availableSeats} available"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                enabled = !isRideFull && !requestState.isSubmittingRequest && selectedSeats > 1,
                                onClick = { onSeatsChange(selectedSeats - 1) }
                            )
                            .testTag("request_seat_decrement_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = WapiSubtleSurface,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Remove,
                                contentDescription = "Decrease seats",
                                tint = if (!isRideFull && selectedSeats > 1) WapiPrimaryText else WapiMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isRideFull) "0" else "$selectedSeats",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryText,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("request_seat_count_text")
                    )

                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(
                                enabled = !isRideFull && !requestState.isSubmittingRequest && selectedSeats < trip.availableSeats,
                                onClick = { onSeatsChange(selectedSeats + 1) }
                            )
                            .testTag("request_seat_increment_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = WapiSubtleSurface,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Increase seats",
                                tint = if (!isRideFull && selectedSeats < trip.availableSeats) WapiPrimaryText else WapiMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (!requestState.seatsError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = requestState.seatsError,
                    style = MaterialTheme.typography.bodySmall,
                    color = WapiError
                )
            }

            // Optional Pickup & Dropoff Stop selectors (only shown when stops exist for this trip)
            if (pickupStops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                StopSelectorGroup(
                    label = stringResource(Res.string.trip_details_label_pickup_stop),
                    stops = pickupStops,
                    selectedStopId = requestState.selectedPickupStopId,
                    enabled = !isRideFull && !requestState.isSubmittingRequest,
                    onSelectStop = onPickupStopSelect,
                    testTagPrefix = "pickup_stop"
                )
            }

            if (dropoffStops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                StopSelectorGroup(
                    label = stringResource(Res.string.trip_details_label_dropoff_stop),
                    stops = dropoffStops,
                    selectedStopId = requestState.selectedDropoffStopId,
                    enabled = !isRideFull && !requestState.isSubmittingRequest,
                    onSelectStop = onDropoffStopSelect,
                    testTagPrefix = "dropoff_stop"
                )
            }

            if (!isRideFull) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                WapiTextField(
                    value = requestState.message,
                    onValueChange = onMessageChange,
                    label = stringResource(Res.string.trip_details_label_passenger_message),
                    placeholder = stringResource(Res.string.trip_details_placeholder_passenger_message),
                    errorMessage = requestState.messageError,
                    enabled = !requestState.isSubmittingRequest,
                    leadingIcon = Icons.Outlined.ChatBubbleOutline,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    testTag = "request_seat_message_input"
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            WapiPrimaryButton(
                text = if (isRideFull) {
                    stringResource(Res.string.trip_details_cta_ride_full)
                } else {
                    stringResource(Res.string.trip_details_cta_request_seat)
                },
                onClick = onSubmitRequestClick,
                enabled = !isRideFull && !requestState.isSubmittingRequest,
                isLoading = requestState.isSubmittingRequest,
                testTag = "trip_details_request_seat_button"
            )
        }
    }
}

@Composable
private fun StopSelectorGroup(
    label: String,
    stops: List<TripStop>,
    selectedStopId: String?,
    enabled: Boolean,
    onSelectStop: (String?) -> Unit,
    testTagPrefix: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = WapiPrimaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            stops.forEachIndexed { index, stop ->
                val isSelected = selectedStopId == stop.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = enabled) {
                            onSelectStop(if (isSelected) null else stop.id)
                        }
                        .testTag("${testTagPrefix}_option_$index"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) WapiGreenTint else WapiSubtleSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) WapiPrimaryGreen else WapiBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stop.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) WapiDeepGreen else WapiPrimaryText
                        )
                        if (isSelected) {
                            Text(
                                text = "Selected",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSharingFoundationCard(
    trip: Trip,
    contacts: List<WapiEmergencyContact>,
    tripShareState: TripShareState,
    onSelectContact: (String?) -> Unit,
    onShareTripClick: () -> Unit,
    onRevokeShareClick: (String) -> Unit,
    onManageEmergencyContactsClick: () -> Unit
) {
    val activeShares = tripShareState.activeShares
    val selectedContactId = tripShareState.selectedContactId
        ?: contacts.firstOrNull { it.isPrimary }?.id
        ?: contacts.firstOrNull()?.id

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trip_details_trip_share_card"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = WapiDeepGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.trip_share_section_title),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen
                    )
                }

                if (activeShares.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = WapiGreenTint
                    ) {
                        Text(
                            text = "${activeShares.size} Active",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("trip_details_active_share_badge")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            Text(
                text = "Record a trusted trip share for ${trip.origin} → ${trip.destination} (${trip.departureDate} · ${trip.formattedDepartureTimeShort}).",
                style = MaterialTheme.typography.bodySmall,
                color = WapiSecondaryText
            )

            if (!tripShareState.feedbackMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                WapiFeedbackBanner(
                    message = tripShareState.feedbackMessage,
                    isError = false,
                    testTag = "trip_details_share_feedback_banner"
                )
            }

            if (!tripShareState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                WapiFeedbackBanner(
                    message = tripShareState.errorMessage,
                    isError = true,
                    testTag = "trip_details_share_error_banner"
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            if (contacts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select emergency contact",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryText
                    )
                    TextButton(
                        onClick = onManageEmergencyContactsClick,
                        modifier = Modifier.testTag("trip_details_manage_contacts_button")
                    ) {
                        Text(
                            text = "Manage contacts",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    contacts.forEachIndexed { index, contact ->
                        val isSelected = selectedContactId == contact.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = !tripShareState.isCreating) {
                                    onSelectContact(contact.id)
                                }
                                .testTag("trip_details_share_contact_option_$index"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) WapiGreenTint else WapiSubtleSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) WapiPrimaryGreen else WapiBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = contact.effectiveName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = WapiPrimaryText
                                        )
                                        if (contact.isPrimary) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "PRIMARY",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = WapiDeepGreen
                                            )
                                        }
                                    }
                                    Text(
                                        text = buildString {
                                            append(contact.effectivePhone)
                                            contact.effectiveRelationship?.let { append(" · $it") }
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WapiSecondaryText
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Selected contact",
                                        tint = WapiPrimaryGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WapiSubtleSurface,
                    border = BorderStroke(1.dp, WapiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No emergency contacts saved yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiSecondaryText,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = onManageEmergencyContactsClick,
                            modifier = Modifier.testTag("trip_details_add_contact_button")
                        ) {
                            Text(
                                text = "Add contact",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            WapiPrimaryButton(
                text = stringResource(Res.string.trip_share_cta),
                onClick = onShareTripClick,
                isLoading = tripShareState.isCreating,
                enabled = !tripShareState.isCreating,
                testTag = "trip_details_share_trip_button"
            )

            if (activeShares.isNotEmpty()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                Text(
                    text = "Active Trip Shares",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WapiDeepGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeShares.forEachIndexed { index, share ->
                        val isRevoking = tripShareState.revokingShareId == share.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trip_details_active_share_item_$index"),
                            shape = RoundedCornerShape(10.dp),
                            color = WapiGreenTint,
                            border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = share.formattedRecipientSummary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = WapiDeepGreen
                                    )
                                    val code = share.effectiveShareCode
                                    if (code.isNotBlank()) {
                                        Text(
                                            text = "Share ID: ${code.take(16)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WapiMutedText
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { onRevokeShareClick(share.id) },
                                    enabled = !isRevoking,
                                    modifier = Modifier.testTag("trip_details_revoke_share_button_$index")
                                ) {
                                    Text(
                                        text = if (isRevoking) "Revoking…" else "Stop sharing",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = WapiError
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

