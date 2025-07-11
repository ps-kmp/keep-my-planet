package pt.isel.keepmyplanet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        error = errorLight,
        onError = onErrorLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
    )

private val DarkColors =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        error = errorDark,
        onError = onErrorDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
    )

@Immutable
data class CustomColors(
    val statusReported: Color,
    val statusCleaningScheduled: Color,
    val statusCleaned: Color,
    val statusCancelled: Color,
    val statusUnknown: Color,
    val severityLow: Color,
    val severityMedium: Color,
    val severityHigh: Color,
    val severityUnknown: Color,
)

private val LightCustomColors =
    CustomColors(
        statusReported = statusReportedLight,
        statusCleaningScheduled = statusCleaningScheduledLight,
        statusCleaned = statusCleanedLight,
        statusCancelled = statusCancelledLight,
        statusUnknown = statusUnknownLight,
        severityLow = severityLowLight,
        severityMedium = severityMediumLight,
        severityHigh = severityHighLight,
        severityUnknown = severityUnknownLight,
    )

private val DarkCustomColors =
    CustomColors(
        statusReported = statusReportedDark,
        statusCleaningScheduled = statusCleaningScheduledDark,
        statusCleaned = statusCleanedDark,
        statusCancelled = statusCancelledDark,
        statusUnknown = statusUnknownDark,
        severityLow = severityLowDark,
        severityMedium = severityMediumDark,
        severityHigh = severityHighDark,
        severityUnknown = severityUnknownDark,
    )

private val LocalCustomColors = staticCompositionLocalOf { LightCustomColors }

@Composable
fun KeepMyPlanetTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (!useDarkTheme) LightColors else DarkColors
    val customColors = if (!useDarkTheme) LightCustomColors else DarkCustomColors

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colors,
            content = content,
        )
    }
}
