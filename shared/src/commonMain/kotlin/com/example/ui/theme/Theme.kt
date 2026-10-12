package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WapiLightColorScheme = lightColorScheme(
    primary = WapiPrimaryGreen,
    onPrimary = WapiWhite,
    primaryContainer = WapiGreenTint,
    onPrimaryContainer = WapiDeepGreen,
    secondary = WapiOrange,
    onSecondary = WapiPrimaryText,
    secondaryContainer = WapiOrangeTint,
    onSecondaryContainer = WapiPrimaryText,
    tertiary = WapiDeepGreen,
    onTertiary = WapiWhite,
    background = WapiSoftBackground,
    onBackground = WapiPrimaryText,
    surface = WapiWhite,
    onSurface = WapiPrimaryText,
    surfaceVariant = WapiSubtleSurface,
    onSurfaceVariant = WapiSecondaryText,
    outline = WapiBorder,
    outlineVariant = WapiMutedText,
    error = WapiError,
    onError = WapiWhite,
    errorContainer = WapiErrorContainer,
    onErrorContainer = WapiOnErrorContainer
)

val WapiShapes = Shapes(
    extraSmall = RoundedCornerShape(WapiDimens.RadiusSM),
    small = RoundedCornerShape(WapiDimens.RadiusSM),
    medium = RoundedCornerShape(WapiDimens.RadiusMD),
    large = RoundedCornerShape(WapiDimens.RadiusLG),
    extraLarge = RoundedCornerShape(WapiDimens.RadiusLG)
)

@Composable
fun WapiRidesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WapiLightColorScheme,
        typography = rememberWapiTypography(),
        shapes = WapiShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    WapiRidesTheme(content = content)
}
