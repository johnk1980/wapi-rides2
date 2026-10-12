package com.example.ui.screens

import com.example.resources.Res
import com.example.resources.action_back
import com.example.resources.action_forgot_password
import com.example.resources.cta_cancel
import com.example.resources.cta_create_account
import com.example.resources.cta_done
import com.example.resources.cta_send_reset_link
import com.example.resources.cta_sign_in
import com.example.resources.forgot_password_subtitle
import com.example.resources.forgot_password_title
import com.example.resources.label_email
import com.example.resources.label_password
import com.example.resources.placeholder_email
import com.example.resources.placeholder_password
import com.example.resources.prompt_no_account
import com.example.resources.reset_password_sent_body
import com.example.resources.reset_password_sent_title
import com.example.resources.sign_in_subtitle
import com.example.resources.sign_in_title
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.WapiBrandLogoSlot
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiPrimaryButton
import com.example.ui.components.WapiSecondaryButton
import com.example.ui.components.WapiTextField
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.theme.WapiWhite
import com.example.ui.viewmodel.ForgotPasswordState
import com.example.ui.viewmodel.SignInFormState

@Composable
fun SignInScreen(
    formState: SignInFormState,
    forgotPasswordState: ForgotPasswordState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInSubmit: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onForgotPasswordEmailChange: (String) -> Unit,
    onForgotPasswordSubmit: () -> Unit,
    onForgotPasswordDismiss: () -> Unit,
    onNavigateToCreateAccount: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiSoftBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("sign_in_screen"),
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
            // Top bar with Back navigation
            IconButton(
                onClick = onNavigateBack,
                enabled = !formState.isSubmitting,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("sign_in_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                    tint = WapiPrimaryText
                )
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            WapiBrandLogoSlot(
                onDarkBackground = false,
                size = 56.dp
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            Text(
                text = stringResource(Res.string.sign_in_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("sign_in_title_text")
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Text(
                text = stringResource(Res.string.sign_in_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!formState.infoMessage.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = formState.infoMessage,
                    isError = false,
                    testTag = "sign_in_info_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            if (!formState.generalError.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = formState.generalError,
                    isError = true,
                    testTag = "sign_in_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            WapiTextField(
                value = formState.email,
                onValueChange = onEmailChange,
                label = stringResource(Res.string.label_email),
                placeholder = stringResource(Res.string.placeholder_email),
                errorMessage = formState.emailError,
                enabled = !formState.isSubmitting,
                leadingIcon = Icons.Outlined.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                testTag = "sign_in_email_input"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            WapiTextField(
                value = formState.password,
                onValueChange = onPasswordChange,
                label = stringResource(Res.string.label_password),
                placeholder = stringResource(Res.string.placeholder_password),
                errorMessage = formState.passwordError,
                isPassword = true,
                enabled = !formState.isSubmitting,
                leadingIcon = Icons.Outlined.Lock,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSignInSubmit()
                    }
                ),
                testTag = "sign_in_password_input"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = onForgotPasswordClick,
                    enabled = !formState.isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("forgot_password_button")
                ) {
                    Text(
                        text = stringResource(Res.string.action_forgot_password),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = WapiPrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            WapiPrimaryButton(
                text = stringResource(Res.string.cta_sign_in),
                onClick = {
                    focusManager.clearFocus()
                    onSignInSubmit()
                },
                isLoading = formState.isSubmitting,
                testTag = "sign_in_submit_button"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXL))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.prompt_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WapiSecondaryText
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onNavigateToCreateAccount,
                    enabled = !formState.isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("sign_in_to_create_account_button")
                ) {
                    Text(
                        text = stringResource(Res.string.cta_create_account),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryGreen
                    )
                }
            }
        }

        if (forgotPasswordState.isVisible) {
            ForgotPasswordModal(
                state = forgotPasswordState,
                onEmailChange = onForgotPasswordEmailChange,
                onSubmit = onForgotPasswordSubmit,
                onDismiss = onForgotPasswordDismiss
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordModal(
    state: ForgotPasswordState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {
            if (!state.isSubmitting) {
                onDismiss()
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("forgot_password_dialog"),
            shape = RoundedCornerShape(WapiDimens.RadiusLG),
            color = WapiWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WapiDimens.SpaceLG)
            ) {
                if (state.isSuccess) {
                    Text(
                        text = stringResource(Res.string.reset_password_sent_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = WapiPrimaryText
                    )
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))
                    WapiFeedbackBanner(
                        message = stringResource(Res.string.reset_password_sent_body),
                        isError = false,
                        testTag = "forgot_password_success_banner"
                    )
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))
                    WapiPrimaryButton(
                        text = stringResource(Res.string.cta_done),
                        onClick = onDismiss,
                        testTag = "forgot_password_done_button"
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.forgot_password_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = WapiPrimaryText
                    )
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))
                    Text(
                        text = stringResource(Res.string.forgot_password_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WapiSecondaryText
                    )
                    Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                    if (!state.errorMessage.isNullOrBlank()) {
                        WapiFeedbackBanner(
                            message = state.errorMessage,
                            isError = true,
                            testTag = "forgot_password_error_banner"
                        )
                        Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                    }

                    WapiTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = stringResource(Res.string.label_email),
                        placeholder = stringResource(Res.string.placeholder_email),
                        errorMessage = state.emailError,
                        enabled = !state.isSubmitting,
                        leadingIcon = Icons.Outlined.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        testTag = "forgot_password_email_input"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

                    WapiPrimaryButton(
                        text = stringResource(Res.string.cta_send_reset_link),
                        onClick = onSubmit,
                        isLoading = state.isSubmitting,
                        testTag = "forgot_password_submit_button"
                    )

                    Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

                    WapiSecondaryButton(
                        text = stringResource(Res.string.cta_cancel),
                        onClick = onDismiss,
                        enabled = !state.isSubmitting,
                        testTag = "forgot_password_cancel_button"
                    )
                }
            }
        }
    }
}
