package com.example.ui.screens

import com.example.resources.Res
import com.example.resources.action_back
import com.example.resources.create_account_subtitle
import com.example.resources.create_account_title
import com.example.resources.cta_create_account
import com.example.resources.cta_sign_in
import com.example.resources.label_confirm_password
import com.example.resources.label_email
import com.example.resources.label_full_name
import com.example.resources.label_password
import com.example.resources.label_phone
import com.example.resources.placeholder_confirm_password
import com.example.resources.placeholder_email
import com.example.resources.placeholder_full_name
import com.example.resources.placeholder_password
import com.example.resources.placeholder_phone
import com.example.resources.prompt_have_account
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.WapiBrandLogoSlot
import com.example.ui.components.WapiFeedbackBanner
import com.example.ui.components.WapiPrimaryButton
import com.example.ui.components.WapiTextField
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiPrimaryGreen
import com.example.ui.theme.WapiPrimaryText
import com.example.ui.theme.WapiSecondaryText
import com.example.ui.theme.WapiSoftBackground
import com.example.ui.viewmodel.CreateAccountFormState

@Composable
fun CreateAccountScreen(
    formState: CreateAccountFormState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToSignIn: () -> Unit,
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
            .testTag("create_account_screen"),
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
                    .testTag("create_account_back_button")
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
                text = stringResource(Res.string.create_account_title),
                style = MaterialTheme.typography.headlineLarge,
                color = WapiPrimaryText,
                modifier = Modifier.testTag("create_account_title_text")
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceXS))

            Text(
                text = stringResource(Res.string.create_account_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = WapiSecondaryText
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            if (!formState.generalError.isNullOrBlank()) {
                WapiFeedbackBanner(
                    message = formState.generalError,
                    isError = true,
                    testTag = "create_account_error_banner"
                )
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
            }

            WapiTextField(
                value = formState.fullName,
                onValueChange = onFullNameChange,
                label = stringResource(Res.string.label_full_name),
                placeholder = stringResource(Res.string.placeholder_full_name),
                errorMessage = formState.fullNameError,
                enabled = !formState.isSubmitting,
                leadingIcon = Icons.Outlined.Person,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                testTag = "create_account_full_name_input"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            WapiTextField(
                value = formState.phone,
                onValueChange = onPhoneChange,
                label = stringResource(Res.string.label_phone),
                placeholder = stringResource(Res.string.placeholder_phone),
                errorMessage = formState.phoneError,
                enabled = !formState.isSubmitting,
                leadingIcon = Icons.Outlined.Phone,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                testTag = "create_account_phone_input"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

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
                testTag = "create_account_email_input"
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
                    imeAction = ImeAction.Next
                ),
                testTag = "create_account_password_input"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

            WapiTextField(
                value = formState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(Res.string.label_confirm_password),
                placeholder = stringResource(Res.string.placeholder_confirm_password),
                errorMessage = formState.confirmPasswordError,
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
                        onSubmit()
                    }
                ),
                testTag = "create_account_confirm_password_input"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            WapiPrimaryButton(
                text = stringResource(Res.string.cta_create_account),
                onClick = {
                    focusManager.clearFocus()
                    onSubmit()
                },
                isLoading = formState.isSubmitting,
                testTag = "create_account_submit_button"
            )

            Spacer(modifier = Modifier.height(WapiDimens.SpaceLG))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.prompt_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WapiSecondaryText
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onNavigateToSignIn,
                    enabled = !formState.isSubmitting,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("create_account_to_sign_in_button")
                ) {
                    Text(
                        text = stringResource(Res.string.cta_sign_in),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WapiPrimaryGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
        }
    }
}
