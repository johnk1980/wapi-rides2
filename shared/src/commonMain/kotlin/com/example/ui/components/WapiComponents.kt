package com.example.ui.components

import com.example.resources.Res
import com.example.resources.brand_logo_slot_desc
import com.example.resources.wapi_logo
import org.jetbrains.compose.resources.decodeToImageBitmap
import com.example.platform.WapiRemoteImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.WapiBottomTab
import com.example.ui.theme.WapiBorder
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiError
import com.example.ui.theme.WapiErrorContainer
import com.example.ui.theme.WapiGreenTint
import com.example.ui.theme.WapiMutedText
import com.example.ui.theme.WapiOnErrorContainer
import com.example.ui.theme.WapiOrange
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSuccess
import com.example.ui.theme.WapiSuccessContainer
import com.example.ui.theme.WapiWhite

/**
 * Clean, dedicated Wapi logo container slot rendering the official `logo.png` asset.
 * Displays the logo inside a premium rounded-rectangle container with 16dp corners,
 * white/subtle neutral surface, thin refined border, soft elevation, comfortable
 * internal padding, and ContentScale.Fit so the original aspect ratio is preserved.
 */
@Composable
fun WapiBrandLogoSlot(
    modifier: Modifier = Modifier,
    onDarkBackground: Boolean = true,
    size: Dp = 68.dp,
    testTag: String = "wapi_brand_logo_slot"
) {
    val desc = stringResource(Res.string.brand_logo_slot_desc)
    val borderColor = if (onDarkBackground) {
        WapiWhite.copy(alpha = 0.85f)
    } else {
        WapiBorder
    }
    val internalPadding = (size * 0.14f).coerceIn(6.dp, 12.dp)

    Surface(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .semantics { contentDescription = desc },
        shape = RoundedCornerShape(16.dp),
        color = WapiWhite,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.wapi_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("${testTag}_image")
            )
        }
    }
}

/**
 * Reusable circular user avatar component across Wapi Rides.
 *
 * Behavior:
 * 1. If `isLoading` is true, renders a compact circular progress indicator.
 * 2. If `localPreviewBytes` is non-null and decodable, renders the local bitmap preview.
 * 3. If `avatarUrl` is non-null and non-blank, loads the remote image using Coil (`SubcomposeAsyncImage`)
 *    with `ContentScale.Crop` and falls back to `initials` while loading or if loading fails.
 * 4. Otherwise, renders the existing Wapi green circular initials placeholder.
 */
@Composable
fun WapiUserAvatar(
    avatarUrl: String?,
    initials: String,
    size: Dp,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    localPreviewBytes: ByteArray? = null,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    borderAlpha: Float = 0.25f,
    initialsTextStyle: TextStyle? = null,
    testTag: String? = null,
    remoteImageTestTag: String? = null,
    localPreviewTestTag: String? = null,
    loadingIndicatorTestTag: String? = null
) {
    val cleanInitials = initials.trim().ifEmpty { "WR" }
    val cleanAvatarUrl = avatarUrl?.trim()?.takeIf { it.isNotEmpty() }
    val resolvedTextStyle = initialsTextStyle ?: when {
        size >= 60.dp -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        size >= 50.dp -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        size >= 42.dp -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        size >= 36.dp -> MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        else -> MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
    }

    val previewBitmap = remember(localPreviewBytes) {
        localPreviewBytes?.let { bytes ->
            runCatching { bytes.decodeToImageBitmap() }.getOrNull()
        }
    }

    Surface(
        shape = CircleShape,
        color = WapiGreenTint,
        border = BorderStroke(1.dp, WapiPrimaryGreen.copy(alpha = borderAlpha)),
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .then(
                if (!testTag.isNullOrBlank()) {
                    Modifier.testTag(testTag)
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size((size * 0.375f).coerceIn(16.dp, 24.dp))
                            .then(
                                if (!loadingIndicatorTestTag.isNullOrBlank()) {
                                    Modifier.testTag(loadingIndicatorTestTag)
                                } else {
                                    Modifier
                                }
                            ),
                        strokeWidth = 2.dp,
                        color = WapiDeepGreen
                    )
                }
                previewBitmap != null -> {
                    Image(
                        bitmap = previewBitmap,
                        contentDescription = contentDescription ?: "Selected profile photo preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .then(
                                if (!localPreviewTestTag.isNullOrBlank()) {
                                    Modifier.testTag(localPreviewTestTag)
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
                cleanAvatarUrl != null -> {
                    WapiRemoteImage(
                        model = cleanAvatarUrl,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .then(
                                if (!remoteImageTestTag.isNullOrBlank()) {
                                    Modifier.testTag(remoteImageTestTag)
                                } else {
                                    Modifier
                                }
                            ),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cleanInitials,
                                    style = resolvedTextStyle,
                                    color = WapiDeepGreen
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cleanInitials,
                                    style = resolvedTextStyle,
                                    color = WapiDeepGreen
                                )
                            }
                        }
                    )
                }
                else -> {
                    Text(
                        text = cleanInitials,
                        style = resolvedTextStyle,
                        color = WapiDeepGreen
                    )
                }
            }
        }
    }
}

@Composable
fun WapiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    useOrangeAccent: Boolean = false,
    testTag: String = "wapi_primary_button"
) {
    val containerColor = if (useOrangeAccent) WapiOrange else WapiPrimaryGreen
    val contentColor = if (useOrangeAccent) WapiPrimaryText else WapiWhite

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(WapiDimens.ButtonHeight)
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.55f),
            disabledContentColor = contentColor.copy(alpha = 0.75f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = contentColor
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun WapiSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDarkBackground: Boolean = false,
    testTag: String = "wapi_secondary_button"
) {
    val borderColor = if (onDarkBackground) WapiWhite.copy(alpha = 0.45f) else WapiBorder
    val contentColor = if (onDarkBackground) WapiWhite else WapiPrimaryText
    val containerColor = if (onDarkBackground) WapiWhite.copy(alpha = 0.08f) else WapiWhite

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(WapiDimens.ButtonHeight)
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun WapiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = "wapi_text_field"
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = WapiPrimaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            isError = errorMessage != null,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WapiMutedText
                )
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (errorMessage != null) WapiError else WapiSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            trailingIcon = if (isPassword) {
                {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = WapiSecondaryText
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(WapiDimens.RadiusMD),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = WapiWhite,
                unfocusedContainerColor = WapiWhite,
                disabledContainerColor = WapiWhite.copy(alpha = 0.6f),
                focusedBorderColor = WapiPrimaryGreen,
                unfocusedBorderColor = WapiBorder,
                errorBorderColor = WapiError,
                focusedTextColor = WapiPrimaryText,
                unfocusedTextColor = WapiPrimaryText,
                cursorColor = WapiPrimaryGreen
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
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

@Composable
fun WapiFeedbackBanner(
    message: String,
    isError: Boolean = true,
    modifier: Modifier = Modifier,
    testTag: String = "wapi_feedback_banner"
) {
    val bgColor = if (isError) WapiErrorContainer else WapiSuccessContainer
    val textColor = if (isError) WapiOnErrorContainer else WapiSuccess
    val icon = if (isError) Icons.Filled.ErrorOutline else Icons.Filled.CheckCircleOutline

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}

@Composable
fun WapiEmptyStateSection(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    testTag: String = "wapi_empty_state"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = WapiWhite,
        border = BorderStroke(1.dp, WapiBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WapiGreenTint,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = WapiDeepGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WapiGreenTint
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiDeepGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = WapiPrimaryText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = WapiSecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WapiBottomNavigationBar(
    selectedTab: WapiBottomTab,
    onTabSelected: (WapiBottomTab) -> Unit,
    unreadMessageCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp, color = WapiBorder)
        NavigationBar(
            containerColor = WapiWhite,
            tonalElevation = 0.dp
        ) {
            WapiBottomTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val icon = when (tab) {
                    WapiBottomTab.HOME -> Icons.Outlined.Home
                    WapiBottomTab.FIND -> Icons.Outlined.Search
                    WapiBottomTab.TRIPS -> Icons.Outlined.Map
                    WapiBottomTab.MESSAGES -> Icons.AutoMirrored.Outlined.Message
                    WapiBottomTab.PROFILE -> Icons.Outlined.Person
                }
                val label = stringResource(tab.labelRes)
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                            if (tab == WapiBottomTab.MESSAGES && unreadMessageCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = WapiOrange,
                                    modifier = Modifier.testTag("bottom_nav_messages_unread_badge")
                                ) {
                                    Text(
                                        text = if (unreadMessageCount > 99) "99+" else unreadMessageCount.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = WapiDeepGreen,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WapiDeepGreen,
                        selectedTextColor = WapiDeepGreen,
                        indicatorColor = WapiGreenTint,
                        unselectedIconColor = WapiSecondaryText,
                        unselectedTextColor = WapiSecondaryText
                    ),
                    modifier = Modifier.testTag(tab.testTag)
                )
            }
        }
    }
}
