package com.example.ui.screens

import com.example.platform.PlatformBackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.EventSeat
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.WapiNotification
import com.example.data.WapiNotificationType
import com.example.ui.components.WapiEmptyStateSection
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiSecondaryButton
import com.example.ui.theme.WapiBorder
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiError
import com.example.ui.theme.WapiErrorContainer
import com.example.ui.theme.WapiGreenTint
import com.example.ui.theme.WapiMutedText
import com.example.ui.theme.WapiOrange
import com.example.ui.theme.WapiOrangeTint
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.theme.WapiSubtleSurface
import com.example.ui.theme.WapiWhite
import com.example.ui.viewmodel.NotificationsState
import com.example.ui.viewmodel.NotificationsStatus

@Composable
fun NotificationsScreen(
    notificationsState: NotificationsState,
    onNotificationClick: (WapiNotification) -> Unit,
    onMarkAllReadClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlatformBackHandler(onBack = onNavigateBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("notifications_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = WapiDimens.MaxContentWidth)
                .padding(
                    horizontal = WapiDimens.ScreenHorizontalPadding,
                    vertical = WapiDimens.SpaceLG
                )
        ) {
            // Top bar: Back button, Title + unread count badge, Refresh & Mark all as read actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("notifications_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = WapiPrimaryText
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryText,
                        modifier = Modifier.testTag("notifications_title")
                    )

                    if (notificationsState.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WapiOrange,
                            modifier = Modifier.testTag("notifications_unread_count_chip")
                        ) {
                            Text(
                                text = "${notificationsState.unreadCount} new",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notificationsState.hasUnread) {
                        TextButton(
                            onClick = onMarkAllReadClick,
                            enabled = !notificationsState.isMarkingAllRead,
                            modifier = Modifier.testTag("notifications_mark_all_read_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DoneAll,
                                contentDescription = null,
                                tint = WapiDeepGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (notificationsState.isMarkingAllRead) "Marking…" else "Mark all as read",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = WapiDeepGreen
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !notificationsState.isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("notifications_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh notifications",
                            tint = WapiDeepGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            if (!notificationsState.errorMessage.isNullOrBlank() &&
                notificationsState.notifications.isNotEmpty()
            ) {
                WapiFeedbackBanner(
                    message = notificationsState.errorMessage,
                    isError = true,
                    testTag = "notifications_inline_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            when {
                notificationsState.status == NotificationsStatus.LOADING &&
                    notificationsState.notifications.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notifications_loading_state"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = WapiDeepGreen,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Loading notifications…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                notificationsState.status == NotificationsStatus.ERROR &&
                    notificationsState.notifications.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notifications_error_state"),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = WapiWhite,
                        border = BorderStroke(1.dp, WapiBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(WapiDimens.SpaceXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            WapiFeedbackBanner(
                                message = notificationsState.errorMessage
                                    ?: "Unable to load notifications right now.",
                                isError = true,
                                testTag = "notifications_error_banner"
                            )
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                            WapiSecondaryButton(
                                text = "Try Again",
                                onClick = onRefreshClick,
                                testTag = "notifications_retry_button"
                            )
                        }
                    }
                }

                notificationsState.notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notifications_empty_state")
                    ) {
                        WapiEmptyStateSection(
                            icon = Icons.Outlined.NotificationsNone,
                            title = "No notifications yet",
                            description = "Ride requests, booking updates, and trip activity will appear here.",
                            testTag = "notifications_empty_card"
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("notifications_list"),
                        verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                    ) {
                        itemsIndexed(
                            items = notificationsState.notifications,
                            key = { index, item -> item.id.ifBlank { "notification_$index" } }
                        ) { index, notification ->
                            WapiNotificationCard(
                                notification = notification,
                                onClick = { onNotificationClick(notification) },
                                testTagPrefix = "notification_item_$index"
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WapiNotificationCard(
    notification: WapiNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "notification_item_0"
) {
    val isUnread = !notification.isRead
    val visualSpec = resolveNotificationVisualSpec(notification.notificationType)

    val containerColor = if (isUnread) WapiWhite else WapiSubtleSurface
    val borderColor = if (isUnread) WapiPrimaryGreen.copy(alpha = 0.45f) else WapiBorder

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WapiDimens.RadiusMD))
            .clickable(onClick = onClick)
            .testTag(testTagPrefix),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = containerColor,
        border = BorderStroke(if (isUnread) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = visualSpec.iconContainerColor,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = visualSpec.icon,
                        contentDescription = null,
                        tint = visualSpec.iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = WapiPrimaryText,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${testTagPrefix}_title")
                    )

                    if (isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("${testTagPrefix}_unread_indicator")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(WapiOrange)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Unread",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Read",
                            style = MaterialTheme.typography.labelSmall,
                            color = WapiMutedText,
                            modifier = Modifier.testTag("${testTagPrefix}_read_indicator")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnread) WapiPrimaryText else WapiSecondaryText,
                    modifier = Modifier.testTag("${testTagPrefix}_body")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = visualSpec.badgeBackgroundColor
                    ) {
                        Text(
                            text = visualSpec.categoryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = visualSpec.badgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = notification.formattedTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = WapiMutedText,
                        modifier = Modifier.testTag("${testTagPrefix}_timestamp")
                    )
                }
            }
        }
    }
}

private data class NotificationVisualSpec(
    val icon: ImageVector,
    val iconContainerColor: Color,
    val iconTint: Color,
    val categoryLabel: String,
    val badgeBackgroundColor: Color,
    val badgeTextColor: Color
)

private fun resolveNotificationVisualSpec(type: WapiNotificationType): NotificationVisualSpec {
    return when (type) {
        WapiNotificationType.TRIP_REQUEST_RECEIVED -> NotificationVisualSpec(
            icon = Icons.Outlined.EventSeat,
            iconContainerColor = WapiOrangeTint,
            iconTint = WapiDeepGreen,
            categoryLabel = "Seat request",
            badgeBackgroundColor = WapiOrangeTint,
            badgeTextColor = WapiDeepGreen
        )
        WapiNotificationType.TRIP_REQUEST_ACCEPTED -> NotificationVisualSpec(
            icon = Icons.Outlined.CheckCircle,
            iconContainerColor = WapiGreenTint,
            iconTint = WapiDeepGreen,
            categoryLabel = "Confirmed",
            badgeBackgroundColor = WapiGreenTint,
            badgeTextColor = WapiDeepGreen
        )
        WapiNotificationType.TRIP_REQUEST_DECLINED -> NotificationVisualSpec(
            icon = Icons.Outlined.Cancel,
            iconContainerColor = WapiErrorContainer,
            iconTint = WapiError,
            categoryLabel = "Declined",
            badgeBackgroundColor = WapiErrorContainer,
            badgeTextColor = WapiError
        )
        WapiNotificationType.TRIP_REQUEST_CANCELLED -> NotificationVisualSpec(
            icon = Icons.Outlined.Cancel,
            iconContainerColor = WapiSubtleSurface,
            iconTint = WapiSecondaryText,
            categoryLabel = "Request cancelled",
            badgeBackgroundColor = WapiSubtleSurface,
            badgeTextColor = WapiSecondaryText
        )
        WapiNotificationType.BOOKING_CANCELLED -> NotificationVisualSpec(
            icon = Icons.Outlined.Cancel,
            iconContainerColor = WapiErrorContainer,
            iconTint = WapiError,
            categoryLabel = "Booking cancelled",
            badgeBackgroundColor = WapiErrorContainer,
            badgeTextColor = WapiError
        )
        WapiNotificationType.TRIP_STARTED -> NotificationVisualSpec(
            icon = Icons.Outlined.DirectionsCar,
            iconContainerColor = WapiGreenTint,
            iconTint = WapiDeepGreen,
            categoryLabel = "In progress",
            badgeBackgroundColor = WapiGreenTint,
            badgeTextColor = WapiDeepGreen
        )
        WapiNotificationType.TRIP_COMPLETED -> NotificationVisualSpec(
            icon = Icons.Outlined.Flag,
            iconContainerColor = WapiGreenTint,
            iconTint = WapiDeepGreen,
            categoryLabel = "Completed",
            badgeBackgroundColor = WapiGreenTint,
            badgeTextColor = WapiDeepGreen
        )
        WapiNotificationType.RATING_AVAILABLE -> NotificationVisualSpec(
            icon = Icons.Outlined.StarOutline,
            iconContainerColor = WapiOrangeTint,
            iconTint = WapiOrange,
            categoryLabel = "Rating ready",
            badgeBackgroundColor = WapiOrangeTint,
            badgeTextColor = WapiDeepGreen
        )
        WapiNotificationType.UNKNOWN -> NotificationVisualSpec(
            icon = Icons.Outlined.NotificationsNone,
            iconContainerColor = WapiGreenTint,
            iconTint = WapiDeepGreen,
            categoryLabel = "Update",
            badgeBackgroundColor = WapiGreenTint,
            badgeTextColor = WapiDeepGreen
        )
    }
}
