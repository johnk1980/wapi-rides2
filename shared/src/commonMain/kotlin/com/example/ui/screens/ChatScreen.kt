package com.example.ui.screens

import com.example.platform.PlatformBackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.WapiMessage
import com.example.ui.components.WapiEmptyStateSection
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiSecondaryButton
import com.example.ui.components.WapiUserAvatar
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
import com.example.ui.theme.WapiWhite
import com.example.ui.viewmodel.ChatMessagesStatus
import com.example.ui.viewmodel.ChatState

@Composable
fun ChatScreen(
    chatState: ChatState,
    currentUserId: String,
    onDraftMessageChange: (String) -> Unit,
    onSendMessageClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PlatformBackHandler(onBack = onNavigateBack)
    val focusManager = LocalFocusManager.current
    val otherParticipant = chatState.conversation?.resolvedOtherParticipant
        ?: chatState.members.firstOrNull { it.userId != currentUserId }?.resolvedProfileSummary
        ?: chatState.members.firstOrNull()?.resolvedProfileSummary

    val participantTitle = otherParticipant?.displayName ?: "Wapi Member"
    val routeSubtitle = chatState.conversation?.routeLabel

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .testTag("chat_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = WapiDimens.MaxContentWidth)
        ) {
            // Top App Bar: Back button, Participant avatar + name (+ route subtitle), Refresh button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WapiWhite,
                border = BorderStroke(1.dp, WapiBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WapiDimens.SpaceMD,
                            vertical = WapiDimens.SpaceSM
                        ),
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
                                .testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = WapiPrimaryText
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        WapiUserAvatar(
                            avatarUrl = otherParticipant?.avatarUrl,
                            initials = otherParticipant?.initials ?: "WM",
                            size = 38.dp,
                            contentDescription = "$participantTitle avatar",
                            testTag = "chat_participant_avatar",
                            remoteImageTestTag = "chat_participant_avatar_remote_image"
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = participantTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WapiPrimaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("chat_participant_title")
                            )

                            if (!routeSubtitle.isNullOrBlank()) {
                                Text(
                                    text = routeSubtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WapiSecondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("chat_route_subtitle")
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !chatState.isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh messages",
                            tint = WapiDeepGreen
                        )
                    }
                }
            }

            // Messages area (weight 1f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = WapiDimens.ScreenHorizontalPadding)
            ) {
                when {
                    chatState.status == ChatMessagesStatus.LOADING &&
                        chatState.messages.isEmpty() -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = WapiDimens.SpaceLG)
                                .testTag("chat_loading_state"),
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
                                    text = "Loading messages…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WapiSecondaryText
                                )
                            }
                        }
                    }

                    chatState.status == ChatMessagesStatus.ERROR &&
                        chatState.messages.isEmpty() -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = WapiDimens.SpaceLG)
                                .testTag("chat_error_state"),
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
                                    message = chatState.errorMessage
                                        ?: "Unable to load messages for this conversation.",
                                    isError = true,
                                    testTag = "chat_error_banner"
                                )
                                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                                WapiSecondaryButton(
                                    text = "Try Again",
                                    onClick = onRefreshClick,
                                    testTag = "chat_retry_button"
                                )
                            }
                        }
                    }

                    chatState.messages.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = WapiDimens.SpaceLG)
                                .testTag("chat_empty_state")
                        ) {
                            WapiEmptyStateSection(
                                icon = Icons.Outlined.ChatBubbleOutline,
                                title = "No messages yet",
                                description = "Send a message below to coordinate pickup details or departure timing.",
                                testTag = "chat_empty_card"
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("chat_messages_list"),
                            verticalArrangement = Arrangement.spacedBy(WapiDimens.SpaceSM)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                            }

                            itemsIndexed(
                                items = chatState.messages,
                                key = { index, msg -> msg.id.ifBlank { "msg_$index" } }
                            ) { index, message ->
                                val isMine = currentUserId.isNotBlank() && message.senderId == currentUserId
                                WapiChatMessageBubble(
                                    message = message,
                                    isSentByCurrentUser = isMine,
                                    testTagPrefix = "chat_message_item_$index"
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                            }
                        }
                    }
                }
            }

            // Send error banner (if any)
            if (!chatState.sendErrorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WapiDimens.ScreenHorizontalPadding,
                            vertical = WapiDimens.SpaceXS
                        )
                ) {
                    WapiFeedbackBanner(
                        message = chatState.sendErrorMessage,
                        isError = true,
                        testTag = "chat_send_error_banner"
                    )
                }
            }

            // Bottom Composer Bar: Text input + Send button
            HorizontalDivider(thickness = 1.dp, color = WapiBorder)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = WapiWhite
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = WapiDimens.ScreenHorizontalPadding,
                            vertical = WapiDimens.SpaceSM
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatState.draftMessage,
                        onValueChange = onDraftMessageChange,
                        enabled = !chatState.isSending,
                        singleLine = false,
                        maxLines = 4,
                        placeholder = {
                            Text(
                                text = "Write a message…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WapiMutedText
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (chatState.draftMessage.trim().isNotEmpty() && !chatState.isSending) {
                                    focusManager.clearFocus()
                                    onSendMessageClick()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WapiSoftBackground,
                            unfocusedContainerColor = WapiSoftBackground,
                            disabledContainerColor = WapiSoftBackground.copy(alpha = 0.6f),
                            focusedBorderColor = WapiPrimaryGreen,
                            unfocusedBorderColor = WapiBorder,
                            errorBorderColor = WapiError,
                            focusedTextColor = WapiPrimaryText,
                            unfocusedTextColor = WapiPrimaryText,
                            cursorColor = WapiPrimaryGreen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    val canSend = chatState.draftMessage.trim().isNotEmpty() && !chatState.isSending
                    Surface(
                        shape = RoundedCornerShape(WapiDimens.RadiusMD),
                        color = if (canSend) WapiPrimaryGreen else WapiBorder,
                        modifier = Modifier
                            .size(48.dp)
                            .minimumInteractiveComponentSize()
                    ) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                onSendMessageClick()
                            },
                            enabled = canSend,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("chat_send_button")
                        ) {
                            if (chatState.isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = WapiWhite
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Send,
                                    contentDescription = "Send message",
                                    tint = if (canSend) WapiWhite else WapiMutedText,
                                    modifier = Modifier.size(20.dp)
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
fun WapiChatMessageBubble(
    message: WapiMessage,
    isSentByCurrentUser: Boolean,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "chat_message_item_0"
) {
    val alignment = if (isSentByCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSentByCurrentUser) WapiDeepGreen else WapiWhite
    val textColor = if (isSentByCurrentUser) WapiWhite else WapiPrimaryText
    val timestampColor = if (isSentByCurrentUser) WapiWhite.copy(alpha = 0.78f) else WapiMutedText
    val bubbleShape = if (isSentByCurrentUser) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 4.dp,
            bottomEnd = 16.dp
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTagPrefix),
        contentAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            border = if (isSentByCurrentUser) null else BorderStroke(1.dp, WapiBorder),
            modifier = Modifier.widthIn(min = 96.dp, max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.textContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.testTag("${testTagPrefix}_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = message.formattedTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = timestampColor,
                        modifier = Modifier.testTag("${testTagPrefix}_timestamp")
                    )

                    if (isSentByCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isEffectivelyRead) {
                                Icons.Outlined.DoneAll
                            } else {
                                Icons.Outlined.Done
                            },
                            contentDescription = if (message.isEffectivelyRead) "Read" else "Sent",
                            tint = timestampColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
