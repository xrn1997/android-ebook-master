package com.xrn1997.common.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = lib_common_light_primary,
    onPrimary = lib_common_light_onPrimary,
    primaryContainer = lib_common_light_primaryContainer,
    onPrimaryContainer = lib_common_light_onPrimaryContainer,
    secondary = lib_common_light_secondary,
    onSecondary = lib_common_light_onSecondary,
    secondaryContainer = lib_common_light_secondaryContainer,
    onSecondaryContainer = lib_common_light_onSecondaryContainer,
    tertiary = lib_common_light_tertiary,
    onTertiary = lib_common_light_onTertiary,
    tertiaryContainer = lib_common_light_tertiaryContainer,
    onTertiaryContainer = lib_common_light_onTertiaryContainer,
    error = lib_common_light_error,
    onError = lib_common_light_onError,
    errorContainer = lib_common_light_errorContainer,
    onErrorContainer = lib_common_light_onErrorContainer,
    outline = lib_common_light_outline,
    background = lib_common_light_background,
    onBackground = lib_common_light_onBackground,
    surface = lib_common_light_surface,
    onSurface = lib_common_light_onSurface,
    surfaceVariant = lib_common_light_surfaceVariant,
    onSurfaceVariant = lib_common_light_onSurfaceVariant,
    inverseSurface = lib_common_light_inverseSurface,
    inverseOnSurface = lib_common_light_inverseOnSurface,
    inversePrimary = lib_common_light_inversePrimary,
    surfaceTint = lib_common_light_surfaceTint,
    outlineVariant = lib_common_light_outlineVariant,
    scrim = lib_common_light_scrim,
)


private val DarkColors = darkColorScheme(
    primary = lib_common_dark_primary,
    onPrimary = lib_common_dark_onPrimary,
    primaryContainer = lib_common_dark_primaryContainer,
    onPrimaryContainer = lib_common_dark_onPrimaryContainer,
    secondary = lib_common_dark_secondary,
    onSecondary = lib_common_dark_onSecondary,
    secondaryContainer = lib_common_dark_secondaryContainer,
    onSecondaryContainer = lib_common_dark_onSecondaryContainer,
    tertiary = lib_common_dark_tertiary,
    onTertiary = lib_common_dark_onTertiary,
    tertiaryContainer = lib_common_dark_tertiaryContainer,
    onTertiaryContainer = lib_common_dark_onTertiaryContainer,
    error = lib_common_dark_error,
    onError = lib_common_dark_onError,
    errorContainer = lib_common_dark_errorContainer,
    onErrorContainer = lib_common_dark_onErrorContainer,
    outline = lib_common_dark_outline,
    background = lib_common_dark_background,
    onBackground = lib_common_dark_onBackground,
    surface = lib_common_dark_surface,
    onSurface = lib_common_dark_onSurface,
    surfaceVariant = lib_common_dark_surfaceVariant,
    onSurfaceVariant = lib_common_dark_onSurfaceVariant,
    inverseSurface = lib_common_dark_inverseSurface,
    inverseOnSurface = lib_common_dark_inverseOnSurface,
    inversePrimary = lib_common_dark_inversePrimary,
    surfaceTint = lib_common_dark_surfaceTint,
    outlineVariant = lib_common_dark_outlineVariant,
    scrim = lib_common_dark_scrim,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}