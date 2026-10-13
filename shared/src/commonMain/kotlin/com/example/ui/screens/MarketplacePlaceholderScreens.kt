package com.example.ui.screens

import com.example.platform.formatIsoDateUtc
import com.example.platform.currentHourOfDay
import com.example.platform.formatClockTime
import com.example.platform.currentTimeMillis
import com.example.resources.Res
import com.example.resources.coming_soon_badge
import com.example.resources.cta_find_rides
import com.example.resources.cta_sign_out
import com.example.resources.driver_request_accept
import com.example.resources.driver_request_confirmed_badge
import com.example.resources.driver_request_decline
import com.example.resources.driver_request_declined_badge
import com.example.resources.find_ride_empty_body
import com.example.resources.find_ride_empty_title
import com.example.resources.find_ride_prompt_body
import com.example.resources.find_ride_prompt_title
import com.example.resources.find_ride_results_header
import com.example.resources.find_ride_subtitle
import com.example.resources.find_ride_title
import com.example.resources.label_date
import com.example.resources.label_from
import com.example.resources.label_full_name
import com.example.resources.label_phone
import com.example.resources.label_time
import com.example.resources.label_to
import com.example.resources.placeholder_date
import com.example.resources.placeholder_from
import com.example.resources.placeholder_full_name
import com.example.resources.placeholder_phone
import com.example.resources.placeholder_time
import com.example.resources.placeholder_to
import com.example.resources.profile_bio_label
import com.example.resources.profile_bio_placeholder
import com.example.resources.profile_cancel_button
import com.example.resources.profile_completed_trips_label
import com.example.resources.profile_completed_trips_placeholder
import com.example.resources.profile_edit_button
import com.example.resources.profile_rating_label
import com.example.resources.profile_rating_placeholder
import com.example.resources.profile_save_button
import com.example.resources.profile_section_account
import com.example.resources.profile_section_notifications
import com.example.resources.profile_section_safety
import com.example.resources.profile_section_settings
import com.example.resources.profile_section_vehicles
import com.example.resources.profile_section_verification
import com.example.resources.profile_title
import com.example.resources.rating_status_not_rated
import com.example.resources.rating_status_rated
import com.example.resources.trips_empty_active_body
import com.example.resources.trips_empty_active_title
import com.example.resources.trips_empty_cancelled_body
import com.example.resources.trips_empty_cancelled_title
import com.example.resources.trips_empty_completed_body
import com.example.resources.trips_empty_completed_title
import com.example.resources.trips_empty_requests_body
import com.example.resources.trips_empty_requests_title
import com.example.resources.trips_empty_upcoming_body
import com.example.resources.trips_empty_upcoming_title
import com.example.resources.trips_tab_active
import com.example.resources.trips_tab_cancelled
import com.example.resources.trips_tab_completed
import com.example.resources.trips_tab_requests
import com.example.resources.trips_tab_upcoming
import com.example.resources.trips_title
import com.example.platform.rememberImagePickerLauncher
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.FmdGood
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.OverallVerificationStatus
import com.example.data.Trip
import com.example.data.TripRequest
import com.example.data.UserProfile
import com.example.ui.components.WapiEmptyStateSection
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
import com.example.ui.viewmodel.ProfileEditFormState
import com.example.ui.viewmodel.SafetyTrustState
import com.example.ui.theme.WapiSuccessContainer
import com.example.ui.theme.WapiWhite
import com.example.ui.viewmodel.DriverRequestsState
import com.example.ui.viewmodel.DriverRequestsStatus
import com.example.ui.viewmodel.FindRideSearchState
import com.example.ui.viewmodel.FindRideSearchStatus
import com.example.ui.viewmodel.RequestDecisionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRideScreen(
    searchState: FindRideSearchState = FindRideSearchState(),
    onOriginChange: (String) -> Unit = {},
    onDestinationChange: (String) -> Unit = {},
    onDateSelected: (String) -> Unit = {},
    onPreferredTimeSelected: (String) -> Unit = {},
    onClearPreferredTime: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onTripClick: (Trip) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("find_ride_screen"),
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
            Text(
                text = stringResource(Res.string.find_ride_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("find_ride_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.find_ride_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            // Search Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(WapiDimens.RadiusMD),
                color = WapiWhite,
                border = BorderStroke(1.dp, WapiBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WapiDimens.SpaceLG)
                ) {
                    WapiTextField(
                        value = searchState.origin,
                        onValueChange = onOriginChange,
                        label = stringResource(Res.string.label_from),
                        placeholder = stringResource(Res.string.placeholder_from),
                        errorMessage = searchState.originError,
                        enabled = !searchState.isSearching,
                        leadingIcon = Icons.Outlined.MyLocation,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "find_ride_from_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    WapiTextField(
                        value = searchState.destination,
                        onValueChange = onDestinationChange,
                        label = stringResource(Res.string.label_to),
                        placeholder = stringResource(Res.string.placeholder_to),
                        errorMessage = searchState.destinationError,
                        enabled = !searchState.isSearching,
                        leadingIcon = Icons.Outlined.FmdGood,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "find_ride_to_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                    ) {
                        FindRidePickerBox(
                            label = stringResource(Res.string.label_date),
                            value = searchState.departureDate,
                            placeholder = stringResource(Res.string.placeholder_date),
                            icon = Icons.Outlined.CalendarToday,
                            errorMessage = searchState.departureDateError,
                            enabled = !searchState.isSearching,
                            onClick = {
                                focusManager.clearFocus()
                                showDatePicker = true
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "find_ride_date_picker_field"
                        )

                        FindRidePickerBox(
                            label = stringResource(Res.string.label_time),
                            value = if (searchState.preferredTime.length >= 5) {
                                searchState.preferredTime.substring(0, 5)
                            } else {
                                searchState.preferredTime
                            },
                            placeholder = stringResource(Res.string.placeholder_time),
                            icon = Icons.Outlined.AccessTime,
                            errorMessage = searchState.preferredTimeError,
                            enabled = !searchState.isSearching,
                            onClearClick = if (searchState.preferredTime.isNotBlank()) {
                                onClearPreferredTime
                            } else null,
                            onClick = {
                                focusManager.clearFocus()
                                showTimePicker = true
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "find_ride_time_picker_field"
                        )
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

                    WapiPrimaryButton(
                        text = stringResource(Res.string.cta_find_rides),
                        onClick = {
                            focusManager.clearFocus()
                            onSearchClick()
                        },
                        isLoading = searchState.isSearching,
                        enabled = !searchState.isSearching,
                        testTag = "find_rides_submit_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            // Search States & Results Section
            when (searchState.searchStatus) {
                FindRideSearchStatus.IDLE -> {
                    WapiEmptyStateSection(
                        icon = Icons.Outlined.Search,
                        title = stringResource(Res.string.find_ride_prompt_title),
                        description = stringResource(Res.string.find_ride_prompt_body),
                        testTag = "find_ride_idle_prompt"
                    )
                }

                FindRideSearchStatus.LOADING -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("find_ride_loading_state"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp,
                                color = WapiPrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Searching published rides…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                FindRideSearchStatus.ERROR -> {
                    WapiFeedbackBanner(
                        message = searchState.errorMessage
                            ?: "Unable to search rides right now. Please try again.",
                        isError = true,
                        testTag = "find_ride_error_banner"
                    )
                }

                FindRideSearchStatus.EMPTY -> {
                    WapiEmptyStateSection(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(Res.string.find_ride_empty_title),
                        description = stringResource(Res.string.find_ride_empty_body),
                        badgeText = "${searchState.origin.trim()} → ${searchState.destination.trim()}",
                        testTag = "find_ride_empty_results"
                    )
                }

                FindRideSearchStatus.RESULTS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.find_ride_results_header),
                            style = MaterialTheme.typography.titleLarge,
                            color = WapiPrimaryText,
                            modifier = Modifier.testTag("find_ride_results_header")
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WapiGreenTint
                        ) {
                            val count = searchState.results.size
                            Text(
                                text = if (count == 1) "1 ride found" else "$count rides found",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("find_ride_results_count_badge")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                        modifier = Modifier.testTag("find_ride_results_list")
                    ) {
                        searchState.results.forEachIndexed { index, trip ->
                            WapiRideResultCard(
                                trip = trip,
                                onClick = { onTripClick(trip) },
                                testTag = "find_ride_result_item_$index"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis ?: currentTimeMillis()
                            onDateSelected(formatIsoDateUtc(millis))
                            showDatePicker = false
                        },
                        modifier = Modifier.testTag("find_date_picker_confirm_button")
                    ) {
                        Text(
                            text = "Confirm",
                            color = WapiPrimaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(text = "Cancel", color = WapiSecondaryText)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
                        val timePickerState = rememberTimePickerState(
                initialHour = currentHourOfDay(),
                initialMinute = 0,
                is24Hour = true
            )
            BasicAlertDialog(
                onDismissRequest = { showTimePicker = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(WapiDimens.RadiusLG),
                    color = WapiWhite,
                    modifier = Modifier.padding(WapiDimens.SpaceMD)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WapiDimens.SpaceLG),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Select preferred departure time",
                            style = MaterialTheme.typography.titleMedium,
                            color = WapiPrimaryText,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                        TimePicker(state = timePickerState)
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    onClearPreferredTime()
                                    showTimePicker = false
                                }
                            ) {
                                Text(text = "Any time", color = WapiSecondaryText)
                            }
                            Row {
                                TextButton(onClick = { showTimePicker = false }) {
                                    Text(text = "Cancel", color = WapiSecondaryText)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                TextButton(
                                    onClick = {
                                        val formatted = formatClockTime(timePickerState.hour, timePickerState.minute)
                                        onPreferredTimeSelected(formatted)
                                        showTimePicker = false
                                    },
                                    modifier = Modifier.testTag("find_time_picker_confirm_button")
                                ) {
                                    Text(
                                        text = "Confirm",
                                        color = WapiPrimaryGreen,
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun FindRidePickerBox(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    errorMessage: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    onClearClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = WapiPrimaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(WapiDimens.InputFieldMinHeight)
                .clip(RoundedCornerShape(WapiDimens.RadiusMD))
                .clickable(enabled = enabled, onClick = onClick)
                .testTag(testTag),
            shape = RoundedCornerShape(WapiDimens.RadiusMD),
            color = if (enabled) WapiWhite else WapiWhite.copy(alpha = 0.6f),
            border = BorderStroke(
                width = 1.dp,
                color = if (errorMessage != null) WapiError else WapiBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = if (onClearClick != null) 4.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (errorMessage != null) WapiError else WapiSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value.ifBlank { placeholder },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isNotBlank()) WapiPrimaryText else WapiMutedText,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (onClearClick != null) {
                    IconButton(
                        onClick = onClearClick,
                        enabled = enabled,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("${testTag}_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear preferred time",
                            tint = WapiMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = WapiError,
                modifier = Modifier.testTag("${testTag}_error")
            )
        }
    }
}

sealed interface TripsScheduleItem {
    data class DriverTripItem(val trip: Trip) : TripsScheduleItem
    data class PassengerBookedTripItem(
        val booking: com.example.data.Booking,
        val trip: Trip?
    ) : TripsScheduleItem
}

@Composable
fun TripsScreen(
    trips: List<Trip> = emptyList(),
    passengerBookings: List<com.example.data.Booking> = emptyList(),
    driverCompletedBookings: List<com.example.data.Booking> = emptyList(),
    ratingState: com.example.ui.viewmodel.RatingState = com.example.ui.viewmodel.RatingState(),
    driverRequestsState: DriverRequestsState = DriverRequestsState(),
    selectedTabIndex: Int = 0,
    onSelectTabIndex: (Int) -> Unit = {},
    onAcceptRequest: (String) -> Unit = {},
    onDeclineRequest: (String) -> Unit = {},
    onTripClick: (Trip) -> Unit = {},
    onBookingTripClick: ((com.example.data.Booking) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val pendingRequestCount = driverRequestsState.requests.count {
        it.status.equals("pending", ignoreCase = true)
    }

    val requestsTabLabel = if (pendingRequestCount > 0) {
        "${stringResource(Res.string.trips_tab_requests)} ($pendingRequestCount)"
    } else {
        stringResource(Res.string.trips_tab_requests)
    }

    val tabs = listOf(
        stringResource(Res.string.trips_tab_upcoming),
        requestsTabLabel,
        stringResource(Res.string.trips_tab_active),
        stringResource(Res.string.trips_tab_completed),
        stringResource(Res.string.trips_tab_cancelled)
    )

    val tripsById = remember(trips) { trips.associateBy { it.id } }

    val passengerBookingItemsByStatus: (String) -> List<TripsScheduleItem.PassengerBookedTripItem> = { targetBookingStatus ->
        passengerBookings
            .filter { booking ->
                booking.status.equals(targetBookingStatus, ignoreCase = true) &&
                    (booking.passengerId.isBlank() || booking.passengerId != booking.driverId)
            }
            .distinctBy { booking ->
                booking.id.ifBlank { "${booking.tripId}:${booking.passengerId}:${booking.status}" }
            }
            .map { booking ->
                TripsScheduleItem.PassengerBookedTripItem(
                    booking = booking,
                    trip = booking.tripSummary ?: tripsById[booking.tripId]
                )
            }
    }

    val upcomingDriverItems = trips
        .filter {
            it.tripStatus.equals("published", ignoreCase = true) ||
                it.tripStatus.equals("full", ignoreCase = true) ||
                it.tripStatus.equals("draft", ignoreCase = true)
        }
        .distinctBy { it.id }
        .map { TripsScheduleItem.DriverTripItem(it) }
    val upcomingPassengerItems = passengerBookingItemsByStatus("confirmed")
    val upcomingItems: List<TripsScheduleItem> = upcomingDriverItems + upcomingPassengerItems

    val activeDriverItems = trips
        .filter {
            it.tripStatus.equals("in_progress", ignoreCase = true)
        }
        .distinctBy { it.id }
        .map { TripsScheduleItem.DriverTripItem(it) }
    val activePassengerItems = passengerBookingItemsByStatus("in_progress")
    val activeItems: List<TripsScheduleItem> = activeDriverItems + activePassengerItems

    val completedDriverTrips = (
        trips.filter {
            it.tripStatus.equals("completed", ignoreCase = true)
        } + driverCompletedBookings.mapNotNull { it.tripSummary }
        ).distinctBy { it.id }
    val completedDriverItems = completedDriverTrips.map { TripsScheduleItem.DriverTripItem(it) }
    val completedPassengerItems = passengerBookingItemsByStatus("completed")
    val completedItems: List<TripsScheduleItem> = completedDriverItems + completedPassengerItems

    val cancelledDriverTrips = (
        trips.filter {
            it.tripStatus.equals("cancelled", ignoreCase = true)
        } + driverRequestsState.requests.mapNotNull { req ->
            val b = req.booking
            if (b != null && b.status.equals("cancelled", ignoreCase = true)) {
                req.tripSummary
            } else null
        }
        ).distinctBy { it.id }
    val cancelledDriverItems = cancelledDriverTrips.map { TripsScheduleItem.DriverTripItem(it) }
    val cancelledPassengerItems = passengerBookingItemsByStatus("cancelled")
    val cancelledItems: List<TripsScheduleItem> = cancelledDriverItems + cancelledPassengerItems

    val ratedBadgeLabel = stringResource(Res.string.rating_status_rated)
    val notRatedBadgeLabel = stringResource(Res.string.rating_status_not_rated)

    val isBookingRated: (com.example.data.Booking) -> Boolean = { booking ->
        booking.isRatedByCurrentUser || ratingState.formForBooking(booking).isRated
    }

    val resolveCompletedDriverTripRatingBadge: (Trip) -> Pair<String, Boolean> = { trip ->
        val matchingDriverBookings = driverCompletedBookings.filter {
            it.tripId == trip.id && it.isCompleted
        } + driverRequestsState.requests.mapNotNull { req ->
            req.booking?.takeIf { it.tripId == trip.id && it.isCompleted }
        }
        val relevantBookings = matchingDriverBookings.distinctBy { it.id }

        val rated = relevantBookings.isNotEmpty() && relevantBookings.all { isBookingRated(it) }
        if (rated) {
            ratedBadgeLabel to true
        } else {
            notRatedBadgeLabel to false
        }
    }

    val resolveCompletedPassengerBookingRatingBadge: (com.example.data.Booking) -> Pair<String, Boolean> = { booking ->
        if (isBookingRated(booking)) {
            ratedBadgeLabel to true
        } else {
            notRatedBadgeLabel to false
        }
    }

    val handlePassengerBookingClick: (com.example.data.Booking, Trip?) -> Unit = { booking, resolvedTrip ->
        if (onBookingTripClick != null) {
            onBookingTripClick(booking)
        } else {
            val targetTrip = resolvedTrip ?: Trip(
                id = booking.tripId,
                driverId = booking.driverId,
                origin = "",
                destination = "",
                departureDate = "",
                departureTime = "",
                totalSeats = booking.seatsBooked.coerceAtLeast(1),
                availableSeats = 0
            )
            onTripClick(targetTrip)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("trips_screen"),
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
            Text(
                text = stringResource(Res.string.trips_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("trips_title")
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            // Clean Segmented Control (Upcoming | Requests | Active | Completed | Cancelled)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(WapiDimens.RadiusMD),
                color = WapiSubtleSurface,
                border = BorderStroke(1.dp, WapiBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        val isSelected = selectedTabIndex == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(WapiDimens.RadiusSM))
                                .clickable { onSelectTabIndex(index) }
                                .testTag("trips_tab_$index"),
                            shape = RoundedCornerShape(WapiDimens.RadiusSM),
                            color = if (isSelected) WapiWhite else WapiSubtleSurface
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) WapiDeepGreen else WapiSecondaryText,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            when (selectedTabIndex) {
                0 -> {
                    if (upcomingItems.isEmpty()) {
                        WapiEmptyStateSection(
                            icon = Icons.Outlined.Map,
                            title = stringResource(Res.string.trips_empty_upcoming_title),
                            description = stringResource(Res.string.trips_empty_upcoming_body),
                            badgeText = stringResource(Res.string.trips_tab_upcoming),
                            testTag = "trips_upcoming_empty_state"
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                            modifier = Modifier.testTag("trips_upcoming_list")
                        ) {
                            upcomingItems.forEachIndexed { index, item ->
                                when (item) {
                                    is TripsScheduleItem.DriverTripItem -> {
                                        WapiTripSummaryCard(
                                            trip = item.trip,
                                            roleLabelText = "DRIVING",
                                            onClick = { onTripClick(item.trip) },
                                            testTag = "trip_item_upcoming_$index"
                                        )
                                    }
                                    is TripsScheduleItem.PassengerBookedTripItem -> {
                                        PassengerBookedTripCard(
                                            booking = item.booking,
                                            trip = item.trip,
                                            statusBadgeText = "CONFIRMED SEAT",
                                            onClick = { handlePassengerBookingClick(item.booking, item.trip) },
                                            testTag = "trip_item_upcoming_$index"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    DriverRequestsTabContent(
                        driverRequestsState = driverRequestsState,
                        onAcceptRequest = onAcceptRequest,
                        onDeclineRequest = onDeclineRequest
                    )
                }
                2 -> {
                    if (activeItems.isEmpty()) {
                        WapiEmptyStateSection(
                            icon = Icons.Outlined.DirectionsCar,
                            title = stringResource(Res.string.trips_empty_active_title),
                            description = stringResource(Res.string.trips_empty_active_body),
                            badgeText = stringResource(Res.string.trips_tab_active),
                            testTag = "trips_active_empty_state"
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                            modifier = Modifier.testTag("trips_active_list")
                        ) {
                            activeItems.forEachIndexed { index, item ->
                                when (item) {
                                    is TripsScheduleItem.DriverTripItem -> {
                                        WapiTripSummaryCard(
                                            trip = item.trip,
                                            roleLabelText = "DRIVING",
                                            onClick = { onTripClick(item.trip) },
                                            testTag = "trip_item_active_$index"
                                        )
                                    }
                                    is TripsScheduleItem.PassengerBookedTripItem -> {
                                        PassengerBookedTripCard(
                                            booking = item.booking,
                                            trip = item.trip,
                                            statusBadgeText = "IN PROGRESS",
                                            onClick = { handlePassengerBookingClick(item.booking, item.trip) },
                                            testTag = "trip_item_active_$index"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    if (completedItems.isEmpty()) {
                        WapiEmptyStateSection(
                            icon = Icons.Outlined.Map,
                            title = stringResource(Res.string.trips_empty_completed_title),
                            description = stringResource(Res.string.trips_empty_completed_body),
                            badgeText = stringResource(Res.string.trips_tab_completed),
                            testTag = "trips_completed_empty_state"
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                            modifier = Modifier.testTag("trips_completed_list")
                        ) {
                            completedItems.forEachIndexed { index, item ->
                                when (item) {
                                    is TripsScheduleItem.DriverTripItem -> {
                                        val (ratingBadgeText, isRated) = resolveCompletedDriverTripRatingBadge(item.trip)
                                        WapiTripSummaryCard(
                                            trip = item.trip,
                                            roleLabelText = "DRIVING",
                                            onClick = { onTripClick(item.trip) },
                                            ratingStatusText = ratingBadgeText,
                                            isRatedBadge = isRated,
                                            testTag = "trip_item_completed_$index"
                                        )
                                    }
                                    is TripsScheduleItem.PassengerBookedTripItem -> {
                                        val (ratingBadgeText, isRated) = resolveCompletedPassengerBookingRatingBadge(item.booking)
                                        PassengerBookedTripCard(
                                            booking = item.booking,
                                            trip = item.trip,
                                            statusBadgeText = "COMPLETED",
                                            ratingStatusText = ratingBadgeText,
                                            isRatedBadge = isRated,
                                            onClick = { handlePassengerBookingClick(item.booking, item.trip) },
                                            testTag = "trip_item_completed_$index"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    if (cancelledItems.isEmpty()) {
                        WapiEmptyStateSection(
                            icon = Icons.Outlined.Map,
                            title = stringResource(Res.string.trips_empty_cancelled_title),
                            description = stringResource(Res.string.trips_empty_cancelled_body),
                            badgeText = stringResource(Res.string.trips_tab_cancelled),
                            testTag = "trips_cancelled_empty_state"
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                            modifier = Modifier.testTag("trips_cancelled_list")
                        ) {
                            cancelledItems.forEachIndexed { index, item ->
                                when (item) {
                                    is TripsScheduleItem.DriverTripItem -> {
                                        WapiTripSummaryCard(
                                            trip = item.trip,
                                            roleLabelText = "DRIVING",
                                            onClick = { onTripClick(item.trip) },
                                            testTag = "trip_item_cancelled_$index"
                                        )
                                    }
                                    is TripsScheduleItem.PassengerBookedTripItem -> {
                                        PassengerBookedTripCard(
                                            booking = item.booking,
                                            trip = item.trip,
                                            statusBadgeText = "BOOKING CANCELLED",
                                            onClick = { handlePassengerBookingClick(item.booking, item.trip) },
                                            testTag = "trip_item_cancelled_$index"
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
}

@Composable
fun PassengerBookedTripCard(
    booking: com.example.data.Booking,
    trip: Trip?,
    statusBadgeText: String,
    modifier: Modifier = Modifier,
    ratingStatusText: String? = null,
    isRatedBadge: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = "passenger_booked_trip_card"
) {
    val isCancelled = booking.isCancelled || statusBadgeText.equals("BOOKING CANCELLED", ignoreCase = true)
    val isInProgress = booking.isInProgress || statusBadgeText.equals("IN PROGRESS", ignoreCase = true)

    val badgeBgColor = when {
        isCancelled -> WapiErrorContainer
        isInProgress -> WapiOrangeTint
        else -> WapiSuccessContainer
    }
    val badgeTextColor = when {
        isCancelled -> WapiOnErrorContainer
        isInProgress -> WapiDeepGreen
        else -> WapiSuccess
    }

    val seatsBookedCount = booking.seatsBooked.coerceAtLeast(1)
    val seatsBookedLabel = if (seatsBookedCount == 1) {
        "1 seat booked"
    } else {
        "$seatsBookedCount seats booked"
    }

    val contributionText: String? = remember(trip, seatsBookedCount) {
        val perSeat = trip?.contributionAmount
        when {
            perSeat != null -> {
                val totalAmount = perSeat * seatsBookedCount
                if (totalAmount % 1.0 == 0.0) {
                    "KES ${totalAmount.toLong()}"
                } else {
                    "KES $totalAmount"
                }
            }
            trip != null -> trip.formattedContribution
            else -> null
        }
    }

    val driverSummary = trip?.driverSummary

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
        border = BorderStroke(
            width = if (isCancelled) 1.dp else 1.5.dp,
            color = if (isCancelled) WapiBorder else WapiPrimaryGreen.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WapiDimens.SpaceLG)
        ) {
            // Top Row: Passenger Booking Status Badge + Optional Rating Badge + Contribution Total
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
                        color = badgeBgColor
                    ) {
                        Text(
                            text = statusBadgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = badgeTextColor,
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

                if (!contributionText.isNullOrBlank()) {
                    Text(
                        text = contributionText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.testTag("${testTag}_contribution")
                    )
                }
            }

            // Driver Identity Row (when driverSummary is available on trip)
            if (driverSummary != null && driverSummary.fullName.isNotBlank()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WapiUserAvatar(
                        avatarUrl = driverSummary.avatarUrl,
                        initials = driverSummary.initials,
                        size = 36.dp,
                        contentDescription = "${driverSummary.displayName} avatar",
                        testTag = "${testTag}_driver_avatar",
                        remoteImageTestTag = "${testTag}_driver_avatar_remote_image"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = driverSummary.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("${testTag}_driver_name")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = WapiOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = driverSummary.formattedRating,
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText,
                                modifier = Modifier.testTag("${testTag}_driver_rating")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            // Route or Safe Fallback when tripSummary is temporarily unavailable
            if (trip != null && trip.origin.isNotBlank() && trip.destination.isNotBlank()) {
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
            } else {
                Text(
                    text = when {
                        isCancelled -> "Cancelled booking"
                        booking.isCompleted -> "Completed booking"
                        isInProgress -> "Booking in progress"
                        else -> "Confirmed booking"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = WapiPrimaryText,
                    modifier = Modifier.testTag("${testTag}_fallback_title")
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            // Schedule (if trip available) + Seats Booked
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (trip != null && trip.departureDate.isNotBlank()) {
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
                            color = WapiSecondaryText,
                            modifier = Modifier.testTag("${testTag}_schedule")
                        )
                    }
                } else {
                    Text(
                        text = "Tap to view trip details",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText,
                        modifier = Modifier.testTag("${testTag}_fallback_hint")
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
                        text = seatsBookedLabel,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.testTag("${testTag}_seats_booked")
                    )
                }
            }

            // Subtle Pickup / Drop-off rows when available on the booking
            if (!booking.pickupStopName.isNullOrBlank() || !booking.dropoffStopName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                if (!booking.pickupStopName.isNullOrBlank()) {
                    Text(
                        text = "Pickup: ${booking.pickupStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText,
                        modifier = Modifier.testTag("${testTag}_pickup_stop")
                    )
                }
                if (!booking.dropoffStopName.isNullOrBlank()) {
                    Text(
                        text = "Drop-off: ${booking.dropoffStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText,
                        modifier = Modifier.testTag("${testTag}_dropoff_stop")
                    )
                }
            }

            // Booking Reference row when available
            if (booking.bookingReference.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WapiGreenTint,
                    border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ConfirmationNumber,
                                contentDescription = null,
                                tint = WapiDeepGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ref: ${booking.bookingReference}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier.testTag("${testTag}_booking_ref")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverRequestsTabContent(
    driverRequestsState: DriverRequestsState,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!driverRequestsState.feedbackBannerMessage.isNullOrBlank()) {
            WapiFeedbackBanner(
                message = driverRequestsState.feedbackBannerMessage,
                isError = false,
                testTag = "driver_requests_feedback_banner"
            )
            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
        }

        if (!driverRequestsState.errorMessage.isNullOrBlank()) {
            WapiFeedbackBanner(
                message = driverRequestsState.errorMessage,
                isError = true,
                testTag = "driver_requests_error_banner"
            )
            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
        }

        when (driverRequestsState.status) {
            DriverRequestsStatus.LOADING -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_requests_loading_state"),
                    shape = RoundedCornerShape(WapiDimens.RadiusMD),
                    color = WapiWhite,
                    border = BorderStroke(1.dp, WapiBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                            color = WapiPrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading incoming ride requests…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WapiSecondaryText
                        )
                    }
                }
            }

            DriverRequestsStatus.EMPTY,
            DriverRequestsStatus.ERROR -> {
                if (driverRequestsState.requests.isEmpty()) {
                    WapiEmptyStateSection(
                        icon = Icons.Outlined.Inbox,
                        title = stringResource(Res.string.trips_empty_requests_title),
                        description = stringResource(Res.string.trips_empty_requests_body),
                        badgeText = stringResource(Res.string.trips_tab_requests),
                        testTag = "driver_requests_empty_state"
                    )
                }
            }

            DriverRequestsStatus.RESULTS -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                    modifier = Modifier.testTag("driver_requests_list")
                ) {
                    driverRequestsState.requests.forEachIndexed { index, request ->
                        val isThisRequestActive =
                            driverRequestsState.activeDecisionRequestId == request.id &&
                                driverRequestsState.isProcessingDecision
                        val isAcceptLoading = isThisRequestActive &&
                            driverRequestsState.acceptanceStatus == RequestDecisionStatus.LOADING
                        val isDeclineLoading = isThisRequestActive &&
                            driverRequestsState.declineStatus == RequestDecisionStatus.LOADING

                        DriverTripRequestCard(
                            request = request,
                            isDecisionDisabled = driverRequestsState.isProcessingDecision,
                            isAcceptLoading = isAcceptLoading,
                            isDeclineLoading = isDeclineLoading,
                            onAcceptClick = { onAcceptRequest(request.id) },
                            onDeclineClick = { onDeclineRequest(request.id) },
                            testTagPrefix = "driver_request_item_$index"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverTripRequestCard(
    request: TripRequest,
    isDecisionDisabled: Boolean,
    isAcceptLoading: Boolean,
    isDeclineLoading: Boolean,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
    testTagPrefix: String
) {
    val passenger = request.resolvedPassengerSummary
    val trip = request.tripSummary
    val statusLower = request.status.lowercase()
    val isPending = statusLower == "pending"
    val isAccepted = statusLower == "accepted"
    val isDeclined = statusLower == "declined"

    val statusBadgeText = when {
        isAccepted -> stringResource(Res.string.driver_request_confirmed_badge)
        isDeclined -> stringResource(Res.string.driver_request_declined_badge)
        else -> request.status.uppercase()
    }
    val statusBadgeBg = when {
        isAccepted -> WapiSuccessContainer
        isDeclined -> WapiErrorContainer
        else -> WapiOrangeTint
    }
    val statusBadgeTextColor = when {
        isAccepted -> WapiSuccess
        isDeclined -> WapiOnErrorContainer
        else -> WapiDeepGreen
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagPrefix),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiWhite,
        border = BorderStroke(1.dp, WapiBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WapiDimens.SpaceLG)
        ) {
            // Passenger identity + Trust + Request Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    WapiUserAvatar(
                        avatarUrl = passenger.avatarUrl,
                        initials = passenger.initials,
                        size = 44.dp,
                        contentDescription = "${passenger.displayName} avatar",
                        testTag = "${testTagPrefix}_passenger_avatar",
                        remoteImageTestTag = "${testTagPrefix}_passenger_avatar_remote_image"
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = passenger.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("${testTagPrefix}_passenger_name")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = null,
                                tint = WapiPrimaryGreen,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = WapiOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${passenger.formattedRating} · ${passenger.formattedCompletedTrips}",
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

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
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("${testTagPrefix}_status_badge")
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            // Route & Schedule summary
            if (trip != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = trip.origin,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryText
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = WapiPrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trip.destination,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trip.departureDate} · ${trip.formattedDepartureTimeShort} (${trip.availableSeats}/${trip.totalSeats} seats left)",
                    style = MaterialTheme.typography.bodySmall,
                    color = WapiSecondaryText
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
            }

            // Requested seats & timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.EventSeat,
                        contentDescription = null,
                        tint = WapiDeepGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (request.requestedSeats == 1) "1 seat requested" else "${request.requestedSeats} seats requested",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.testTag("${testTagPrefix}_seats_requested")
                    )
                }

                Text(
                    text = request.formattedCreatedAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = WapiMutedText
                )
            }

            // Pickup & Drop-off stops (if selected)
            if (!request.pickupStopName.isNullOrBlank() || !request.dropoffStopName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                if (!request.pickupStopName.isNullOrBlank()) {
                    Text(
                        text = "Pickup: ${request.pickupStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }
                if (!request.dropoffStopName.isNullOrBlank()) {
                    Text(
                        text = "Drop-off: ${request.dropoffStopName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }
            }

            // Passenger message (if provided)
            if (!request.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WapiSubtleSurface,
                    border = BorderStroke(1.dp, WapiBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${request.message}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiPrimaryText,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("${testTagPrefix}_message")
                    )
                }
            }

            // Booking reference display when accepted
            val bookingRef = request.booking?.bookingReference
            if (isAccepted && !bookingRef.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WapiGreenTint,
                    border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.ConfirmationNumber,
                                contentDescription = null,
                                tint = WapiDeepGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Booking reference",
                                style = MaterialTheme.typography.labelSmall,
                                color = WapiSecondaryText
                            )
                        }
                        Text(
                            text = bookingRef,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier.testTag("${testTagPrefix}_booking_ref")
                        )
                    }
                }
            }

            // Decision Buttons (Accept / Decline) for pending requests
            if (isPending) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                ) {
                    WapiSecondaryButton(
                        text = if (isDeclineLoading) "Declining…" else stringResource(Res.string.driver_request_decline),
                        onClick = onDeclineClick,
                        enabled = !isDecisionDisabled,
                        modifier = Modifier.weight(1f),
                        testTag = "${testTagPrefix}_decline_button"
                    )

                    WapiPrimaryButton(
                        text = stringResource(Res.string.driver_request_accept),
                        onClick = onAcceptClick,
                        enabled = !isDecisionDisabled,
                        isLoading = isAcceptLoading,
                        modifier = Modifier.weight(1f),
                        testTag = "${testTagPrefix}_accept_button"
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    unreadNotificationCount: Int = 0,
    safetyTrustState: SafetyTrustState = SafetyTrustState(),
    onAvatarImageSelected: (ByteArray, String?) -> Unit = { _, _ -> },
    onStartEditProfileClick: () -> Unit = {},
    onCancelEditProfileClick: () -> Unit = {},
    onEditFullNameChange: (String) -> Unit = {},
    onEditPhoneChange: (String) -> Unit = {},
    onEditBioChange: (String) -> Unit = {},
    onSaveProfileClick: () -> Unit = {},
    onVehiclesClick: () -> Unit = {},
    onVerificationClick: () -> Unit = {},
    onSafetyContactsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val fullName = userProfile?.fullName?.takeIf { it.isNotBlank() } ?: "Wapi Member"
    val email = userProfile?.email?.takeIf { it.isNotBlank() } ?: "—"
    val phone = userProfile?.phone?.takeIf { it.isNotBlank() } ?: "—"
    val bio = userProfile?.bio?.trim()?.takeIf { it.isNotEmpty() }
    val accountStatus = userProfile?.formattedAccountStatus ?: "Active"
    val profileEdit: ProfileEditFormState = safetyTrustState.profileEdit
    val avatarSelection = safetyTrustState.avatarSelection

    val launchPhotoPicker = rememberImagePickerLauncher { bytes, mimeType ->
        onAvatarImageSelected(bytes, mimeType)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("profile_screen"),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.profile_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = WapiPrimaryText,
                    modifier = Modifier.testTag("profile_title")
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WapiGreenTint
                ) {
                    Text(
                        text = accountStatus,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("profile_account_status_badge")
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!avatarSelection.avatarFeedbackMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = avatarSelection.avatarFeedbackMessage,
                    isError = false,
                    testTag = "profile_avatar_feedback_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (!avatarSelection.avatarErrorMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = avatarSelection.avatarErrorMessage,
                    isError = true,
                    testTag = "profile_avatar_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (!profileEdit.feedbackMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = profileEdit.feedbackMessage,
                    isError = false,
                    testTag = "profile_edit_feedback_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (!profileEdit.errorMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = profileEdit.errorMessage,
                    isError = true,
                    testTag = "profile_edit_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            // Profile Header Summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WapiUserAvatar(
                            avatarUrl = userProfile?.avatarUrl,
                            initials = userProfile?.initials ?: "WR",
                            size = 64.dp,
                            contentDescription = "Profile photo",
                            localPreviewBytes = avatarSelection.selectedAvatarPreviewBytes,
                            isLoading = avatarSelection.isBusy,
                            enabled = !avatarSelection.isBusy,
                            onClick = {
                                launchPhotoPicker()
                            },
                            testTag = "profile_avatar_placeholder",
                            remoteImageTestTag = "profile_avatar_remote_image",
                            localPreviewTestTag = "profile_avatar_local_preview",
                            loadingIndicatorTestTag = "profile_avatar_processing_indicator"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fullName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("profile_full_name_text")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText,
                                modifier = Modifier.testTag("profile_email_text")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiMutedText,
                                modifier = Modifier.testTag("profile_phone_text")
                            )
                        }

                        if (!profileEdit.isEditing) {
                            IconButton(
                                onClick = onStartEditProfileClick,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("profile_edit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = stringResource(Res.string.profile_edit_button),
                                    tint = WapiDeepGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (!bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WapiPrimaryText,
                            modifier = Modifier.testTag("profile_bio_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    // Rating & Completed Trips Trust Summary (Phase 4.5)
                    val hasRatings = userProfile?.hasRatings == true
                    val ratingAverageText = userProfile?.formattedRatingAverage
                        ?: stringResource(Res.string.profile_rating_placeholder)
                    val ratingCountText = userProfile?.formattedRatingCount
                        ?: stringResource(Res.string.profile_rating_label)
                    val completedTripsText = userProfile?.formattedCompletedTrips
                        ?: stringResource(Res.string.profile_completed_trips_placeholder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    tint = if (hasRatings) WapiOrange else WapiDeepGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = ratingAverageText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = WapiPrimaryText,
                                    modifier = Modifier.testTag("profile_rating_placeholder")
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasRatings) ratingCountText else stringResource(Res.string.profile_rating_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText,
                                modifier = Modifier.testTag("profile_rating_count_text")
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(WapiBorder)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = completedTripsText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("profile_completed_trips_placeholder")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(Res.string.profile_completed_trips_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }
            }

            // Inline Profile Edit Form Card (Part 1: full_name, phone, bio)
            if (profileEdit.isEditing) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_edit_form_card"),
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
                            text = "EDIT PROFILE DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier.testTag("profile_edit_form_header")
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        WapiTextField(
                            value = profileEdit.fullName,
                            onValueChange = onEditFullNameChange,
                            label = stringResource(Res.string.label_full_name),
                            placeholder = stringResource(Res.string.placeholder_full_name),
                            errorMessage = profileEdit.fullNameError,
                            enabled = !profileEdit.isSaving,
                            leadingIcon = Icons.Outlined.PersonOutline,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "profile_edit_full_name_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        WapiTextField(
                            value = profileEdit.phone,
                            onValueChange = onEditPhoneChange,
                            label = stringResource(Res.string.label_phone),
                            placeholder = stringResource(Res.string.placeholder_phone),
                            errorMessage = profileEdit.phoneError,
                            enabled = !profileEdit.isSaving,
                            leadingIcon = Icons.Outlined.Phone,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "profile_edit_phone_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        WapiTextField(
                            value = profileEdit.bio,
                            onValueChange = onEditBioChange,
                            label = stringResource(Res.string.profile_bio_label),
                            placeholder = stringResource(Res.string.profile_bio_placeholder),
                            errorMessage = profileEdit.bioError,
                            enabled = !profileEdit.isSaving,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            testTag = "profile_edit_bio_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiSecondaryButton(
                                text = stringResource(Res.string.profile_cancel_button),
                                onClick = {
                                    focusManager.clearFocus()
                                    onCancelEditProfileClick()
                                },
                                enabled = !profileEdit.isSaving,
                                modifier = Modifier.weight(1f),
                                testTag = "profile_edit_cancel_button"
                            )

                            WapiPrimaryButton(
                                text = stringResource(Res.string.profile_save_button),
                                onClick = {
                                    focusManager.clearFocus()
                                    onSaveProfileClick()
                                },
                                isLoading = profileEdit.isSaving,
                                enabled = !profileEdit.isSaving,
                                modifier = Modifier.weight(1f),
                                testTag = "profile_edit_save_button"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            // Profile Sections: Account, Vehicles, Verification, Safety, Notifications, Settings
            val vehicleCount = safetyTrustState.vehiclesState.vehicles.size
            val vehiclesBadge = if (vehicleCount > 0) {
                "$vehicleCount saved"
            } else {
                "Add vehicle"
            }

            val overallVerification = safetyTrustState.verificationState.overallStatus
            val verificationBadge = overallVerification.badgeLabel
            val isVerificationHighlighted = overallVerification == OverallVerificationStatus.VERIFIED

            val contactCount = safetyTrustState.emergencyContactsState.contacts.size
            val safetyBadge = if (contactCount > 0) {
                "$contactCount contact${if (contactCount == 1) "" else "s"}"
            } else {
                "Set up"
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(WapiDimens.RadiusMD),
                color = WapiWhite,
                border = BorderStroke(1.dp, WapiBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileSectionItem(
                        icon = Icons.Outlined.PersonOutline,
                        title = stringResource(Res.string.profile_section_account),
                        subtitle = "Edit name, phone & community bio",
                        badgeText = "Edit",
                        onClick = onStartEditProfileClick,
                        testTag = "profile_section_account"
                    )
                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                    ProfileSectionItem(
                        icon = Icons.Outlined.DirectionsCar,
                        title = stringResource(Res.string.profile_section_vehicles),
                        subtitle = "Manage vehicles for rides you offer",
                        badgeText = vehiclesBadge,
                        onClick = onVehiclesClick,
                        testTag = "profile_section_vehicles"
                    )
                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                    ProfileSectionItem(
                        icon = Icons.Outlined.VerifiedUser,
                        title = stringResource(Res.string.profile_section_verification),
                        subtitle = "ID & community trust badges",
                        badgeText = verificationBadge,
                        isHighlightedBadge = isVerificationHighlighted,
                        onClick = onVerificationClick,
                        testTag = "profile_section_verification"
                    )
                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                    ProfileSectionItem(
                        icon = Icons.Outlined.HealthAndSafety,
                        title = stringResource(Res.string.profile_section_safety),
                        subtitle = "Trusted contacts & journey sharing",
                        badgeText = safetyBadge,
                        onClick = onSafetyContactsClick,
                        testTag = "profile_section_safety"
                    )
                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                    val notificationsBadge = if (unreadNotificationCount > 0) {
                        "$unreadNotificationCount unread"
                    } else {
                        "Active"
                    }
                    ProfileSectionItem(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(Res.string.profile_section_notifications),
                        subtitle = "Ride alerts & booking updates",
                        badgeText = notificationsBadge,
                        isHighlightedBadge = unreadNotificationCount > 0,
                        onClick = onNotificationsClick,
                        testTag = "profile_section_notifications"
                    )
                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                    ProfileSectionItem(
                        icon = Icons.Outlined.Settings,
                        title = stringResource(Res.string.profile_section_settings),
                        subtitle = "App preferences & privacy",
                        badgeText = accountStatus,
                        testTag = "profile_section_settings"
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            WapiSecondaryButton(
                text = stringResource(Res.string.cta_sign_out),
                onClick = onSignOutClick,
                testTag = "profile_sign_out_button"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
        }
    }
}

@Composable
private fun ProfileSectionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    isHighlightedBadge: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = WapiSubtleSurface,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WapiDeepGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WapiPrimaryText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WapiSecondaryText
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isHighlightedBadge) WapiOrange else WapiGreenTint
        ) {
            Text(
                text = badgeText ?: stringResource(Res.string.coming_soon_badge),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isHighlightedBadge) FontWeight.Bold else FontWeight.Normal
                ),
                color = WapiDeepGreen,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("${testTag}_badge")
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = WapiMutedText,
            modifier = Modifier.size(20.dp)
        )
    }
}
