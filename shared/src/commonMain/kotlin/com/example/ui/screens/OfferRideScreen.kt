package com.example.ui.screens

import com.example.platform.formatIsoDateUtc
import com.example.platform.currentHourOfDay
import com.example.platform.formatClockTime
import com.example.platform.currentTimeMillis
import com.example.resources.Res
import com.example.resources.action_back
import com.example.resources.cta_publish_ride
import com.example.resources.label_available_seats
import com.example.resources.label_contribution
import com.example.resources.label_date
import com.example.resources.label_departure_time
import com.example.resources.label_from
import com.example.resources.label_notes
import com.example.resources.label_to
import com.example.resources.offer_ride_subtitle
import com.example.resources.offer_ride_title
import com.example.resources.placeholder_available_seats
import com.example.resources.placeholder_contribution
import com.example.resources.placeholder_departure_time
import com.example.resources.placeholder_notes
import com.example.resources.placeholder_offer_date
import com.example.resources.placeholder_offer_from
import com.example.resources.placeholder_offer_to
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.FmdGood
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.WapiVehicle
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiPrimaryButton
import com.example.ui.components.WapiTextField
import com.example.ui.theme.WapiBorder
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiError
import com.example.ui.theme.WapiGreenTint
import com.example.ui.theme.WapiMutedText
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.theme.WapiSubtleSurface
import com.example.ui.theme.WapiWhite
import com.example.ui.viewmodel.OfferRideFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRideScreen(
    formState: OfferRideFormState,
    vehicles: List<WapiVehicle> = emptyList(),
    onOriginChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onDateSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
    onSeatsChange: (String) -> Unit,
    onContributionChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onVehicleSelected: (String?) -> Unit = {},
    onManageVehiclesClick: () -> Unit = {},
    onPublishClick: () -> Unit,
    onNavigateBack: () -> Unit,
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
            .testTag("offer_ride_screen"),
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
                enabled = !formState.isSubmitting,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("offer_ride_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = WapiPrimaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Text(
                text = stringResource(Res.string.offer_ride_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("offer_ride_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.offer_ride_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!formState.errorMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = formState.errorMessage,
                    isError = true,
                    testTag = "offer_ride_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (!formState.successMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = formState.successMessage,
                    isError = false,
                    testTag = "offer_ride_success_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            // Route & Schedule Section
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
                    Text(
                        text = "ROUTE & SCHEDULE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    WapiTextField(
                        value = formState.origin,
                        onValueChange = onOriginChange,
                        label = stringResource(Res.string.label_from),
                        placeholder = stringResource(Res.string.placeholder_offer_from),
                        errorMessage = formState.originError,
                        enabled = !formState.isSubmitting,
                        leadingIcon = Icons.Outlined.MyLocation,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "offer_ride_from_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    WapiTextField(
                        value = formState.destination,
                        onValueChange = onDestinationChange,
                        label = stringResource(Res.string.label_to),
                        placeholder = stringResource(Res.string.placeholder_offer_to),
                        errorMessage = formState.destinationError,
                        enabled = !formState.isSubmitting,
                        leadingIcon = Icons.Outlined.FmdGood,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "offer_ride_to_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                    ) {
                        PickerFieldBox(
                            label = stringResource(Res.string.label_date),
                            value = formState.departureDate,
                            placeholder = stringResource(Res.string.placeholder_offer_date),
                            icon = Icons.Outlined.CalendarToday,
                            errorMessage = formState.departureDateError,
                            enabled = !formState.isSubmitting,
                            onClick = {
                                focusManager.clearFocus()
                                showDatePicker = true
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "offer_ride_date_picker_field"
                        )

                        PickerFieldBox(
                            label = stringResource(Res.string.label_departure_time),
                            value = if (formState.departureTime.length >= 5) {
                                formState.departureTime.substring(0, 5)
                            } else {
                                formState.departureTime
                            },
                            placeholder = stringResource(Res.string.placeholder_departure_time),
                            icon = Icons.Outlined.AccessTime,
                            errorMessage = formState.departureTimeError,
                            enabled = !formState.isSubmitting,
                            onClick = {
                                focusManager.clearFocus()
                                showTimePicker = true
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "offer_ride_time_picker_field"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            // Optional Vehicle Selection Section (Phase 4.8B)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offer_ride_vehicle_card"),
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
                            text = "VEHICLE (OPTIONAL)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen
                        )

                        TextButton(
                            onClick = onManageVehiclesClick,
                            enabled = !formState.isSubmitting,
                            modifier = Modifier.testTag("offer_ride_manage_vehicles_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = WapiDeepGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (vehicles.isEmpty()) "Add vehicle" else "Manage",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen
                            )
                        }
                    }

                    val activeVehicles = vehicles.filter { it.isActive }
                    if (activeVehicles.isEmpty()) {
                        Text(
                            text = "No active vehicles registered yet. You can still publish your ride or add a vehicle for passenger trust.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiSecondaryText,
                            modifier = Modifier.testTag("offer_ride_no_vehicles_hint")
                        )
                    } else {
                        Text(
                            text = "Select the vehicle you will be driving on this route.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiSecondaryText
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeVehicles.forEachIndexed { index, vehicle ->
                                val isSelected = formState.selectedVehicleId == vehicle.id
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable(enabled = !formState.isSubmitting) {
                                            onVehicleSelected(if (isSelected) null else vehicle.id)
                                        }
                                        .testTag("offer_ride_vehicle_option_$index"),
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DirectionsCar,
                                                contentDescription = null,
                                                tint = if (isSelected) WapiDeepGreen else WapiSecondaryText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = vehicle.displayTitle,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = WapiPrimaryText
                                                )
                                                Text(
                                                    text = "${vehicle.displaySubtitle} · ${vehicle.seatCapacity} seats",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = WapiSecondaryText
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = "Selected vehicle",
                                                tint = WapiPrimaryGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            // Seats, Contribution & Notes Section
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
                    Text(
                        text = "SEATS & COST SHARING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiDeepGreen
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    WapiTextField(
                        value = formState.availableSeats,
                        onValueChange = onSeatsChange,
                        label = stringResource(Res.string.label_available_seats),
                        placeholder = stringResource(Res.string.placeholder_available_seats),
                        errorMessage = formState.availableSeatsError,
                        enabled = !formState.isSubmitting,
                        leadingIcon = Icons.Outlined.EventSeat,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "offer_ride_seats_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    WapiTextField(
                        value = formState.contributionAmount,
                        onValueChange = onContributionChange,
                        label = stringResource(Res.string.label_contribution),
                        placeholder = stringResource(Res.string.placeholder_contribution),
                        errorMessage = formState.contributionError,
                        enabled = !formState.isSubmitting,
                        leadingIcon = Icons.Outlined.Payments,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        testTag = "offer_ride_contribution_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    WapiTextField(
                        value = formState.notes,
                        onValueChange = onNotesChange,
                        label = stringResource(Res.string.label_notes),
                        placeholder = stringResource(Res.string.placeholder_notes),
                        enabled = !formState.isSubmitting,
                        leadingIcon = Icons.AutoMirrored.Outlined.Notes,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "offer_ride_notes_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

                    WapiPrimaryButton(
                        text = stringResource(Res.string.cta_publish_ride),
                        onClick = {
                            focusManager.clearFocus()
                            onPublishClick()
                        },
                        isLoading = formState.isSubmitting,
                        enabled = !formState.isSubmitting,
                        testTag = "offer_ride_publish_button"
                    )
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
                        modifier = Modifier.testTag("date_picker_confirm_button")
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
                            text = "Select departure time",
                            style = MaterialTheme.typography.titleMedium,
                            color = WapiPrimaryText,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                        TimePicker(state = timePickerState)
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text(text = "Cancel", color = WapiSecondaryText)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    val formatted = formatClockTime(timePickerState.hour, timePickerState.minute)
                                    onTimeSelected(formatted)
                                    showTimePicker = false
                                },
                                modifier = Modifier.testTag("time_picker_confirm_button")
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

@Composable
private fun PickerFieldBox(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    errorMessage: String?,
    enabled: Boolean,
    onClick: () -> Unit,
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
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (errorMessage != null) WapiError else WapiSecondaryText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = value.ifBlank { placeholder },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (value.isNotBlank()) WapiPrimaryText else WapiMutedText,
                    maxLines = 1
                )
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
