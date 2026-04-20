package com.ims.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = PrimaryBlueDark,
    secondary = SecondaryGreen,
    onSecondary = TextOnPrimary,
    secondaryContainer = SecondaryGreenContainer,
    onSecondaryContainer = SecondaryGreenDark,
    tertiary = WarningAmber,
    onTertiary = TextOnPrimary,
    tertiaryContainer = WarningAmberContainer,
    onTertiaryContainer = WarningAmberDark,
    error = DangerRed,
    onError = TextOnPrimary,
    errorContainer = DangerRedContainer,
    onErrorContainer = DangerRedDark,
    background = BackgroundLight,
    onBackground = TextMain,
    surface = SurfaceWhite,
    onSurface = TextMain,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = PrimaryBlueDark,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,
    secondary = SecondaryGreenLight,
    onSecondary = SecondaryGreenDark,
    secondaryContainer = SecondaryGreenDark,
    onSecondaryContainer = SecondaryGreenLight,
    tertiary = WarningAmberLight,
    onTertiary = WarningAmberDark,
    tertiaryContainer = WarningAmberDark,
    onTertiaryContainer = WarningAmberLight,
    error = DangerRedLight,
    onError = DangerRedDark,
    errorContainer = DangerRedDark,
    onErrorContainer = DangerRedLight,
    background = DarkBackground,
    onBackground = DarkTextMain,
    surface = DarkSurface,
    onSurface = DarkTextMain,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkSurfaceVariant
)

@Composable
fun IMSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = IMSTypography,
        content = content
    )
}
