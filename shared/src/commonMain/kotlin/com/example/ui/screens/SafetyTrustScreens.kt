package com.example.ui.screens

import com.example.resources.Res
import com.example.resources.action_back
import com.example.resources.emergency_contact_add_cta
import com.example.resources.profile_cancel_button
import com.example.resources.safety_screen_subtitle
import com.example.resources.safety_screen_title
import com.example.resources.vehicles_add_cta
import com.example.resources.vehicles_screen_subtitle
import com.example.resources.vehicles_screen_title
import com.example.resources.verification_screen_subtitle
import com.example.resources.verification_screen_title
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.OverallVerificationStatus
import com.example.data.UserProfile
import com.example.data.WapiEmergencyContact
import com.example.data.WapiVehicle
import com.example.data.WapiVerificationRecord
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiPrimaryButton
import com.example.ui.components.WapiSecondaryButton
import com.example.ui.components.WapiTextField
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
import com.example.ui.viewmodel.EmergencyContactsState
import com.example.ui.viewmodel.VehiclesState
import com.example.ui.viewmodel.VerificationState

// ============================================================================
// PART 2 — VEHICLES MANAGEMENT SCREEN (`public.vehicles`)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    vehiclesState: VehiclesState,
    onAddVehicleClick: () -> Unit,
    onEditVehicleClick: (WapiVehicle) -> Unit,
    onDismissVehicleForm: () -> Unit,
    onMakeChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onColourChange: (String) -> Unit,
    onRegistrationChange: (String) -> Unit,
    onSeatCapacityChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onSaveVehicleClick: () -> Unit,
    onRequestDeleteVehicleClick: (String) -> Unit,
    onConfirmDeleteVehicleClick: () -> Unit,
    onDismissDeleteVehicleDialog: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val form = vehiclesState.form

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("vehicles_screen"),
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
                    .testTag("vehicles_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = WapiPrimaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Text(
                text = stringResource(Res.string.vehicles_screen_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("vehicles_screen_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.vehicles_screen_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!vehiclesState.feedbackMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = vehiclesState.feedbackMessage,
                    isError = false,
                    testTag = "vehicles_feedback_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (!vehiclesState.errorMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = vehiclesState.errorMessage,
                    isError = true,
                    testTag = "vehicles_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            // Add / Edit Vehicle Form Card
            if (form.isVisible) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_form_card"),
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
                            text = if (form.isEditMode) "EDIT VEHICLE" else "ADD NEW VEHICLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier.testTag("vehicle_form_header")
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        if (!form.errorMessage.isNullOrBlank()) {
                            WapiFeedbackBanner(
                                message = form.errorMessage,
                                isError = true,
                                testTag = "vehicle_form_error_banner"
                            )
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                        }

                        WapiTextField(
                            value = form.make,
                            onValueChange = onMakeChange,
                            label = "Make",
                            placeholder = "e.g. Toyota",
                            errorMessage = form.makeError,
                            enabled = !form.isSaving,
                            leadingIcon = Icons.Outlined.DirectionsCar,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "vehicle_make_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        WapiTextField(
                            value = form.model,
                            onValueChange = onModelChange,
                            label = "Model",
                            placeholder = "e.g. Fielder",
                            errorMessage = form.modelError,
                            enabled = !form.isSaving,
                            leadingIcon = Icons.Outlined.DirectionsCar,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "vehicle_model_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiTextField(
                                value = form.year,
                                onValueChange = onYearChange,
                                label = "Year (optional)",
                                placeholder = "e.g. 2018",
                                errorMessage = form.yearError,
                                enabled = !form.isSaving,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.weight(1f),
                                testTag = "vehicle_year_input"
                            )

                            WapiTextField(
                                value = form.colour,
                                onValueChange = onColourChange,
                                label = "Colour",
                                placeholder = "e.g. Silver",
                                errorMessage = form.colourError,
                                enabled = !form.isSaving,
                                leadingIcon = Icons.Outlined.Palette,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.weight(1f),
                                testTag = "vehicle_colour_input"
                            )
                        }

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiTextField(
                                value = form.registration,
                                onValueChange = onRegistrationChange,
                                label = "Registration",
                                placeholder = "e.g. KDA 123A",
                                errorMessage = form.registrationError,
                                enabled = !form.isSaving,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.weight(1f),
                                testTag = "vehicle_registration_input"
                            )

                            WapiTextField(
                                value = form.seatCapacity,
                                onValueChange = onSeatCapacityChange,
                                label = "Seat capacity",
                                placeholder = "e.g. 4",
                                errorMessage = form.seatCapacityError,
                                enabled = !form.isSaving,
                                leadingIcon = Icons.Outlined.EventSeat,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier.weight(1f),
                                testTag = "vehicle_seat_capacity_input"
                            )
                        }

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !form.isSaving) {
                                    onActiveChange(!form.isActive)
                                }
                                .padding(vertical = 4.dp)
                                .testTag("vehicle_active_checkbox_row"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = form.isActive,
                                onCheckedChange = { onActiveChange(it) },
                                enabled = !form.isSaving,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = WapiPrimaryGreen,
                                    uncheckedColor = WapiSecondaryText
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Available for offering rides",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiPrimaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiSecondaryButton(
                                text = stringResource(Res.string.profile_cancel_button),
                                onClick = {
                                    focusManager.clearFocus()
                                    onDismissVehicleForm()
                                },
                                enabled = !form.isSaving,
                                modifier = Modifier.weight(1f),
                                testTag = "vehicle_form_cancel_button"
                            )

                            WapiPrimaryButton(
                                text = if (form.isEditMode) "Update Vehicle" else "Save Vehicle",
                                onClick = {
                                    focusManager.clearFocus()
                                    onSaveVehicleClick()
                                },
                                isLoading = form.isSaving,
                                enabled = !form.isSaving,
                                modifier = Modifier.weight(1f),
                                testTag = "vehicle_form_save_button"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            } else {
                WapiPrimaryButton(
                    text = stringResource(Res.string.vehicles_add_cta),
                    onClick = onAddVehicleClick,
                    testTag = "vehicles_add_button"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            when {
                vehiclesState.isLoading && vehiclesState.vehicles.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vehicles_loading_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp,
                                color = WapiPrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading your vehicles…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                vehiclesState.vehicles.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vehicles_empty_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(WapiDimens.SpaceLG),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = WapiGreenTint,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.DirectionsCar,
                                        contentDescription = null,
                                        tint = WapiDeepGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No vehicles added yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("vehicles_empty_title")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add your vehicle to associate it when offering rides on Wapi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                        modifier = Modifier.testTag("vehicles_list")
                    ) {
                        vehiclesState.vehicles.forEachIndexed { index, vehicle ->
                            VehicleItemCard(
                                vehicle = vehicle,
                                isDeleting = vehiclesState.deletingVehicleId == vehicle.id,
                                onEditClick = { onEditVehicleClick(vehicle) },
                                onDeleteClick = { onRequestDeleteVehicleClick(vehicle.id) },
                                testTagPrefix = "vehicle_card_$index"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
        }

        if (!vehiclesState.deleteConfirmationVehicleId.isNullOrBlank()) {
            BasicAlertDialog(onDismissRequest = onDismissDeleteVehicleDialog) {
                Surface(
                    shape = RoundedCornerShape(WapiDimens.RadiusLG),
                    color = WapiWhite,
                    border = BorderStroke(1.dp, WapiBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WapiDimens.SpaceMD)
                        .testTag("delete_vehicle_confirm_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WapiDimens.SpaceLG)
                    ) {
                        Text(
                            text = "Remove vehicle?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiPrimaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This will remove the vehicle from your Wapi account.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WapiSecondaryText
                        )
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiSecondaryButton(
                                text = "Cancel",
                                onClick = onDismissDeleteVehicleDialog,
                                modifier = Modifier.weight(1f),
                                testTag = "delete_vehicle_cancel_button"
                            )
                            WapiPrimaryButton(
                                text = "Remove",
                                onClick = onConfirmDeleteVehicleClick,
                                modifier = Modifier.weight(1f),
                                testTag = "delete_vehicle_confirm_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleItemCard(
    vehicle: WapiVehicle,
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    testTagPrefix: String
) {
    val isVerified = vehicle.isVerified
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
                        color = WapiGreenTint,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.DirectionsCar,
                                contentDescription = null,
                                tint = WapiDeepGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vehicle.displayTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiPrimaryText,
                            modifier = Modifier.testTag("${testTagPrefix}_title")
                        )
                        Text(
                            text = "${vehicle.effectiveRegistration} · ${vehicle.effectiveColour}",
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiSecondaryText,
                            modifier = Modifier.testTag("${testTagPrefix}_subtitle")
                        )
                    }
                }

                // Read-only verification status badge (`verification_status`)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isVerified) WapiSuccessContainer else WapiSubtleSurface
                ) {
                    Text(
                        text = vehicle.formattedVerificationStatus,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isVerified) WapiSuccess else WapiSecondaryText,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("${testTagPrefix}_verification_badge")
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
            Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.EventSeat,
                            contentDescription = null,
                            tint = WapiDeepGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${vehicle.seatCapacity} seats",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = WapiPrimaryText,
                            modifier = Modifier.testTag("${testTagPrefix}_seats")
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (vehicle.isActive) WapiGreenTint else WapiSubtleSurface
                    ) {
                        Text(
                            text = if (vehicle.isActive) "Active" else "Inactive",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (vehicle.isActive) WapiDeepGreen else WapiMutedText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEditClick,
                        enabled = !isDeleting,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("${testTagPrefix}_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit vehicle",
                            tint = WapiDeepGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        enabled = !isDeleting,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("${testTagPrefix}_delete_button")
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = WapiError
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete vehicle",
                                tint = WapiError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// PART 3 — VERIFICATION STATUS SCREEN (`public.verification_records` Read-Only)
// ============================================================================

@Composable
fun VerificationStatusScreen(
    userProfile: UserProfile?,
    verificationState: VerificationState,
    onRefreshClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val overallStatus = verificationState.overallStatus

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("verification_screen"),
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
                    .testTag("verification_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = WapiPrimaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Text(
                text = stringResource(Res.string.verification_screen_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("verification_screen_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.verification_screen_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!verificationState.errorMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = verificationState.errorMessage,
                    isError = true,
                    testTag = "verification_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            // Overall Account & Verification Summary Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verification_summary_card"),
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
                            Surface(
                                shape = CircleShape,
                                color = WapiGreenTint,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = WapiDeepGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Verification Status",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = WapiPrimaryText
                                )
                                Text(
                                    text = "Account status: ${userProfile?.formattedAccountStatus ?: "Active"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WapiSecondaryText,
                                    modifier = Modifier.testTag("verification_account_status_text")
                                )
                            }
                        }

                        val badgeBg = when (overallStatus) {
                            OverallVerificationStatus.VERIFIED -> WapiSuccessContainer
                            OverallVerificationStatus.PENDING -> WapiOrangeTint
                            OverallVerificationStatus.NOT_VERIFIED,
                            OverallVerificationStatus.NO_RECORD -> WapiSubtleSurface
                        }
                        val badgeColor = when (overallStatus) {
                            OverallVerificationStatus.VERIFIED -> WapiSuccess
                            OverallVerificationStatus.PENDING -> WapiDeepGreen
                            OverallVerificationStatus.NOT_VERIFIED,
                            OverallVerificationStatus.NO_RECORD -> WapiSecondaryText
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = overallStatus.badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = badgeColor,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("verification_overall_status_badge")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                    HorizontalDivider(thickness = 1.dp, color = WapiBorder)
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                    Text(
                        text = "Verification records are reviewed and managed securely by Wapi Trust & Safety.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiSecondaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            when {
                verificationState.isLoading && verificationState.records.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_loading_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp,
                                color = WapiPrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading verification records…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                verificationState.records.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verification_empty_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(WapiDimens.SpaceLG),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = WapiSecondaryText,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No verification record",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("verification_empty_title")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No identity or document verification records are currently on file for your account.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                        modifier = Modifier.testTag("verification_records_list")
                    ) {
                        verificationState.records.forEachIndexed { index, record ->
                            VerificationRecordCard(
                                record = record,
                                testTagPrefix = "verification_record_$index"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            WapiSecondaryButton(
                text = "Refresh Status",
                onClick = onRefreshClick,
                enabled = !verificationState.isLoading,
                testTag = "verification_refresh_button"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
        }
    }
}

@Composable
private fun VerificationRecordCard(
    record: WapiVerificationRecord,
    testTagPrefix: String
) {
    val statusIcon = when {
        record.isVerified -> Icons.Outlined.CheckCircle
        record.isPending -> Icons.Outlined.HourglassTop
        else -> Icons.Outlined.Info
    }
    val badgeBg = when {
        record.isVerified -> WapiSuccessContainer
        record.isPending -> WapiOrangeTint
        else -> WapiSubtleSurface
    }
    val badgeTextColor = when {
        record.isVerified -> WapiSuccess
        record.isPending -> WapiDeepGreen
        else -> WapiSecondaryText
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagPrefix),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiWhite,
        border = BorderStroke(1.dp, WapiBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WapiDimens.SpaceLG),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = if (record.isVerified) WapiPrimaryGreen else WapiSecondaryText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = record.formattedTypeLabel,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiPrimaryText,
                        modifier = Modifier.testTag("${testTagPrefix}_type")
                    )
                    val dateText = record.verifiedAt?.take(10) ?: record.createdAt?.take(10)
                    if (!dateText.isNullOrBlank()) {
                        Text(
                            text = "Updated $dateText",
                            style = MaterialTheme.typography.bodySmall,
                            color = WapiMutedText
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeBg
            ) {
                Text(
                    text = record.formattedStatusLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = badgeTextColor,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("${testTagPrefix}_status")
                )
            }
        }
    }
}

// ============================================================================
// PART 4 — EMERGENCY CONTACTS SCREEN (`public.emergency_contacts`)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyEmergencyContactsScreen(
    contactsState: EmergencyContactsState,
    onAddContactClick: () -> Unit,
    onEditContactClick: (WapiEmergencyContact) -> Unit,
    onDismissContactForm: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onPrimaryChange: (Boolean) -> Unit,
    onSaveContactClick: () -> Unit,
    onRequestDeleteContactClick: (String) -> Unit,
    onConfirmDeleteContactClick: () -> Unit,
    onDismissDeleteContactDialog: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val form = contactsState.form

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("safety_emergency_contacts_screen"),
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
                    .testTag("safety_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = WapiPrimaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Text(
                text = stringResource(Res.string.safety_screen_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("safety_screen_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.safety_screen_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!contactsState.feedbackMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = contactsState.feedbackMessage,
                    isError = false,
                    testTag = "emergency_contacts_feedback_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (!contactsState.errorMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = contactsState.errorMessage,
                    isError = true,
                    testTag = "emergency_contacts_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (form.isVisible) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_contact_form_card"),
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
                            text = if (form.isEditMode) "EDIT EMERGENCY CONTACT" else "ADD EMERGENCY CONTACT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiDeepGreen,
                            modifier = Modifier.testTag("emergency_contact_form_header")
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        if (!form.errorMessage.isNullOrBlank()) {
                            WapiFeedbackBanner(
                                message = form.errorMessage,
                                isError = true,
                                testTag = "emergency_contact_form_error_banner"
                            )
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                        }

                        WapiTextField(
                            value = form.name,
                            onValueChange = onNameChange,
                            label = "Contact full name",
                            placeholder = "e.g. Grace Wanjiku",
                            errorMessage = form.nameError,
                            enabled = !form.isSaving,
                            leadingIcon = Icons.Outlined.PersonOutline,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "emergency_contact_name_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        WapiTextField(
                            value = form.phone,
                            onValueChange = onPhoneChange,
                            label = "Kenyan phone number",
                            placeholder = "e.g. 0712 345 678",
                            errorMessage = form.phoneError,
                            enabled = !form.isSaving,
                            leadingIcon = Icons.Outlined.Phone,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            testTag = "emergency_contact_phone_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        WapiTextField(
                            value = form.relationship,
                            onValueChange = onRelationshipChange,
                            label = "Relationship (optional)",
                            placeholder = "e.g. Sister, Spouse, Colleague",
                            enabled = !form.isSaving,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            testTag = "emergency_contact_relationship_input"
                        )

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !form.isSaving) {
                                    onPrimaryChange(!form.isPrimary)
                                }
                                .padding(vertical = 4.dp)
                                .testTag("emergency_contact_primary_checkbox_row"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = form.isPrimary,
                                onCheckedChange = { onPrimaryChange(it) },
                                enabled = !form.isSaving,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = WapiPrimaryGreen,
                                    uncheckedColor = WapiSecondaryText
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Set as primary emergency contact",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiPrimaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiSecondaryButton(
                                text = stringResource(Res.string.profile_cancel_button),
                                onClick = {
                                    focusManager.clearFocus()
                                    onDismissContactForm()
                                },
                                enabled = !form.isSaving,
                                modifier = Modifier.weight(1f),
                                testTag = "emergency_contact_cancel_button"
                            )

                            WapiPrimaryButton(
                                text = if (form.isEditMode) "Update Contact" else "Save Contact",
                                onClick = {
                                    focusManager.clearFocus()
                                    onSaveContactClick()
                                },
                                isLoading = form.isSaving,
                                enabled = !form.isSaving,
                                modifier = Modifier.weight(1f),
                                testTag = "emergency_contact_save_button"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            } else {
                WapiPrimaryButton(
                    text = stringResource(Res.string.emergency_contact_add_cta),
                    onClick = onAddContactClick,
                    testTag = "emergency_contact_add_button"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            when {
                contactsState.isLoading && contactsState.contacts.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_contacts_loading_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.5.dp,
                                color = WapiPrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading emergency contacts…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                contactsState.contacts.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_contacts_empty_card"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(WapiDimens.SpaceLG),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = WapiGreenTint,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.HealthAndSafety,
                                        contentDescription = null,
                                        tint = WapiDeepGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No emergency contacts yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                modifier = Modifier.testTag("emergency_contacts_empty_title")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add a trusted friend or family member so you can share your Wapi trip details with them.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                else -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM),
                        modifier = Modifier.testTag("emergency_contacts_list")
                    ) {
                        contactsState.contacts.forEachIndexed { index, contact ->
                            EmergencyContactCard(
                                contact = contact,
                                isDeleting = contactsState.deletingContactId == contact.id,
                                onEditClick = { onEditContactClick(contact) },
                                onDeleteClick = { onRequestDeleteContactClick(contact.id) },
                                testTagPrefix = "emergency_contact_card_$index"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
        }

        if (!contactsState.deleteConfirmationContactId.isNullOrBlank()) {
            BasicAlertDialog(onDismissRequest = onDismissDeleteContactDialog) {
                Surface(
                    shape = RoundedCornerShape(WapiDimens.RadiusLG),
                    color = WapiWhite,
                    border = BorderStroke(1.dp, WapiBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WapiDimens.SpaceMD)
                        .testTag("delete_emergency_contact_confirm_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WapiDimens.SpaceLG)
                    ) {
                        Text(
                            text = "Remove emergency contact?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = WapiPrimaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This trusted contact will be removed from your account.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WapiSecondaryText
                        )
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            WapiSecondaryButton(
                                text = "Cancel",
                                onClick = onDismissDeleteContactDialog,
                                modifier = Modifier.weight(1f),
                                testTag = "delete_emergency_contact_cancel_button"
                            )
                            WapiPrimaryButton(
                                text = "Remove",
                                onClick = onConfirmDeleteContactClick,
                                modifier = Modifier.weight(1f),
                                testTag = "delete_emergency_contact_confirm_button"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactCard(
    contact: WapiEmergencyContact,
    isDeleting: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    testTagPrefix: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagPrefix),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiWhite,
        border = BorderStroke(1.dp, WapiBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WapiDimens.SpaceLG),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = contact.effectiveName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryText,
                        modifier = Modifier.testTag("${testTagPrefix}_name")
                    )

                    if (contact.isPrimary) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = WapiGreenTint
                        ) {
                            Text(
                                text = "Primary",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .testTag("${testTagPrefix}_primary_badge")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = contact.effectivePhone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WapiSecondaryText,
                    modifier = Modifier.testTag("${testTagPrefix}_phone")
                )

                if (!contact.effectiveRelationship.isNullOrBlank()) {
                    Text(
                        text = contact.effectiveRelationship.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = WapiMutedText,
                        modifier = Modifier.testTag("${testTagPrefix}_relationship")
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onEditClick,
                    enabled = !isDeleting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_edit_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit contact",
                        tint = WapiDeepGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    enabled = !isDeleting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_delete_button")
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = WapiError
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete contact",
                            tint = WapiError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
