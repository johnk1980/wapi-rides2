package com.example.ui.screens

import com.example.resources.Res
import com.example.resources.messages_empty_body
import com.example.resources.messages_empty_title
import com.example.resources.messages_title
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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.WapiConversation
import com.example.ui.components.WapiEmptyStateSection
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiSecondaryButton
import com.example.ui.components.WapiUserAvatar
import com.example.ui.theme.WapiBorder
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiGreenTint
import com.example.ui.theme.WapiMutedText
import com.example.ui.theme.WapiOrange
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.theme.WapiSubtleSurface
import com.example.ui.theme.WapiWhite
import com.example.ui.viewmodel.ConversationsStatus
import com.example.ui.viewmodel.MessagingState

@Composable
fun MessagesScreen(
    messagingState: MessagingState = MessagingState(),
    onConversationClick: (WapiConversation) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("messages_screen"),
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
            // Top bar: Title + Unread count chip + Refresh button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.messages_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = WapiPrimaryText,
                        modifier = Modifier.testTag("messages_title")
                    )

                    if (messagingState.unreadMessageCount > 0) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WapiOrange,
                            modifier = Modifier.testTag("messages_unread_count_chip")
                        ) {
                            Text(
                                text = "${messagingState.unreadMessageCount} unread",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRefreshClick,
                    enabled = !messagingState.isLoadingConversations,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("messages_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh conversations",
                        tint = WapiDeepGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            if (!messagingState.errorMessage.isNullOrBlank() &&
                messagingState.conversations.isNotEmpty()
            ) {
                WapiFeedbackBanner(
                    message = messagingState.errorMessage,
                    isError = true,
                    testTag = "messages_inline_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            when {
                messagingState.status == ConversationsStatus.LOADING &&
                    messagingState.conversations.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("messages_loading_state"),
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
                                text = "Loading conversations…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiSecondaryText
                            )
                        }
                    }
                }

                messagingState.status == ConversationsStatus.ERROR &&
                    messagingState.conversations.isEmpty() -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("messages_error_state"),
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
                                message = messagingState.errorMessage
                                    ?: "Unable to load your conversations right now.",
                                isError = true,
                                testTag = "messages_error_banner"
                            )
                            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                            WapiSecondaryButton(
                                text = "Try Again",
                                onClick = onRefreshClick,
                                testTag = "messages_retry_button"
                            )
                        }
                    }
                }

                messagingState.conversations.isEmpty() -> {
                    WapiEmptyStateSection(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = stringResource(Res.string.messages_empty_title),
                        description = stringResource(Res.string.messages_empty_body),
                        testTag = "messages_empty_state"
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("messages_conversation_list"),
                        verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                    ) {
                        itemsIndexed(
                            items = messagingState.conversations,
                            key = { index, item -> item.id.ifBlank { "conversation_$index" } }
                        ) { index, conversation ->
                            WapiConversationCard(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation) },
                                testTagPrefix = "conversation_item_$index"
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
fun WapiConversationCard(
    conversation: WapiConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "conversation_item_0"
) {
    val participant = conversation.resolvedOtherParticipant
    val hasUnread = conversation.hasUnread
    val containerColor = if (hasUnread) WapiWhite else WapiSubtleSurface
    val borderColor = if (hasUnread) WapiPrimaryGreen.copy(alpha = 0.45f) else WapiBorder

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WapiDimens.RadiusMD))
            .clickable(onClick = onClick)
            .testTag(testTagPrefix),
        shape = RoundedCornerShape(WapiDimens.RadiusMD),
        color = containerColor,
        border = BorderStroke(if (hasUnread) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Participant Avatar
            WapiUserAvatar(
                avatarUrl = participant.avatarUrl,
                initials = participant.initials,
                size = 46.dp,
                contentDescription = "${participant.displayName} avatar",
                testTag = "${testTagPrefix}_participant_avatar",
                remoteImageTestTag = "${testTagPrefix}_participant_avatar_remote_image"
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Top Row: Participant Name + Latest Message Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = participant.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = WapiPrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${testTagPrefix}_participant_name")
                    )

                    if (conversation.latestMessageTimestamp.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = conversation.latestMessageTimestamp,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (hasUnread) WapiDeepGreen else WapiMutedText,
                            modifier = Modifier.testTag("${testTagPrefix}_timestamp")
                        )
                    }
                }

                // Optional Trip Route Context Chip
                val routeLabel = conversation.routeLabel
                if (!routeLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = WapiPrimaryGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = routeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = WapiDeepGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("${testTagPrefix}_route")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Row: Latest Message Preview + Unread Count Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.latestMessagePreview,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (hasUnread) WapiPrimaryText else WapiSecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${testTagPrefix}_preview")
                    )

                    if (hasUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = WapiOrange,
                            modifier = Modifier.testTag("${testTagPrefix}_unread_badge")
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiDeepGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = WapiMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
