package com.example.ui.screens

import com.example.resources.Res
import com.example.resources.cta_create_account
import com.example.resources.cta_sign_in
import com.example.resources.welcome_headline
import com.example.resources.welcome_subheadline
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WapiBrandLogoSlot
import com.example.ui.components.WapiPrimaryButton
import com.example.ui.components.WapiSecondaryButton
import com.example.ui.theme.WapiDeepGreen
import com.example.ui.theme.WapiDimens
import com.example.ui.theme.WapiOrange
import com.example.ui.theme.WapiWhite

@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WapiDeepGreen)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("welcome_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = WapiDimens.MaxContentWidth)
                .padding(
                    horizontal = WapiDimens.ScreenHorizontalPadding,
                    vertical = WapiDimens.SpaceXL
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Mark Area
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))
                WapiBrandLogoSlot(
                    onDarkBackground = true,
                    size = 72.dp
                )
            }

            // Editorial Core Brand Message
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = WapiOrange,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "KENYAN COMMUNITY MOBILITY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.4.sp
                        ),
                        color = WapiWhite.copy(alpha = 0.78f)
                    )
                }

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                Text(
                    text = stringResource(Res.string.welcome_headline),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        lineHeight = 44.sp
                    ),
                    color = WapiWhite,
                    modifier = Modifier.testTag("welcome_headline_text")
                )

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                HorizontalDivider(
                    modifier = Modifier.width(48.dp),
                    thickness = 2.dp,
                    color = WapiOrange
                )

                Spacer(modifier = Modifier.height(WapiDimens.SpaceMD))

                Text(
                    text = stringResource(Res.string.welcome_subheadline),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        lineHeight = 28.sp
                    ),
                    color = WapiWhite.copy(alpha = 0.88f),
                    modifier = Modifier.testTag("welcome_subheadline_text")
                )
            }

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                WapiPrimaryButton(
                    text = stringResource(Res.string.cta_create_account),
                    onClick = onCreateAccountClick,
                    useOrangeAccent = true,
                    testTag = "welcome_create_account_button"
                )

                Spacer(modifier = Modifier.height(WapiDimens.SpaceSM))

                WapiSecondaryButton(
                    text = stringResource(Res.string.cta_sign_in),
                    onClick = onSignInClick,
                    onDarkBackground = true,
                    testTag = "welcome_sign_in_button"
                )
            }
        }
    }
}
